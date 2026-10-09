#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
if [[ ! -f .env ]]; then
  echo 'Create infra/ec2/.env from .env.example first.' >&2
  exit 1
fi
chmod 600 .env
if [[ $# -gt 0 ]]; then
  printf 'APP_IMAGE=%s\n' "$1" > .deploy.env
fi
compose_args=(--env-file .env)
[[ ! -f .deploy.env ]] || compose_args+=(--env-file .deploy.env)
docker compose "${compose_args[@]}" config --quiet
docker compose "${compose_args[@]}" pull app
docker compose "${compose_args[@]}" up -d --wait --wait-timeout 240
docker compose ps
