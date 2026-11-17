#!/usr/bin/env bash

# Shared, side-effect-free helpers for Projectum staging scripts. Callers must
# enable `set -euo pipefail` before sourcing this file.
readonly STAGING_ROOT="/opt/projectum-staging"
readonly COMPOSE_FILE="${STAGING_ROOT}/backend/deploy/staging/compose.staging.yml"
readonly ENV_FILE="${STAGING_ROOT}/.env"
readonly STATE_FILE="${STAGING_ROOT}/.deployment-state"
readonly PROJECT_NAME="projectum-staging"

assert_safe_layout() {
  local script_root
  script_root="$(cd "$(dirname "${BASH_SOURCE[1]}")/.." && pwd)"
  [[ "${script_root}" == "${STAGING_ROOT}/backend/deploy/staging" ]] || {
    echo "Safety check failed: scripts must run from the dedicated staging checkout." >&2
    return 1
  }
  [[ "${STAGING_ROOT}" == "/opt/projectum-staging" ]] || return 1
  [[ "${COMPOSE_FILE}" != /opt/projectum/* ]] || return 1
  [[ -f "${COMPOSE_FILE}" && -f "${ENV_FILE}" ]] || {
    echo "Missing staging Compose or .env configuration." >&2
    return 1
  }
}

refresh_compose_command() {
  COMPOSE=(
    docker compose
    --project-name "${PROJECT_NAME}"
    --env-file "${ENV_FILE}"
  )
  if [[ -f "${STATE_FILE}" ]]; then
    COMPOSE+=(--env-file "${STATE_FILE}")
  fi
  COMPOSE+=(-f "${COMPOSE_FILE}")
}

env_value() {
  local key="$1"
  sed -n "s/^${key}=//p" "${ENV_FILE}" | tail -1
}

state_value() {
  local key="$1"
  [[ -f "${STATE_FILE}" ]] || return 1
  sed -n "s/^${key}=//p" "${STATE_FILE}" | tail -1
}

require_sha() {
  [[ "$1" =~ ^[0-9a-f]{40}$ ]]
}

check_build_capacity() {
  local available_kib available_disk_kib
  available_kib="$(awk '/MemAvailable:/ {print $2}' /proc/meminfo)"
  available_disk_kib="$(df -Pk "${STAGING_ROOT}" | awk 'NR==2 {print $4}')"
  if (( available_kib < 1572864 )); then
    echo "Refusing to build with less than 1.5 GiB available RAM (${available_kib} KiB available)." >&2
    return 1
  fi
  if (( available_disk_kib < 5242880 )); then
    echo "Refusing to build with less than 5 GiB free disk (${available_disk_kib} KiB available)." >&2
    return 1
  fi
}

production_snapshot() {
  local ids id
  ids="$(docker ps -q --filter 'label=com.docker.compose.project=projectum' | sort)"
  [[ -n "${ids}" ]] || {
    echo "Production projectum containers were not found; refusing staging mutation." >&2
    return 1
  }
  while IFS= read -r id; do
    docker inspect --format '{{.Id}} {{.State.StartedAt}} {{.RestartCount}}' "${id}"
  done <<< "${ids}"
}

assert_production_unchanged() {
  local before="$1" after
  after="$(production_snapshot)"
  [[ "${before}" == "${after}" ]] || {
    echo "CRITICAL: production container state changed during the staging operation." >&2
    return 1
  }
}

service_container_id() {
  "${COMPOSE[@]}" ps --all -q "$1"
}

service_health() {
  local id
  id="$(service_container_id "$1")"
  [[ -n "${id}" ]] || { echo "missing"; return 0; }
  docker inspect --format '{{if .State.Health}}{{.State.Health.Status}}{{else}}{{.State.Status}}{{end}}' "${id}"
}

wait_for_health() {
  local service="$1" timeout_seconds="${2:-240}" elapsed=0 status
  while (( elapsed < timeout_seconds )); do
    status="$(service_health "${service}")"
    if [[ "${status}" == "healthy" ]]; then
      return 0
    fi
    if [[ "${status}" == "unhealthy" || "${status}" == "exited" || "${status}" == "dead" ]]; then
      echo "${service} entered state ${status}." >&2
      return 1
    fi
    sleep 5
    elapsed=$((elapsed + 5))
  done
  echo "Timed out waiting for ${service}; last state: ${status:-unknown}." >&2
  return 1
}

check_backend_readiness() {
  "${COMPOSE[@]}" exec -T backend bash -ec \
    "exec 3<>/dev/tcp/127.0.0.1/10000; printf 'GET /actuator/health HTTP/1.0\\r\\nHost: localhost\\r\\nConnection: close\\r\\n\\r\\n' >&3; timeout 5 cat <&3 | grep -q '\"status\":\"UP\"'"
}

check_frontend_readiness() {
  local port
  port="$(env_value STAGING_HTTP_PORT)"
  curl --fail --silent --show-error "http://127.0.0.1:${port}/healthz" >/dev/null
}

require_image() {
  docker image inspect "$1" >/dev/null 2>&1
}

write_deployment_state() {
  local backend_tag="$1" frontend_tag="$2" backend_commit="$3" frontend_commit="$4" temp
  require_sha "${backend_tag}" && require_sha "${frontend_tag}"
  require_sha "${backend_commit}" && require_sha "${frontend_commit}"
  temp="$(mktemp "${STAGING_ROOT}/.deployment-state.tmp.XXXXXX")"
  chmod 0600 "${temp}"
  printf '%s\n' \
    "BACKEND_IMAGE_TAG=${backend_tag}" \
    "FRONTEND_IMAGE_TAG=${frontend_tag}" \
    "BACKEND_DEPLOYMENT_ID=${backend_commit}" \
    "FRONTEND_DEPLOYMENT_ID=${frontend_commit}" \
    "BACKEND_COMMIT=${backend_commit}" \
    "FRONTEND_COMMIT=${frontend_commit}" > "${temp}"
  mv -f "${temp}" "${STATE_FILE}"
}

load_and_validate_state() {
  [[ -f "${STATE_FILE}" ]] || {
    echo "Staging has not been initialized; run initialize-staging.sh first." >&2
    return 1
  }
  local key value
  for key in BACKEND_IMAGE_TAG FRONTEND_IMAGE_TAG BACKEND_COMMIT FRONTEND_COMMIT; do
    value="$(state_value "${key}")"
    require_sha "${value}" || {
      echo "Invalid ${key} in ${STATE_FILE}." >&2
      return 1
    }
  done
  refresh_compose_command
}
