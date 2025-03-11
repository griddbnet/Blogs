#!/bin/sh

curl -s \
     -X "POST" "http://localhost:8083/connectors/" \
     -H "Content-Type: application/json" \
     -d '{
  "name": "griddb_web_api_sink",
  "config": {
    "connector.class": "io.confluent.connect.http.HttpSinkConnector",
    "transforms": "nestedList",
    "topics": "griddb_test",
    "transforms.nestedList.type": "net.griddb.GridDBWebAPITransform$Value",
    "transforms.nestedList.fields": "ts",
    "http.api.url": "https://cloud5197.griddb.com/griddb/v2/gs_clustermfcloud5197/dbs/ZlQ8/containers/kafka/rows",
    "request.method": "put",
    "headers": "Content-Type: application/json",
    "auth.type": "basic",
    "connection.user": "user",
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
