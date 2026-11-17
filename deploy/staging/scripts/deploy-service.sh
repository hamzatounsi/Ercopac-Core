#!/usr/bin/env bash
set -Eeuo pipefail

readonly script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${script_dir}/staging-common.sh"

service="${1:-}"
target_commit="${2:-}"
case "${service}" in
  backend) repo="${STAGING_ROOT}/backend" ;;
  frontend) repo="${STAGING_ROOT}/frontend" ;;
  *) echo "Usage: $0 backend|frontend COMMIT_SHA" >&2; exit 2 ;;
esac
require_sha "${target_commit}" || {
  echo "A lowercase, full 40-character commit SHA is required." >&2
  exit 2
}

assert_safe_layout
bash "${script_dir}/validate-staging.sh"
load_and_validate_state
check_build_capacity

exec 9>"${STAGING_ROOT}/.deploy.lock"
flock -w 900 9 || { echo "Another staging operation holds the deployment lock." >&2; exit 1; }

production_before="$(production_snapshot)"
verify_production_on_exit() {
  local rc=$?
  trap - EXIT
  if ! assert_production_unchanged "${production_before}"; then
    rc=1
  fi
  exit "${rc}"
}
trap verify_production_on_exit EXIT

if [[ "${service}" == "backend" && "$(service_health db)" != "healthy" ]]; then
  echo "Staging database is not healthy. Run initialize-staging.sh; regular deployment will not create it." >&2
  exit 1
fi
if [[ "${service}" == "frontend" && "$(service_health backend)" != "healthy" ]]; then
  echo "Staging backend is not healthy. Frontend deployment will not recreate backend or database." >&2
  exit 1
fi

previous_backend_tag="$(state_value BACKEND_IMAGE_TAG)"
previous_frontend_tag="$(state_value FRONTEND_IMAGE_TAG)"
previous_backend_commit="$(state_value BACKEND_COMMIT)"
previous_frontend_commit="$(state_value FRONTEND_COMMIT)"
if [[ "${service}" == "backend" ]]; then
  previous_tag="${previous_backend_tag}"
  previous_commit="${previous_backend_commit}"
  image_repository="projectum-staging-backend"
else
  previous_tag="${previous_frontend_tag}"
  previous_commit="${previous_frontend_commit}"
  image_repository="projectum-staging-frontend"
fi

[[ -z "$(git -C "${repo}" status --porcelain --untracked-files=no)" ]] || {
  echo "Refusing to overwrite tracked changes in ${repo}." >&2
  exit 1
}
current_commit="$(git -C "${repo}" rev-parse HEAD)"
[[ "${current_commit}" == "${previous_commit}" ]] || {
  echo "Checkout ${current_commit} does not match successful state ${previous_commit}; refusing ambiguous rollback." >&2
  exit 1
}
require_image "${image_repository}:${previous_tag}" || {
  echo "Previous immutable rollback image ${image_repository}:${previous_tag} is missing." >&2
  exit 1
}

# Git failures occur before checkout/container mutation whenever possible.
git -C "${repo}" fetch --prune origin staging
git -C "${repo}" cat-file -e "${target_commit}^{commit}"
git -C "${repo}" merge-base --is-ancestor "${target_commit}" origin/staging || {
  echo "Commit ${target_commit} is not part of origin/staging." >&2
  exit 1
}

if [[ "${target_commit}" == "${previous_commit}" ]]; then
  echo "${service} ${target_commit} is already the last successful deployment; verifying health only."
  if [[ "${service}" == "backend" ]]; then
    check_backend_readiness
  else
    check_frontend_readiness
  fi
  exit 0
fi

if [[ "${service}" == "backend" ]]; then
  # Regular backend deployment requires an existing healthy staging database.
  # This backup is never restored automatically.
  install -d -m 0700 "${STAGING_ROOT}/backups"
  backup_file="${STAGING_ROOT}/backups/staging_$(date +%Y%m%d_%H%M%S)_${previous_commit}.sql"
  incomplete_backup="${backup_file}.incomplete"
  if ! "${COMPOSE[@]}" exec -T db sh -ec \
    'pg_dump --no-owner --no-acl -U "$POSTGRES_USER" -d "$POSTGRES_DB"' > "${incomplete_backup}"; then
    rm -f -- "${incomplete_backup}"
    echo "Staging database backup failed; deployment was not started." >&2
    exit 1
  fi
  chmod 0600 "${incomplete_backup}"
  mv "${incomplete_backup}" "${backup_file}"
fi

checkout_changed=false
container_replace_attempted=false
rollback_failed=false

deployment_failure() {
  local original_rc=$?
  trap - ERR
  set +e
  echo "Staging ${service} deployment failed; attempting application-only rollback." >&2

  if [[ "${checkout_changed}" == "true" ]]; then
    if ! git -C "${repo}" checkout --detach "${previous_commit}"; then
      echo "ROLLBACK FAILURE: could not restore Git checkout ${previous_commit}." >&2
      rollback_failed=true
    fi
  fi

  if [[ "${container_replace_attempted}" == "true" ]]; then
    if [[ "${service}" == "backend" ]]; then
      export BACKEND_IMAGE_TAG="${previous_backend_tag}" BACKEND_DEPLOYMENT_ID="${previous_backend_commit}"
    else
      export FRONTEND_IMAGE_TAG="${previous_frontend_tag}" FRONTEND_DEPLOYMENT_ID="${previous_frontend_commit}"
    fi
    refresh_compose_command
    if ! require_image "${image_repository}:${previous_tag}" || \
       ! "${COMPOSE[@]}" up -d --no-deps "${service}" || \
       ! wait_for_health "${service}" 240; then
      echo "ROLLBACK FAILURE: previous immutable ${service} image could not be restored." >&2
      rollback_failed=true
    elif [[ "${service}" == "backend" ]] && ! check_backend_readiness; then
      echo "ROLLBACK FAILURE: restored backend image is not application-ready." >&2
      rollback_failed=true
    elif [[ "${service}" == "frontend" ]] && ! check_frontend_readiness; then
      echo "ROLLBACK FAILURE: restored frontend image is not healthy." >&2
      rollback_failed=true
    else
      echo "Rollback succeeded using immutable image ${image_repository}:${previous_tag}." >&2
    fi
  else
    echo "The running ${service} container was not replaced; only the Git checkout needed restoration." >&2
  fi

  if [[ "${rollback_failed}" == "true" ]]; then
    echo "Manual staging recovery is required. Production was not targeted." >&2
  fi
  exit "${original_rc}"
}
trap deployment_failure ERR

git -C "${repo}" checkout --detach "${target_commit}"
checkout_changed=true

if [[ "${service}" == "backend" ]]; then
  export BACKEND_IMAGE_TAG="${target_commit}" BACKEND_DEPLOYMENT_ID="${target_commit}"
else
  export FRONTEND_IMAGE_TAG="${target_commit}" FRONTEND_DEPLOYMENT_ID="${target_commit}"
fi
refresh_compose_command
"${COMPOSE[@]}" config --quiet
"${COMPOSE[@]}" build "${service}"

container_replace_attempted=true
"${COMPOSE[@]}" up -d --no-deps "${service}"
wait_for_health "${service}" 300
if [[ "${service}" == "backend" ]]; then
  check_backend_readiness
  write_deployment_state "${target_commit}" "${previous_frontend_tag}" \
    "${target_commit}" "${previous_frontend_commit}"
else
  check_frontend_readiness
  write_deployment_state "${previous_backend_tag}" "${target_commit}" \
    "${previous_backend_commit}" "${target_commit}"
fi

trap - ERR
printf '%s\t%s\t%s\n' "$(date --iso-8601=seconds)" "${service}" "${target_commit}" \
  >> "${STAGING_ROOT}/deployments.tsv"
"${COMPOSE[@]}" ps "${service}"
echo "Staging ${service} deployment succeeded. Database backups are not automatic rollback points."
