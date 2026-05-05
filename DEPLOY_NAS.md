# ezTicket NAS Deployment

This project is deployed with Docker Compose using three services: MySQL, Redis,
and the Spring Boot app.

## Requirements

- Docker and Docker Compose on the NAS.
- SSH access to the NAS.
- This repository copied to a NAS folder that contains `docker-compose.yml`.

## Environment

Create `.env` from `.env.template` and set production values:

```env
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8085

MYSQL_HOST=mysql
MYSQL_PORT=3306
MYSQL_DATABASE=ezticket
MYSQL_USER=root
MYSQL_PASSWORD=your_mysql_root_password

REDIS_DATABASE=0
REDIS_HOST=redis
REDIS_PORT=6379
REDIS_PASSWORD=

EMAIL_HOST=smtp.gmail.com
EMAIL_PORT=587
EMAIL_USERNAME=your_email@example.com
EMAIL_PASSWORD=your_email_app_password

ECPAY_RETURN_URL=https://ycliang.synology.me:49636
```

The app is published on NAS port `8086` by default:

```yaml
ports:
  - "8086:${SERVER_PORT}"
```

Set the reverse proxy to forward `https://ycliang.synology.me:49636` to
`http://127.0.0.1:8086`.

## Clean Deploy With Seed Data

Use this when the NAS database can be rebuilt from `db/init.sql`.

```bash
cd /path/to/ezTicket
CONFIRM_RESET_DB=yes sh scripts/nas-clean-deploy.sh
```

`docker compose down -v` deletes the MySQL volume. This is required when you
need MySQL to run `db/init.sql` again. MySQL only runs files in
`/docker-entrypoint-initdb.d` when `/var/lib/mysql` is empty.

If the NAS uses the legacy command, replace `docker compose` with
`docker-compose`.

You can also run the commands manually:

```bash
docker compose down -v
docker compose up --build -d
docker compose ps -a
```

## Verify Deployment

Check container state:

```bash
docker compose ps -a
```

Expected state:

- `ezticket-mysql`: running and healthy
- `ezticket-redis`: running and healthy
- `ezticket-app`: running

Check app response from the NAS:

```bash
curl -I http://127.0.0.1:8086/index.html
```

Check seed data:

```bash
docker compose exec mysql sh -c 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" -e "SELECT COUNT(*) FROM information_schema.tables WHERE table_schema='\''ezticket'\'';"'
docker compose exec mysql sh -c 'mysql -uroot -p"$MYSQL_ROOT_PASSWORD" -e "SELECT COUNT(*) FROM ezticket.product; SELECT COUNT(*) FROM ezticket.activity;"'
```

Expected result: the `ezticket` schema has tables, and `product` / `activity`
have rows.

## Troubleshooting

Show logs:

```bash
sh scripts/nas-diagnose.sh
```

If app is not started:

- Confirm `mysql` and `redis` are healthy.
- Confirm `.env` uses `MYSQL_HOST=mysql` and `REDIS_HOST=redis`.
- Confirm `SERVER_PORT=8085` and the reverse proxy points to NAS port `8086`.

If MySQL has tables but no expected data:

- The old MySQL volume was reused.
- Run the clean deploy command again with `docker compose down -v`.

If the public URL does not open:

- First test from the NAS with `curl -I http://127.0.0.1:8086/index.html`.
- Then check Synology reverse proxy target host is `127.0.0.1` and port is
  `8086`.
