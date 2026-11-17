#!/usr/bin/env bash
# Runs the OpenJar backend directly on this machine (no Docker images needed).
# Requirements: Java 17, Maven, MySQL on localhost:3306, RabbitMQ on localhost:5672.
#   Quick RabbitMQ:  docker run -d --name openjar-rabbit -p 5672:5672 -p 15672:15672 \
#                      -e RABBITMQ_DEFAULT_USER=$RABBITMQ_USER -e RABBITMQ_DEFAULT_PASS=$RABBITMQ_PASS rabbitmq:3-management
# Usage:  ./run-local.sh            start (builds jars the first time)
#         ./run-local.sh --build    rebuild all jars first
#         ./run-local.sh stop       stop everything started by this script
set -euo pipefail
cd "$(dirname "$0")"

SERVICES=(user-service recipe-service social-service notification-service api-gateway)
mkdir -p logs

if [[ "${1:-}" == "stop" ]]; then
  for f in logs/*.pid; do [[ -f "$f" ]] && kill "$(cat "$f")" 2>/dev/null && echo "stopped $(basename "$f" .pid)"; rm -f "$f"; done
  exit 0
fi

[[ -f .env ]] || { echo "Missing .env - copy .env.example to .env and fill it in."; exit 1; }
set -a; source .env; set +a

export SPRING_DATASOURCE_USERNAME="${DB_USER:-root}"
export SPRING_DATASOURCE_PASSWORD="$MYSQL_ROOT_PASSWORD"
export SPRING_RABBITMQ_USERNAME="$RABBITMQ_USER"
export SPRING_RABBITMQ_PASSWORD="$RABBITMQ_PASS"
export AUTH_INTERNAL_URL="${AUTH_INTERNAL_URL:-http://localhost:8086}"

jar_of() { ls "$1"/target/*-SNAPSHOT.jar 2>/dev/null | grep -v original | head -1; }

for s in eureka "${SERVICES[@]}"; do
  if [[ "${1:-}" == "--build" || -z "$(jar_of "$s")" ]]; then
    echo "building $s ..."; (cd "$s" && mvn -q -B package -DskipTests)
  fi
done

# Small, dev-friendly JVMs: 6 services at default heap sizes can exhaust a laptop's RAM.
# Override with e.g.  JAVA_OPTS="-Xmx512m" ./run-local.sh
JAVA_OPTS="${JAVA_OPTS:--Xms64m -Xmx256m -XX:+UseSerialGC -XX:MaxMetaspaceSize=160m -Xss512k -XX:TieredStopAtLevel=1}"

start() {
  echo "starting $1 (log: logs/$1.log)"
  # shellcheck disable=SC2086
  java $JAVA_OPTS -jar "$(jar_of "$1")" > "logs/$1.log" 2>&1 &
  echo $! > "logs/$1.pid"
}

start eureka
until curl -sf http://localhost:8761/actuator/health >/dev/null; do sleep 1; done
for s in "${SERVICES[@]}"; do start "$s"; done

echo
echo "Waiting for the gateway on http://localhost:8081 ..."
until curl -s -o /dev/null http://localhost:8081/api/csrf; do sleep 2; done
echo "Backend is up. Services register with Eureka within ~30s."
echo "Now start the frontend:  cd ../OpenJar-Frontend && npm run dev   ->  http://localhost:5173"
