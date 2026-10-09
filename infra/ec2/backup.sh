#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
: "${BACKUP_S3_URI:?Set BACKUP_S3_URI to s3://bucket/prefix}"
umask 077
backup_file=$(mktemp)
trap 'rm -f "$backup_file"' EXIT
docker compose exec -T mysql sh -c 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysqldump -u root --single-transaction --no-tablespaces --set-gtid-purged=OFF --routines --triggers app_db' | gzip > "$backup_file"
aws s3 cp "$backup_file" "${BACKUP_S3_URI%/}/app_db-$(date -u +%Y%m%dT%H%M%SZ).sql.gz" --only-show-errors
