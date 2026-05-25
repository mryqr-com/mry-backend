#!/usr/bin/env bash

echo "Start build..."

#./gradlew composeDown

./start-docker-compose.sh
./gradlew clean build
