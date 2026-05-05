# === Step 1: Build jar ===
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# === Step 2: Create runtime container ===
FROM eclipse-temurin:17-jdk-alpine
VOLUME /tmp
WORKDIR /app
RUN apk add --no-cache netcat-openbsd
COPY --from=builder /app/target/ezticket.jar app.jar
ENTRYPOINT ["sh", "-c", "until nc -z ${MYSQL_HOST:-mysql} ${MYSQL_PORT:-3306}; do echo \"Waiting for MySQL at ${MYSQL_HOST:-mysql}:${MYSQL_PORT:-3306}\"; sleep 5; done; exec java -jar app.jar"]
