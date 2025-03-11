#!/bin/sh

curl -s \
     -X "POST" "http://localhost:8083/connectors/" \
     -H "Content-Type: application/json" \
     -d '{
  "name": "web_api_datagen",
  "config": {
    "connector.class": "io.confluent.kafka.connect.datagen.DatagenConnector",
    "kafka.topic": "griddb_test",
    "schema.string": "{   \"connect.name\": \"net.griddb.webapi.griddb\",   \"connect.parameters\": {     \"io.confluent.connect.avro.field.doc.data\": \"The string is a unicode character sequence.\",     \"io.confluent.connect.avro.field.doc.temp\": \"The double type is a double precision (64-bit) IEEE 754 floating-point number.\",     \"io.confluent.connect.avro.field.doc.ts\": \"The int type is a 32-bit signed integer.\",     \"io.confluent.connect.avro.record.doc\": \"Sample schema to help you get started.\"   },   \"doc\": \"Sample schema to help you get started.\",   \"fields\": [     {       \"doc\": \"The int type is a 32-bit signed integer.\",       \"name\": \"ts\",       \"type\": \"int\"     },     {       \"doc\": \"The double type is a double precision (64-bit) IEEE 754 floating-point number.\",       \"name\": \"temp\",       \"type\": \"double\"     },     {       \"doc\": \"The string is a unicode character sequence.\",       \"name\": \"data\",       \"type\": \"double\"     }   ],   \"name\": \"griddb\",   \"namespace\": \"net.griddb.webapi\",   \"type\": \"record\" }"
  }
}'
