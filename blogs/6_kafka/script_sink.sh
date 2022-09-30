#!/bin/bash

function echo_payload {
    echo '{"payload": {"ts": "'$1 $2'","co": "'$4'","humidity": "'$5'","light": "'$6'","lpg": "'$7'","motion": "'$8'","smoke": "'$9'","temp": "'{$10}'"},"schema": {"fields": [{"field": "ts","optional": false,"type": "string"},{"field": "co","optional": false,"type": "string"},{"field": "humidity","optional": false,"type": "string"},{"field": "light","optional": false,"type": "string"},{"field": "lpg","optional": false,"type": "string"},{"field": "motion","optional": false,"type": "string"},{"field": "smoke","optional": false,"type": "string"},{"field": "temp","optional": false,"type": "string"}],"name": "iot","optional": false,"type": "struct"}}'
}

TOPICS=()

for file in `find $1 -name \*simulate_sensor.txt` ; do
    echo $file
    head -10 $file |while read -r line ; do
        SENSOR=`echo ${line} | awk '{ print $3 }'`
        if [[ ! " ${TOPICS[@]} " =~ " ${SENSOR} " ]]; then
            echo Creating topic ${SENSOR}
            kafka-topics.sh --bootstrap-server 127.0.0.1:9092 --create --topic  ${SENSOR} 2&>1 /dev/null
            TOPICS+=(${SENSOR})
        fi
        echo_payload ${line} | kafka-console-producer.sh --topic ${SENSOR} --bootstrap-server localhost:9092
    done
done
