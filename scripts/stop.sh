#!/usr/bin/env bash

ABSPATH=$(readlink -f $0)
ABSDIR=$(dirname $ABSPATH)
source ${ABSDIR}/profile.sh # import

IDLE_PROFILE=$(find_idle_profile)
IDLE_PORT=$(find_idle_port)

echo "> $IDLE_PORT 에서 구동 중인 애플리케이션 pid 확인"
IDLE_PID=$(ps -ef | grep java | grep ${IDLE_PROFILE} | awk '{print $2}')
# $(lsof -ti tcp:${IDLE_PORT})
echo "> IDLE_PID = $IDLE_PID"

if [ -z ${IDLE_PID} ]
then
  echo "> 현재 구동 중인 애플리케이션이 없으므로 종료하지 않습니다."
else
  echo "> sudo kill -15 $IDLE_PID"
  sudo kill -15 ${IDLE_PID}
  sleep 5
fi
