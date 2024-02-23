#!/bin/sh
curl -s \
     -X "POST" "http://localhost:8083/connectors/" \
     -H "Content-Type: application/json" \
     -d '{
            "name": "test-sink", 
            "config": {
                "connector.class":"io.confluent.connect.jdbc.JdbcSinkConnector",
                "tasks.max":"1",
                "topics.regex": "meter.(.*)",
                "table.name.format": "kafka_${topic}",
            	"dialect.name": "PostgreSqlDatabaseDialect",
                "transforms": "TimestampConverter",
        	    "transforms.TimestampConverter.type": "org.apache.kafka.connect.transforms.TimestampConverter$Value",
        	    "transforms.TimestampConverter.format": "yyyy-MM-dd hh:mm:ss.SSS",
        	    "transforms.TimestampConverter.field": "timestamp",
        	    "transforms.TimestampConverter.target.type": "string",
                "time.precision.mode": "connect",
                "connection.url":"jdbc:gs://griddb-server:20001/myCluster/public",
                "connection.user": "admin",
                "connection.password": "admin",
                "batch.size": "1000",
                "auto.create":"true",
                "pk.mode" : "none",
                "insert.mode": "insert",
                "auto.evolve": "true"
            }
}'
