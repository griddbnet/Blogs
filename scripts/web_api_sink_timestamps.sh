#!/bin/bash

curl --location --request POST 'http://localhost:8083/connectors' \
--header 'Content-Type: application/json' \
--data-raw '{
  "name": "griddb_web_api_sink",
  "config": {
    "connector.class": "io.confluent.connect.http.HttpSinkConnector",
    "transforms": "timestamp, nestedList",
    "topics": "topic_griddb_cloud",
    "transforms.timestamp.type": "org.apache.kafka.connect.transforms.TimestampConverter$Value",
    "transforms.timestamp.target.type": "string",
    "transforms.timestamp.field": "ts",
    "transforms.timestamp.format": "yyyy-MM-dd'\''T'\''HH:mm:ss.SSS'\''Z'\'' ",
    "transforms.nestedList.type": "net.griddb.GridDBWebAPITransform$Value",
    "transforms.nestedList.fields": "ts",
    "http.api.url": "https://cloud517.griddb.com/griddb/v2/gs_clustermfcloud517/dbs/ZVlQ8/containers/kafka_ts/rows",
    "request.method": "put",
    "headers": "Content-Type: application/json",
    "auth.type": "basic",
    "connection.user": "0VkG-israel",
    "connection.password": "password",
    "https.ssl.key.password": "confluent",
    "https.ssl.keystore.key": "",
    "https.ssl.keystore.location": "/etc/kafka/secrets/kafka.kafka-1.keystore.pkcs12",
    "https.ssl.keystore.password": "confluent",
    "https.ssl.truststore.location": "/etc/kafka/secrets/kafka.client.truststore.jks",
    "https.ssl.truststore.password": "confluent",
    "https.ssl.enabled.protocols": "",
    "https.ssl.keystore.type": "PKCS12",
    "https.ssl.protocol": "TLSv1.2",
    "https.ssl.truststore.type": "JKS",
    "reporter.result.topic.replication.factor": "1",
    "reporter.error.topic.replication.factor": "1",
    "reporter.bootstrap.servers": "broker:29092"
  }
}'