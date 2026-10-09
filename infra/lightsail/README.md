# Lightsail development deployment

Target: `ssok-dev`, Seoul, Ubuntu 24.04, 2 GB RAM / 60 GB SSD, $12/month before tax and optional charges.

This directory runs the Java API, MySQL 8.4, Redis and Caddy on one server. MySQL and Redis are private Docker services. The API binds to localhost; Caddy exposes HTTPS after DNS cutover. Persistent Docker volumes survive container replacement. Do not run `docker compose down -v` unless intentionally deleting development data.

## Current migration state (2026-09-22)

- Server created; Docker and 2 GB swap installed.
- App artifact and environment deployed to `/opt/ssok`.
- App, MySQL and Redis passed container health checks.
- Static IPv4 `15.164.34.219` (`ssok-dev-ip`) is attached; TCP 443 is allowed for the existing public API.
- `dev.ssok.store` now points to `15.164.34.219` (A, TTL 300). Caddy is running with a valid HTTPS certificate.
- Public `/health` and Swagger return HTTP 200 from the new IP. Kakao login initiation returns HTTP 302 with the existing HTTPS callback. Full user login and AI processing have not been exercised.
- Latest deployed source: `fe8f641` (2026-09-22).
- Existing Beanstalk and RDS await action-time permanent deletion approval. Costs still overlap until cleanup.
- Existing GitHub Actions workflow still targets Beanstalk. It must be replaced/disabled when migration is complete; this local helper does not change GitHub repository settings.

## Re-deployment

Build runs locally to preserve server memory. Configure the server host and private SSH key:

```sh
LIGHTSAIL_HOST=15.164.34.219 SSH_KEY=<private-key-path> ./infra/lightsail/deploy.sh
```

The SSH host key must already be verified and present in the local known_hosts file. The helper preserves the server `.env` and persistent data volumes. Secrets live only in `/opt/ssok/.env` (mode 600), never in source control.

## Operations

On the server, from `/opt/ssok`:

```sh
sudo docker compose ps
sudo docker compose logs --tail 100 app
curl -fsS http://127.0.0.1:5000/health
sudo docker compose --profile public up -d --wait
```

The new DB was initialized empty as authorized. JWT keys were regenerated, so old login sessions do not carry over. Kakao redirect URI and frontend origin remain unchanged.
