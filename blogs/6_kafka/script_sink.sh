#!/bin/bash

function echo_payload {
    echo '{"payload":{"ts":"'$1$2'","co":"'$3'","humidity":"'$4'","light":"'$5'","lpg":"'$6'","motion":"'$7'","smoke":"'$7'","temp":"'$7'"},"schema":{"fields":[{"field":"ts","optional":false,"type":"string"},{"field":"co","optional":false,"type":"FLOAT64"},{"field":"humidity","optional":false,"type":"FLOAT64"},{"field":"light","optional":false,"type":"BOOLEAN"},{"field":"lpg","optional":false,"type":"FLOAT64"},{"field":"motion","optional":false,"type":"BOOLEAN"}{"field":"smoke","optional":false,"type":"FLOAT64"},{"field":"temp","optional":false,"type":"FLOAT64"}],"name":"iot","optional":false,"type":"struct"}}'

}

TOPICS=()

for file in `find $1 -name \*simulate_sensor.txt` ; do
    echo $file
    LOCATION="topic"
    head -10 $file |while read -r line ; do
        SENSOR=`echo ${line} | awk '{ print $3 }'`
        if [[ ! " ${TOPICS[@]} " =~ " ${LOCATION}_${SENSOR} " ]]; then
            echo Creating topic ${LOCATION}_${SENSOR}
            kafka-topics.sh --bootstrap-server 127.0.0.1:9092 --create --topic  ${LOCATION}_${SENSOR} 2&>1 /dev/null
            TOPICS+=(${LOCATION}_${SENSOR})
        fi
        echo_payload ${line} | kafka-console-producer.sh --topic ${LOCATION}_${SENSOR} --bootstrap-server localhost:9092
    done
done
