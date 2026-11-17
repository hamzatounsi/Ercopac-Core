#!/usr/bin/env bash
set -euo pipefail

readonly script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${script_dir}/staging-common.sh"

usage() {
  echo "Usage: $0 [--resume]" >&2
}

resume=false
case "${1:-}" in
  "") ;;
  --resume) resume=true ;;
  *) usage; exit 2 ;;
esac
(( $# <= 1 )) || { usage; exit 2; }

assert_safe_layout
bash "${script_dir}/validate-staging.sh"
if [[ "${resume}" == false ]]; then
  check_build_capacity
fi

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

resume_fail() {
  echo "RESUME REFUSED: $*" >&2
  exit 1
}

verify_resume_service_image() {
  local service="$1" repository="$2" checkout="$3"
  local container_id image_ref revision tag running_image_id tagged_image_id

  container_id="$(service_container_id "${service}")"
  [[ -n "${container_id}" ]] || resume_fail "staging ${service} container is missing."

  image_ref="$(docker inspect --format '{{.Config.Image}}' "${container_id}" 2>/dev/null)" ||
    resume_fail "cannot inspect the running ${service} container image reference."
  revision="$(docker inspect --format '{{index .Config.Labels "org.opencontainers.image.revision"}}' "${container_id}" 2>/dev/null)" ||
    resume_fail "cannot inspect the running ${service} revision label."

  [[ "${image_ref}" == "${repository}:"* ]] ||
    resume_fail "${service} uses unexpected image '${image_ref}'; expected ${repository}:<commit>."
  tag="${image_ref#${repository}:}"
  require_sha "${tag}" ||
    resume_fail "${service} image tag '${tag}' is not a lowercase 40-character Git SHA."
  require_sha "${revision}" ||
    resume_fail "${service} revision label '${revision:-<empty>}' is not a lowercase 40-character Git SHA."
  [[ "${tag}" == "${revision}" ]] ||
    resume_fail "${service} image tag ${tag} differs from its revision label ${revision}."

  running_image_id="$(docker inspect --format '{{.Image}}' "${container_id}" 2>/dev/null)" ||
    resume_fail "cannot inspect the image ID used by running ${service}."
  tagged_image_id="$(docker image inspect --format '{{.Id}}' "${image_ref}" 2>/dev/null)" ||
    resume_fail "immutable image ${image_ref} is not present locally."
  [[ "${running_image_id}" == "${tagged_image_id}" ]] ||
    resume_fail "${image_ref} no longer resolves to the image used by running ${service}."

  [[ -d "${checkout}/.git" ]] || resume_fail "missing Git checkout ${checkout}."
  git -C "${checkout}" cat-file -e "${revision}^{commit}" 2>/dev/null ||
    resume_fail "running ${service} revision ${revision} does not exist in ${checkout}."

  printf '%s\n' "${revision}"
}

if [[ "${resume}" == true ]]; then
  [[ ! -e "${STATE_FILE}" ]] ||
    resume_fail "${STATE_FILE} already exists; --resume is only for an otherwise successful initialization whose state file is absent."

  # Deliberately do not call build, pull, up, start, restart, or any schema tool.
  refresh_compose_command
  "${COMPOSE[@]}" config --quiet || resume_fail "staging Compose configuration does not render successfully."

  backend_commit="$(verify_resume_service_image backend projectum-staging-backend "${STAGING_ROOT}/backend")"
  frontend_commit="$(verify_resume_service_image frontend projectum-staging-frontend "${STAGING_ROOT}/frontend")"

  for service in db backend frontend; do
    health="$(service_health "${service}")" || resume_fail "cannot inspect staging ${service} health."
    [[ "${health}" == "healthy" ]] ||
      resume_fail "staging ${service} is '${health}', not healthy. No container was changed."
  done

  frontend_id="$(service_container_id frontend)"
  frontend_networks="$(docker inspect --format '{{range $name, $_ := .NetworkSettings.Networks}}{{println $name}}{{end}}' "${frontend_id}" 2>/dev/null)" ||
    resume_fail "cannot inspect frontend networks."
  grep -Fxq 'projectum-staging_staging-internal' <<< "${frontend_networks}" ||
    resume_fail "frontend is not attached to projectum-staging_staging-internal."
  grep -Fxq 'projectum-staging_staging-edge' <<< "${frontend_networks}" ||
    resume_fail "frontend is not attached to projectum-staging_staging-edge; loopback publishing cannot be certified."

  check_backend_readiness ||
    resume_fail "backend Actuator did not report status UP from inside the staging backend container."
  check_frontend_readiness ||
    resume_fail "http://127.0.0.1:$(env_value STAGING_HTTP_PORT)/healthz did not return a successful response."

  if ! table_count="$("${COMPOSE[@]}" exec -T db sh -ec \
    'psql -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d "$POSTGRES_DB" -Atc "select count(*) from information_schema.tables where table_schema = '\''public'\'' and table_type = '\''BASE TABLE'\''"')"; then
    resume_fail "could not query public PostgreSQL tables through the healthy staging database container."
  fi
  [[ "${table_count}" =~ ^[0-9]+$ ]] ||
    resume_fail "PostgreSQL returned an invalid public-table count: '${table_count}'."
  (( table_count > 0 )) ||
    resume_fail "the staging database has no public base tables; schema readiness cannot be certified."

  # Recheck under the shared lock immediately before the sole state transition.
  [[ ! -e "${STATE_FILE}" ]] ||
    resume_fail "${STATE_FILE} appeared during verification; refusing to overwrite it."
  [[ -w "${STAGING_ROOT}" ]] ||
    resume_fail "${STAGING_ROOT} is not writable, so deployment state cannot be recorded."
  [[ ! -L "${STAGING_ROOT}/deployments.tsv" ]] ||
    resume_fail "${STAGING_ROOT}/deployments.tsv must not be a symbolic link."
  [[ ! -e "${STAGING_ROOT}/deployments.tsv" || -f "${STAGING_ROOT}/deployments.tsv" ]] ||
    resume_fail "${STAGING_ROOT}/deployments.tsv exists but is not a regular file."
  [[ ! -e "${STAGING_ROOT}/deployments.tsv" || -w "${STAGING_ROOT}/deployments.tsv" ]] ||
    resume_fail "${STAGING_ROOT}/deployments.tsv is not writable."
  assert_production_unchanged "${production_before}" ||
    resume_fail "the production fingerprint changed during recovery verification."

  write_deployment_state \
    "${backend_commit}" "${frontend_commit}" "${backend_commit}" "${frontend_commit}"
  printf '%s\tresume\tbackend=%s\tfrontend=%s\n' \
    "$(date --iso-8601=seconds)" "${backend_commit}" "${frontend_commit}" \
    >> "${STAGING_ROOT}/deployments.tsv" || {
      echo "RESUME ERROR: deployment state was written, but the resume audit event could not be appended to ${STAGING_ROOT}/deployments.tsv." >&2
      exit 1
    }

  echo "Staging initialization state recovered without rebuilding, restarting, recreating, or modifying application data; ${table_count} public base tables detected."
  exit 0
fi

if [[ -f "${STATE_FILE}" ]]; then
  echo "Existing successful staging state found; reconciling without rebuilding or deleting data."
  load_and_validate_state
  backend_tag="$(state_value BACKEND_IMAGE_TAG)"
  frontend_tag="$(state_value FRONTEND_IMAGE_TAG)"
  backend_commit="$(state_value BACKEND_COMMIT)"
  frontend_commit="$(state_value FRONTEND_COMMIT)"
  require_image "projectum-staging-backend:${backend_tag}" || {
    echo "Recorded backend image is missing; use a reviewed regular deployment to restore it." >&2
    exit 1
  }
  require_image "projectum-staging-frontend:${frontend_tag}" || {
    echo "Recorded frontend image is missing; use a reviewed regular deployment to restore it." >&2
    exit 1
  }
else
  echo "No prior staging state found; preparing immutable first-deployment images."
  for repo in "${STAGING_ROOT}/backend" "${STAGING_ROOT}/frontend"; do
    [[ -d "${repo}/.git" ]] || { echo "Missing Git checkout: ${repo}" >&2; exit 1; }
    [[ -z "$(git -C "${repo}" status --porcelain --untracked-files=no)" ]] || {
      echo "Refusing first deployment with tracked changes in ${repo}." >&2
      exit 1
    }
  done
  backend_commit="$(git -C "${STAGING_ROOT}/backend" rev-parse HEAD)"
  frontend_commit="$(git -C "${STAGING_ROOT}/frontend" rev-parse HEAD)"
  require_sha "${backend_commit}" && require_sha "${frontend_commit}"
  backend_tag="${backend_commit}"
  frontend_tag="${frontend_commit}"
  export BACKEND_IMAGE_TAG="${backend_tag}" BACKEND_DEPLOYMENT_ID="${backend_commit}"
  export FRONTEND_IMAGE_TAG="${frontend_tag}" FRONTEND_DEPLOYMENT_ID="${frontend_commit}"
  refresh_compose_command

  echo "Building backend image first (no staging containers are started yet)."
  "${COMPOSE[@]}" build backend
  check_build_capacity
  echo "Building frontend image second. Builds are intentionally sequential."
  "${COMPOSE[@]}" build frontend
fi

export BACKEND_IMAGE_TAG="${backend_tag}" BACKEND_DEPLOYMENT_ID="${backend_commit}"
export FRONTEND_IMAGE_TAG="${frontend_tag}" FRONTEND_DEPLOYMENT_ID="${frontend_commit}"
refresh_compose_command
"${COMPOSE[@]}" config --quiet

echo "Starting only the staging PostgreSQL service."
"${COMPOSE[@]}" up -d --no-deps db
wait_for_health db 180

echo "Starting only the staging backend after PostgreSQL is healthy."
"${COMPOSE[@]}" up -d --no-deps backend
if ! wait_for_health backend 300 || ! check_backend_readiness; then
  "${COMPOSE[@]}" logs --tail=200 backend >&2 || true
  echo "Backend failed to initialize a healthy application/schema. Staging was left intact for diagnosis." >&2
  exit 1
fi

table_count="$("${COMPOSE[@]}" exec -T db sh -ec \
  'psql -U "$POSTGRES_USER" -d "$POSTGRES_DB" -Atc "select count(*) from information_schema.tables where table_schema = '\''public'\''"')"
if [[ ! "${table_count}" =~ ^[0-9]+$ ]] || (( table_count == 0 )); then
  echo "Backend became healthy but no public PostgreSQL tables were found." >&2
  exit 1
fi

echo "Starting only the staging frontend after backend readiness."
"${COMPOSE[@]}" up -d --no-deps frontend
if ! wait_for_health frontend 120 || ! check_frontend_readiness; then
  "${COMPOSE[@]}" logs --tail=100 frontend >&2 || true
  echo "Frontend health verification failed. Staging was left intact for diagnosis." >&2
  exit 1
fi

write_deployment_state "${backend_tag}" "${frontend_tag}" "${backend_commit}" "${frontend_commit}"
printf '%s\tinitialize\tbackend=%s\tfrontend=%s\n' \
  "$(date --iso-8601=seconds)" "${backend_commit}" "${frontend_commit}" \
  >> "${STAGING_ROOT}/deployments.tsv"

"${COMPOSE[@]}" ps
echo "Staging initialization is healthy and complete; ${table_count} public tables detected."
