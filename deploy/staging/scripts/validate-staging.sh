#!/usr/bin/env bash
set -euo pipefail

source "$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)/staging-common.sh"

assert_safe_layout
command -v docker >/dev/null
docker info >/dev/null

[[ "$(env_value COMPOSE_PROJECT_NAME)" == "projectum-staging" ]] || {
  echo "COMPOSE_PROJECT_NAME must be projectum-staging." >&2
  exit 1
}
[[ "$(env_value STAGING_PUBLIC_URL)" == "https://staging.projectumall.com" ]] || {
  echo "STAGING_PUBLIC_URL must be https://staging.projectumall.com." >&2
  exit 1
}
[[ "$(env_value STAGING_HTTP_PORT)" == "18080" ]] || {
  echo "STAGING_HTTP_PORT must be 18080." >&2
  exit 1
}

for secret in postgres_password jwt_secret demo_password; do
  path="${STAGING_ROOT}/secrets/${secret}"
  [[ -s "${path}" ]] || { echo "Missing or empty secret: ${path}" >&2; exit 1; }
  [[ "$(stat -c '%a' "${path}")" == "600" ]] || {
    echo "Secret ${path} must have mode 0600." >&2
    exit 1
  }
done

refresh_compose_command
rendered="$(mktemp)"
trap 'rm -f "${rendered}"' EXIT
"${COMPOSE[@]}" config > "${rendered}"

grep -q '^name: projectum-staging$' "${rendered}"
grep -q 'host_ip: 127.0.0.1' "${rendered}"
grep -q 'published: "18080"' "${rendered}"
grep -q 'name: projectum-staging_postgres-data' "${rendered}"
grep -q 'name: projectum-staging_staging-internal' "${rendered}"
grep -q 'internal: true' "${rendered}"
grep -q 'source: /opt/projectum-staging/frontend/nginx.staging.conf' "${rendered}"
grep -q 'context: /opt/projectum-staging/backend' "${rendered}"
grep -q 'context: /opt/projectum-staging/frontend' "${rendered}"
grep -q 'target: spring.datasource.password' "${rendered}"
grep -q 'target: jwt.secret' "${rendered}"
grep -q 'POSTGRES_PASSWORD_FILE: /run/secrets/db_password' "${rendered}"
grep -q 'SPRING_CONFIG_IMPORT: configtree:/run/secrets/' "${rendered}"
grep -q 'file: /opt/projectum-staging/secrets/postgres_password' "${rendered}"
grep -q 'file: /opt/projectum-staging/secrets/jwt_secret' "${rendered}"
grep -q 'file: /opt/projectum-staging/secrets/demo_password' "${rendered}"

if grep -Eq 'published: "?(80|443|5432|10000)"?$' "${rendered}"; then
  echo "Unsafe production or internal service port detected." >&2
  exit 1
fi
if grep -q '/opt/projectum/' "${rendered}"; then
  echo "Production path detected in rendered staging configuration." >&2
  exit 1
fi
if grep -Eq 'name: projectum_(db-data|postgres-data|uploads|backend-logs|default)' "${rendered}"; then
  echo "Production resource name detected in staging configuration." >&2
  exit 1
fi
if awk '
    /^  db:$/ { in_db=1; next }
    in_db && /^  [[:alnum:]_-]+:$/ { exit }
    in_db { print }
  ' "${rendered}" | grep -q 'published:'; then
  echo "The staging database must not publish a host port." >&2
  exit 1
fi

production_snapshot >/dev/null
echo "Staging configuration, secrets, paths, resource names, and production isolation checks passed."
echo "No containers or Docker resources were changed."
