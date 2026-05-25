#!/usr/bin/env bash

echo "Start build..."

./start-docker-compose.sh
./mvnw clean package
