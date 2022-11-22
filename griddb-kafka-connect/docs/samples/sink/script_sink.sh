#!/bin/bash

function echo_payload {
    echo '{ "payload": {  "datetime": "'$1 $2'",  "sensor": "'$3'",  "translate01": "'$4'",  "translate02": "'$5'",  "message": "'$6'",  "sensoractivity": "'$7'" }, "schema": {  "fields": [   {    "field": "datetime",    "optional": false,    "type": "string"   },   {    "field": "sensor",    "optional": false,    "type": "string"   },   {    "field": "translate01",    "optional": false,    "type": "string"   },   {    "field": "translate02",    "optional": false,    "type": "string"   },   {    "field": "message",    "optional": false,    "type": "string"   },   {    "field": "sensoractivity",    "optional": false,    "type": "string"   }   ],  "name": "sample",  "optional": false,  "type": "struct" }}'

}

TOPICS=()

for file in `find $1 -name \*rawdata.txt` ; do
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