#!/usr/bin/env bash
# Only replace the app. Never stop, recreate, or reset databases.
set -euo pipefail
umask 077
ROOT=$(cd "$(dirname "$0")/.." && pwd)
DOCKER=${EZTICKET_DOCKER:-/var/packages/ContainerManager/target/usr/bin/docker}
CHANNEL=${EZTICKET_CHANNEL:-ghcr.io/liang0731/ezticket:auto-deploy}
PROJECT=${EZTICKET_COMPOSE_PROJECT:?Set the project label from the existing ezticket-app}
STATE="$ROOT/.deploy-state"
mkdir -p "$STATE"
if ! mkdir "$STATE/lock" 2>/dev/null; then
  echo 'Another deployment may be running; no changes made.' >&2
  exit 1
fi
trap 'rmdir "$STATE/lock"' EXIT
[[ -f "$ROOT/.env" ]] || { echo 'Existing NAS .env required.' >&2; exit 1; }
actual=$("$DOCKER" inspect --format '{{ index .Config.Labels "com.docker.compose.project" }}' ezticket-app)
[[ "$actual" == "$PROJECT" ]] || { echo 'Compose project mismatch.' >&2; exit 1; }
for dependency in ezticket-mysql ezticket-redis; do
  [[ $("$DOCKER" inspect --format '{{.State.Running}}' "$dependency") == true ]] || {
    echo "$dependency is not running; refusing update." >&2; exit 1;
  }
done
previous=$("$DOCKER" inspect --format '{{.Image}}' ezticket-app)
"$DOCKER" pull "$CHANNEL"
candidate=$("$DOCKER" image inspect --format '{{.Id}}' "$CHANNEL")
[[ "$candidate" =~ ^sha256:[0-9a-f]{64}$ && "$previous" =~ ^sha256:[0-9a-f]{64}$ ]] || {
  echo 'Invalid image identity.' >&2; exit 1;
}
if [[ "$candidate" == "$previous" ]]; then
  echo 'App unchanged; no containers recreated.'
  exit 0
fi
compose_app() {
  EZTICKET_IMAGE="$1" "$DOCKER" compose --project-name "$PROJECT" \
    --project-directory "$ROOT" --env-file "$ROOT/.env" \
    -f "$ROOT/docker-compose.pull.yml" up -d --no-deps app
}
healthy() {
  local attempt
  for attempt in $(seq 1 "${EZTICKET_HEALTH_ATTEMPTS:-60}"); do
    if curl --connect-timeout 2 --max-time 5 -fsS -I http://127.0.0.1:8086/index.html >/dev/null 2>&1; then
      return 0
    fi
    sleep "${EZTICKET_HEALTH_INTERVAL:-5}"
  done
  return 1
}
printf '%s\n' "$previous" > "$STATE/previous-image"
if compose_app "$candidate" && healthy; then
  printf '%s\n' "$candidate" > "$STATE/current-image"
  echo 'App updated and HTTP check passed; databases unchanged.'
else
  echo 'Update failed; restoring previous application image.' >&2
  if compose_app "$previous" && healthy; then
    echo 'Previous app image restored.' >&2
  else
    echo 'Rollback not verified; manual attention required.' >&2
  fi
  exit 1
fi
