#!/bin/sh
curl -s \
     -X "POST" "http://localhost:8083/connectors/" \
     -H "Content-Type: application/json" \
     -d '{
    "name": "griddb-kafka-sink",
    "config": {
        "connector.class": "com.github.griddb.kafka.connect.GriddbSinkConnector",
        "name": "griddb-kafka-sink",
        "cluster.name": "myCluster",
        "user": "admin",
        "password": "admin",
        "notification.member": "griddb-server:10001",
        "container.type": "TIME_SERIES",
        "topics": "meter_0,meter_1,meter_2,meter_3",
        "transforms":  "TimestampConverter",
        "transforms.TimestampConverter.type":  "org.apache.kafka.connect.transforms.TimestampConverter$Value",
        "transforms.TimestampConverter.format":  "yyyy-MM-dd hh:mm:ss",
        "transforms.TimestampConverter.field":  "timestamp",
        "transforms.TimestampConverter.target.type":  "Timestamp"
    }
}'
