#!/bin/bash

docker rm $(docker ps -a -q)

docker rmi $(docker images -q -f dangling=true)

docker volume rm `docker volume ls -q -f dangling=true`

docker network rm $(docker network ls | grep "bridge" | awk '/ / { print $1 }')
