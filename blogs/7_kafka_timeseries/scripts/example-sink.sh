#!/bin/sh
curl -s \
     -X "POST" "http://localhost:8083/connectors/" \
     -H "Content-Type: application/json" \
     -d '{
    "name": "example-sink",
    "config": {
        "connector.class": "com.github.griddb.kafka.connect.GriddbSinkConnector",
        "name": "example-sink",
        "cluster.name": "myCluster",
        "user": "admin",
        "password": "admin",
        "notification.member": "griddb-server:10001",
        "container.type": "TIME_SERIES",
        "topics": "topic_D001,topic_D002,topic_BATP102,topic_BATP103"
    }
}'
