#!/usr/bin/env bash
# Existing EB instance only: no cloud resource creation, no open security-group ports.
set -euo pipefail

printf 'Checking capacity for local RabbitMQ\n'
mem_total_kb=$(awk '/^MemTotal:/ {print $2}' /proc/meminfo)
mem_available_kb=$(awk '/^MemAvailable:/ {print $2}' /proc/meminfo)
disk_available_kb=$(df -Pk /var | awk 'NR==2 {print $4}')
printf 'Memory total=%s KiB available=%s KiB; disk available=%s KiB\n' "$mem_total_kb" "$mem_available_kb" "$disk_available_kb"
if (( mem_total_kb < 900000 || disk_available_kb < 3145728 )); then
    echo 'Insufficient capacity: refusing RabbitMQ installation. No instance resize is performed.' >&2
    exit 1
fi

# On repeat deploys the existing broker already occupies its memory budget.
if ! command -v docker >/dev/null || ! docker inspect ssok-rabbitmq >/dev/null 2>&1; then
    if (( mem_available_kb < 393216 )); then
        echo 'Need at least 384 MiB available before first RabbitMQ start. Deployment stopped.' >&2
        exit 1
    fi
fi

if ! command -v docker >/dev/null; then
    dnf install -y docker
fi
systemctl enable --now docker
install -d -m 0750 -o root -g webapp /etc/ssok

if [[ ! -f /etc/ssok/rabbitmq.env ]]; then
    umask 077
    broker_password=$(openssl rand -hex 32)
    printf 'RABBITMQ_DEFAULT_USER=ssok_backend\nRABBITMQ_DEFAULT_PASS=%s\n' "$broker_password" > /etc/ssok/rabbitmq.env
fi
# Existing password survives deployments. Never print it to build or application logs.
broker_password=$(sed -n 's/^RABBITMQ_DEFAULT_PASS=//p' /etc/ssok/rabbitmq.env)
if [[ -z "$broker_password" ]]; then echo 'Missing local broker credential' >&2; exit 1; fi

cat > /etc/ssok/rabbitmq.conf <<'CONF'
listeners.tcp.default = 5672
vm_memory_high_watermark.absolute = 128MB
disk_free_limit.absolute = 256MB
CONF
chmod 0644 /etc/ssok/rabbitmq.conf

if ! docker inspect ssok-rabbitmq >/dev/null 2>&1; then
    docker pull rabbitmq:4.2-alpine@sha256:3d287d0b71631c0230d344c2bc1ea032844ad7f871a681b60234ff258ac1288b
    docker run -d --name ssok-rabbitmq --hostname ssok-rabbitmq \
        --restart unless-stopped --memory 256m --memory-swap 256m --cpus 0.5 \
        --log-opt max-size=5m --log-opt max-file=2 \
        -p 127.0.0.1:5672:5672 \
        --env-file /etc/ssok/rabbitmq.env \
        -e 'RABBITMQ_SERVER_ADDITIONAL_ERL_ARGS=+S 2:2 +A 4' \
        -v ssok-rabbitmq-data:/var/lib/rabbitmq \
        -v /etc/ssok/rabbitmq.conf:/etc/rabbitmq/rabbitmq.conf:ro \
        rabbitmq:4.2-alpine@sha256:3d287d0b71631c0230d344c2bc1ea032844ad7f871a681b60234ff258ac1288b
else
    docker start ssok-rabbitmq >/dev/null
fi

ready=false
for attempt in $(seq 1 30); do
    if docker exec ssok-rabbitmq rabbitmq-diagnostics -q ping >/dev/null 2>&1; then
        ready=true
        break
    fi
    sleep 2
done
if [[ "$ready" != true ]]; then
    echo 'RabbitMQ did not become ready. Application configuration was not enabled.' >&2
    exit 1
fi

# The Procfile loads this local file. No broker password is committed or copied to EB UI.
umask 027
cat > /etc/ssok/rabbitmq.properties.tmp <<CONF
app.rabbitmq.enabled=true
spring.rabbitmq.host=127.0.0.1
spring.rabbitmq.port=5672
spring.rabbitmq.username=ssok_backend
spring.rabbitmq.password=$broker_password
spring.rabbitmq.virtual-host=/
spring.rabbitmq.ssl.enabled=false
CONF
chown root:webapp /etc/ssok/rabbitmq.properties.tmp
chmod 0640 /etc/ssok/rabbitmq.properties.tmp
mv /etc/ssok/rabbitmq.properties.tmp /etc/ssok/rabbitmq.properties
unset broker_password
printf 'Local RabbitMQ ready; external ports unchanged.\n'
