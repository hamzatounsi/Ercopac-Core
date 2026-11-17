#!/usr/bin/env bash
set -euo pipefail

readonly staging_root="/opt/projectum-staging"
readonly script_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

if [[ ! -d "${staging_root}/backend/.git" || ! -d "${staging_root}/frontend/.git" ]]; then
  echo "Refusing to continue: staging repositories must exist below ${staging_root}." >&2
  exit 1
fi
if [[ "${script_root}" != "${staging_root}/backend/deploy/staging" ]]; then
  echo "Refusing to continue outside the dedicated staging tree." >&2
  exit 1
fi

umask 077
install -d -m 0700 "${staging_root}/secrets"

if [[ ! -s "${staging_root}/secrets/postgres_password" ]]; then
  openssl rand -base64 36 | tr -d '\n' > "${staging_root}/secrets/postgres_password"
fi
if [[ ! -s "${staging_root}/secrets/jwt_secret" ]]; then
  openssl rand -base64 64 | tr -d '\n' > "${staging_root}/secrets/jwt_secret"
fi
if [[ ! -s "${staging_root}/secrets/demo_password" ]]; then
  openssl rand -base64 30 | tr -d '\n' > "${staging_root}/secrets/demo_password"
fi

chmod 0600 "${staging_root}/secrets/postgres_password" \
  "${staging_root}/secrets/jwt_secret" \
  "${staging_root}/secrets/demo_password"

if [[ ! -e "${staging_root}/.env" ]]; then
  install -m 0600 "${script_root}/.env.example" "${staging_root}/.env"
  echo "Created ${staging_root}/.env for staging.projectumall.com. Review it before startup."
else
  echo "Kept existing ${staging_root}/.env unchanged."
fi

echo "Staging secrets are ready. No containers were started."
