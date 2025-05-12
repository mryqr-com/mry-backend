#!/usr/bin/env bash

currentBranch=`git symbolic-ref --short HEAD`
if [ $currentBranch != "online" ]; then
  echo "Deploy failed as only online branch can be deployed but current is on branch $currentBranch.";
  exit 1
fi

currentMinute=$(date +"%M")

if [ $currentMinute -lt 30 ] ; then
echo "You can do deployment in the second half of the hour, now is in the first half, skip deploy."
exit 1
fi

echo Are sure to deploy to production?[Y/N]

read confirmed

if [ $confirmed == "Y" ] || [ $confirmed == "y" ]; then
    echo Build project...
    cp ../mry-devops/deployment/backend/application-prod2.yml src/main/resources/
    cp ../mry-devops/deployment/backend/logback-spring.xml src/main/resources/
    cp ../mry-devops/deployment/backend/fullchain.pem src/main/resources/
    cp ../mry-devops/deployment/backend/privkey.pem src/main/resources/
    ./gradlew clean assemble

    echo Upload mry-backend.jar to remote server...
    scp -i ../mry-devops/infrastructure/keys/mry-ssh-keys/id_rsa -P 20202 build/libs/mry-backend.jar mry@47.96.113.134:/home/mry
    scp -i ../mry-devops/infrastructure/keys/mry-ssh-keys/id_rsa -P 20202 ../mry-devops/deployment/backend/run-mry-backend.sh mry@47.96.113.134:/home/mry

    ssh mry@47.96.113.134 -i ../mry-devops/infrastructure/keys/mry-ssh-keys/id_rsa -p 20202 'sudo ./run-mry-backend.sh'
else
    echo You answered no, exit.
    exit 1
fi