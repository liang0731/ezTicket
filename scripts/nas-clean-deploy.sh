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

echo "Building and starting stack..."
$COMPOSE up --build -d

echo "Current container state:"
$COMPOSE ps -a

echo "Waiting for app HTTP endpoint..."
for i in $(seq 1 30); do
  if command -v curl >/dev/null 2>&1 && curl -fsS -I http://127.0.0.1:8086/index.html >/dev/null 2>&1; then
    echo "App is responding on http://127.0.0.1:8086/index.html"
    exit 0
  fi
  if command -v wget >/dev/null 2>&1 && wget -q --spider http://127.0.0.1:8086/index.html >/dev/null 2>&1; then
    echo "App is responding on http://127.0.0.1:8086/index.html"
    exit 0
  fi
  sleep 5
done

echo "App did not respond within 150 seconds. Run: sh scripts/nas-diagnose.sh"
exit 1
