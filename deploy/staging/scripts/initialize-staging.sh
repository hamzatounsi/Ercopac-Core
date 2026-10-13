#!/usr/bin/env bash
set -euo pipefail

readonly script_dir="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
source "${script_dir}/staging-common.sh"

assert_safe_layout
bash "${script_dir}/validate-staging.sh"
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
