#!/usr/bin/env bash
#
# capture-openapi.sh
#
# Boots the LIS application against throwaway Postgres + Redis containers,
# captures the generated OpenAPI document to <project-root>/openapi.json, then
# shuts the application and containers down.
#
# Kafka is not required: the OpenAPI document is produced by the HTTP server,
# and Kafka Streams auto-startup is disabled for this run.
#
# Usage:  ./scripts/capture-openapi.sh
# Env overrides: PG_PORT, REDIS_PORT, SERVER_PORT, STARTUP_TIMEOUT
#
set -euo pipefail

PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

PG_CONTAINER="lis-openapi-pg"
REDIS_CONTAINER="lis-openapi-redis"
PG_PORT="${PG_PORT:-55432}"
REDIS_PORT="${REDIS_PORT:-6380}"
SERVER_PORT="${SERVER_PORT:-8089}"
STARTUP_TIMEOUT="${STARTUP_TIMEOUT:-180}"

DB_NAME="lis_db"
DB_USER="did_user"
DB_PASSWORD="did_password"
REDIS_PASSWORD="redis_password"

OUTPUT="${PROJECT_ROOT}/openapi.json"
API_DOCS_URL="http://localhost:${SERVER_PORT}/v3/api-docs"
MARKER="lis-openapi-capture-$$"        # unique tag to target the forked JVM on cleanup
APP_LOG="$(mktemp)"
GRADLE_PID=""

log() { printf '\033[1;34m[capture]\033[0m %s\n' "$*"; }
err() { printf '\033[1;31m[capture]\033[0m %s\n' "$*" >&2; }

cleanup() {
    local code=$?
    log "Shutting down…"
    pkill -f "$MARKER" 2>/dev/null || true
    [ -n "$GRADLE_PID" ] && kill "$GRADLE_PID" 2>/dev/null || true
    docker rm -f "$PG_CONTAINER" "$REDIS_CONTAINER" >/dev/null 2>&1 || true
    [ -f "$APP_LOG" ] && rm -f "$APP_LOG" || true
    exit "$code"
}
trap cleanup EXIT INT TERM

require() { command -v "$1" >/dev/null 2>&1 || { err "'$1' is required but not installed"; exit 1; }; }
require docker
require curl

# --- throwaway infra --------------------------------------------------------
log "Starting throwaway Postgres (:${PG_PORT}) and Redis (:${REDIS_PORT})…"
docker rm -f "$PG_CONTAINER" "$REDIS_CONTAINER" >/dev/null 2>&1 || true
docker run -d --rm --name "$PG_CONTAINER" \
    -e POSTGRES_USER="$DB_USER" -e POSTGRES_PASSWORD="$DB_PASSWORD" -e POSTGRES_DB="$DB_NAME" \
    -p "${PG_PORT}:5432" postgres:16 >/dev/null
docker run -d --rm --name "$REDIS_CONTAINER" \
    -p "${REDIS_PORT}:6379" redis:7 redis-server --requirepass "$REDIS_PASSWORD" >/dev/null

log "Waiting for Postgres to accept connections…"
for _ in $(seq 1 60); do
    docker exec "$PG_CONTAINER" pg_isready -U "$DB_USER" -d "$DB_NAME" >/dev/null 2>&1 && break
    sleep 1
done
docker exec "$PG_CONTAINER" pg_isready -U "$DB_USER" -d "$DB_NAME" >/dev/null 2>&1 \
    || { err "Postgres did not become ready in time"; exit 1; }

# --- application ------------------------------------------------------------
log "Starting application (profile=dev)…"
cd "$PROJECT_ROOT"
./gradlew bootRun \
    --args="--spring.profiles.active=dev \
        --server.port=${SERVER_PORT} \
        --spring.r2dbc.url=r2dbc:postgresql://localhost:${PG_PORT}/${DB_NAME} \
        --spring.flyway.url=jdbc:postgresql://localhost:${PG_PORT}/${DB_NAME} \
        --spring.data.redis.host=localhost \
        --spring.data.redis.port=${REDIS_PORT} \
        --spring.data.redis.password=${REDIS_PASSWORD} \
        --spring.kafka.streams.auto-startup=false \
        --logging.level.org.apache.kafka=ERROR \
        --logging.level.org.springframework.kafka=ERROR \
        --openapi.capture.marker=${MARKER}" \
    > "$APP_LOG" 2>&1 &
GRADLE_PID=$!

log "Waiting for OpenAPI endpoint ${API_DOCS_URL} (timeout ${STARTUP_TIMEOUT}s)…"
ready=0
for _ in $(seq 1 "$STARTUP_TIMEOUT"); do
    if curl -sf -o /dev/null "$API_DOCS_URL" 2>/dev/null; then ready=1; break; fi
    if grep -q "APPLICATION FAILED TO START" "$APP_LOG" 2>/dev/null; then
        err "Application failed to start:"; tail -n 40 "$APP_LOG" >&2; exit 1
    fi
    if ! kill -0 "$GRADLE_PID" 2>/dev/null; then
        err "Application process exited unexpectedly:"; tail -n 40 "$APP_LOG" >&2; exit 1
    fi
    sleep 1
done
[ "$ready" -eq 1 ] || { err "Timed out waiting for the OpenAPI endpoint"; tail -n 40 "$APP_LOG" >&2; exit 1; }

# --- capture ----------------------------------------------------------------
log "Capturing OpenAPI document…"
TMP_JSON="$(mktemp)"
curl -sf "$API_DOCS_URL" -o "$TMP_JSON" || { err "Failed to fetch OpenAPI document"; rm -f "$TMP_JSON"; exit 1; }

if command -v python3 >/dev/null 2>&1; then
    python3 -m json.tool "$TMP_JSON" "$OUTPUT" || { err "Captured document is not valid JSON"; rm -f "$TMP_JSON"; exit 1; }
elif command -v jq >/dev/null 2>&1; then
    jq . "$TMP_JSON" > "$OUTPUT" || { err "Captured document is not valid JSON"; rm -f "$TMP_JSON"; exit 1; }
else
    cp "$TMP_JSON" "$OUTPUT"
fi
rm -f "$TMP_JSON"

log "✓ Wrote ${OUTPUT} ($(wc -c < "$OUTPUT" | tr -d ' ') bytes)"
