#!/usr/bin/env bash
set -euo pipefail
: "${LIGHTSAIL_HOST:?Set LIGHTSAIL_HOST to the server static IP}"
: "${SSH_KEY:?Set SSH_KEY to the Lightsail private key path}"
repo_dir=$(cd "$(dirname "$0")/../.." && pwd)
cd "$repo_dir"
./gradlew bootJar --no-daemon
jar_file=$(find build/libs -maxdepth 1 -name '*.jar' ! -name '*-plain.jar' -print)
[[ -n "$jar_file" && $(printf '%s\n' "$jar_file" | wc -l) -eq 1 ]] || { echo 'Expected one executable JAR.' >&2; exit 1; }
ssh_opts=(-i "$SSH_KEY" -o StrictHostKeyChecking=yes)
scp "${ssh_opts[@]}" "$jar_file" "ubuntu@${LIGHTSAIL_HOST}:/opt/ssok/application.jar.next"
scp "${ssh_opts[@]}" infra/lightsail/{compose.yml,Dockerfile,Caddyfile,.dockerignore} "ubuntu@${LIGHTSAIL_HOST}:/opt/ssok/"
ssh "${ssh_opts[@]}" "ubuntu@${LIGHTSAIL_HOST}" 'cd /opt/ssok && test -s .env && mv application.jar.next application.jar && sudo docker compose --profile public up -d --build --wait --wait-timeout 300 && curl -fsS http://127.0.0.1:5000/health'
