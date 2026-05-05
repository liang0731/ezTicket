#!/usr/bin/env sh
set -eu

if [ "${CONFIRM_RESET_DB:-}" != "yes" ]; then
  echo "This will delete the MySQL Docker volume and rerun db/init.sql."
  echo "Run with: CONFIRM_RESET_DB=yes sh scripts/nas-clean-deploy.sh"
  exit 1
fi

if command -v docker >/dev/null 2>&1 && docker compose version >/dev/null 2>&1; then
  COMPOSE="docker compose"
elif command -v docker-compose >/dev/null 2>&1; then
  COMPOSE="docker-compose"
else
  echo "Docker Compose is not available."
  exit 1
fi

echo "Stopping stack and deleting volumes..."
$COMPOSE down -v

echo "Removing stale project containers..."
for name in ezticket-app ezticket-mysql ezticket-redis; do
  docker rm -f "$name" >/dev/null 2>&1 || true
done

echo "Building and starting stack..."
$COMPOSE up --build -d

echo "Current container state:"
$COMPOSE ps -a

echo "Waiting for app HTTP endpoint..."
for i in $(seq 1 180); do
  if command -v curl >/dev/null 2>&1 && curl -fsS -I http://127.0.0.1:8086/index.html >/dev/null 2>&1; then
    echo "App is responding on http://127.0.0.1:8086/index.html"
    exit 0
  fi
  if command -v wget >/dev/null 2>&1 && wget -q --spider http://127.0.0.1:8086/index.html >/dev/null 2>&1; then
    echo "App is responding on http://127.0.0.1:8086/index.html"
    exit 0
  fi
  if [ $((i % 12)) -eq 0 ]; then
    echo "Still waiting for app endpoint... ($((i * 5)) seconds)"
    $COMPOSE ps -a || true
  fi
  sleep 5
done

echo "App did not respond within 900 seconds. Diagnostics:"
sh scripts/nas-diagnose.sh || true
exit 1
