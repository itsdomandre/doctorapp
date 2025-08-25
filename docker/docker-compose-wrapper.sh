#!/bin/bash
set -e

COMPOSE_FILE="/opt/postgres/docker-compose.yml"
PROJECT_NAME="postgres"

cmd=$1

if [ "$cmd" = "up" ]; then
  sudo docker compose -f $COMPOSE_FILE -p $PROJECT_NAME up -d
elif [ "$cmd" = "down" ]; then
  sudo docker compose -f $COMPOSE_FILE -p $PROJECT_NAME down
elif [ "$cmd" = "logs" ]; then
  sudo docker compose -f $COMPOSE_FILE -p $PROJECT_NAME logs -f
elif [ "$cmd" = "ps" ]; then
  sudo docker compose -f $COMPOSE_FILE -p $PROJECT_NAME ps
elif [ "$cmd" = "exec" ]; then
  sudo docker compose -f $COMPOSE_FILE -p $PROJECT_NAME exec postgres sh
elif [ "$cmd" = "psql" ]; then
  sudo docker compose -f $COMPOSE_FILE -p $PROJECT_NAME exec postgres \
    psql -U admin -d doctorappdb
else
  echo "Uso: $0 {up|down|logs|ps|exec|psql}"
  exit 1
fi