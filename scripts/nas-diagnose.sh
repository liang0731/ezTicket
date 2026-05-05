#!/usr/bin/env sh
set -eu

if command -v docker >/dev/null 2>&1 && docker compose version >/dev/null 2>&1; then
  COMPOSE="docker compose"
elif command -v docker-compose >/dev/null 2>&1; then
  COMPOSE="docker-compose"
else
  echo "Docker Compose is not available."
  exit 1
fi

echo "== Compose config status =="
$COMPOSE config >/dev/null && echo "compose config: OK"

echo
echo "== Containers =="
$COMPOSE ps -a

echo
echo "== Local app HTTP =="
if curl -fsS -I http://127.0.0.1:8086/index.html; then
  echo "local app: OK"
else
  echo "local app: FAILED"
fi

echo
echo "== Seed data counts =="
$COMPOSE exec mysql sh -c 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" -e "SELECT COUNT(*) AS tables_count FROM information_schema.tables WHERE table_schema='\''ezticket'\''; SELECT COUNT(*) AS product_count FROM ezticket.product; SELECT COUNT(*) AS activity_count FROM ezticket.activity;"' || true

echo
echo "== MySQL logs =="
$COMPOSE logs --tail=120 mysql

echo
echo "== Redis logs =="
$COMPOSE logs --tail=120 redis

echo
echo "== App logs =="
$COMPOSE logs --tail=160 app
