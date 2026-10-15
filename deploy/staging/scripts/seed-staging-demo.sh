#!/usr/bin/env bash
set -Eeuo pipefail

readonly script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${script_dir}/staging-common.sh"

[[ "${1:-}" == "--confirm-staging-demo" && $# -eq 1 ]] || {
  echo "Usage: $0 --confirm-staging-demo" >&2
  exit 2
}

assert_safe_layout
unset APP_STAGING_SEED_ENABLED
bash "${script_dir}/validate-staging.sh"
load_and_validate_state
[[ "$(env_value APP_STAGING_SEED_ENABLED)" == "false" ]] || {
  echo "APP_STAGING_SEED_ENABLED must remain false in ${ENV_FILE}; this script enables it only in-process." >&2
  exit 1
}

exec 9>"${STAGING_ROOT}/.deploy.lock"
flock -w 900 9 || { echo "Another staging operation holds the deployment lock." >&2; exit 1; }

production_before="$(production_snapshot)"
seed_boot_active=false

finish() {
  local rc=$? cleanup_failed=false
  trap - EXIT ERR
  set +e

  if [[ "${seed_boot_active}" == true ]]; then
    echo "Restoring the staging backend with demo seeding disabled." >&2
    export APP_STAGING_SEED_ENABLED=false
    refresh_compose_command
    if ! "${COMPOSE[@]}" up -d --no-deps backend \
       || ! wait_for_health backend 300 \
       || ! check_backend_readiness; then
      echo "SEED CLEANUP FAILURE: staging backend could not be verified with seeding disabled." >&2
      cleanup_failed=true
    fi
  fi
  if ! assert_production_unchanged "${production_before}"; then
    cleanup_failed=true
  fi
  if [[ "${cleanup_failed}" == true ]]; then
    rc=1
  fi
  exit "${rc}"
}
trap finish EXIT

[[ "$(service_health db)" == "healthy" ]] || {
  echo "Staging PostgreSQL is not healthy; no container was changed." >&2
  exit 1
}
[[ "$(service_health backend)" == "healthy" ]] || {
  echo "Staging backend is not healthy; refusing the controlled seed boot." >&2
  exit 1
}

# Pin Compose operations to the successfully deployed staging images.
export BACKEND_IMAGE_TAG="$(state_value BACKEND_IMAGE_TAG)"
export BACKEND_DEPLOYMENT_ID="$(state_value BACKEND_COMMIT)"
export FRONTEND_IMAGE_TAG="$(state_value FRONTEND_IMAGE_TAG)"
export FRONTEND_DEPLOYMENT_ID="$(state_value FRONTEND_COMMIT)"
refresh_compose_command

backend_tag="$(state_value BACKEND_IMAGE_TAG)"
require_image "projectum-staging-backend:${backend_tag}" || {
  echo "Recorded immutable backend image projectum-staging-backend:${backend_tag} is missing." >&2
  exit 1
}

echo "Starting one staging-only backend boot with demo seeding enabled. No image will be built."
export APP_STAGING_SEED_ENABLED=true
refresh_compose_command
"${COMPOSE[@]}" config --quiet
seed_boot_active=true
"${COMPOSE[@]}" up -d --no-deps backend
wait_for_health backend 300
check_backend_readiness

if ! counts="$("${COMPOSE[@]}" exec -T db sh -ec \
  'psql -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d "$POSTGRES_DB" -At -F "|" -c "
    select
      (select count(*) from organisations where code = '\''STAGING_DEMO'\''),
      (select count(*) from users where email like '\''%@projectum.example'\''),
      (select count(*) from projects where code like '\''STG-DEMO-%'\'');
  "')"; then
  echo "Could not verify the staging demo records; the backend will be restored with seeding disabled." >&2
  exit 1
fi
IFS='|' read -r organisation_count user_count project_count <<< "${counts}"
[[ "${organisation_count}" == "1" && "${user_count}" == "8" && "${project_count}" == "2" ]] || {
  echo "Unexpected demo counts: organisations=${organisation_count}, users=${user_count}, projects=${project_count}." >&2
  exit 1
}

echo "Demo records verified. Recreating only the staging backend with seeding disabled."
export APP_STAGING_SEED_ENABLED=false
refresh_compose_command
"${COMPOSE[@]}" up -d --no-deps backend
wait_for_health backend 300
check_backend_readiness
seed_boot_active=false

printf '%s\tseed-demo\tbackend=%s\tusers=%s\tprojects=%s\n' \
  "$(date --iso-8601=seconds)" "${backend_tag}" "${user_count}" "${project_count}" \
  >> "${STAGING_ROOT}/deployments.tsv"

echo "Staging demo initialization succeeded; seeding is disabled and the backend is healthy."
