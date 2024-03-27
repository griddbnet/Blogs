#!/bin/bash
export TERM=xterm
echo "wait 5s"
sleep 5

mvn spring-boot:run -Dspring-boot.run.jvmArguments="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005" &

while true; do
    watch -d -t -g "ls -lR . | sha1sum" && mvn compile
done