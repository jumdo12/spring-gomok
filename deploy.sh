#!/bin/bash

BLUE="gomok-server-blue"
GREEN="gomok-server-green"
UPSTREAM_FILE="./caddy_upstream"

CURRENT=$(cat $UPSTREAM_FILE)

if [[ $CURRENT == *"blue"* ]]; then
    NEXT=$GREEN
    NEXT_NAME="green"
else
    NEXT=$BLUE
    NEXT_NAME="blue"
fi

echo "현재: $CURRENT → 배포 대상: $NEXT"

docker-compose pull
docker-compose up -d app-$NEXT_NAME

echo "헬스체크 대기..."
for i in {1..30}; do
    STATUS=$(docker exec $NEXT curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/actuator/health)
    if [ "$STATUS" == "200" ]; then
        echo "헬스체크 통과"
        break
    fi
    echo "대기 중... ($i/30)"
    sleep 2
done

if [ "$STATUS" != "200" ]; then
    echo "헬스체크 실패 - 롤백"
    docker-compose stop app-$NEXT_NAME
    exit 1
fi

echo "$NEXT:8080" > $UPSTREAM_FILE
docker exec caddy caddy reload --config /etc/caddy/Caddyfile

echo "이전 컨테이너 종료"
if [[ $NEXT == *"blue"* ]]; then
    docker-compose stop app-green
else
    docker-compose stop app-blue
fi

echo "배포 완료"
