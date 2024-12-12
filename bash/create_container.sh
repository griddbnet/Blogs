#!/bin/bash

curl -i -X POST --location "${GRIDDB_WEBAPI_URL}/containers" \
--header 'Content-Type: application/json' \
--header "Authorization: Basic ${USER_PASS}"  \
--data '{
    "container_name": "device1",
    "container_type": "TIME_SERIES",
    "rowkey": true,
    "columns": [
        {
            "name": "ts",
            "type": "TIMESTAMP"
        },
        {
            "name": "co",
            "type": "DOUBLE"
        },
        {
            "name": "humidity",
            "type": "DOUBLE"
        },
        {
            "name": "light",
            "type": "BOOL"
        },
        {
            "name": "lpg",
            "type": "DOUBLE"
        },
        {
            "name": "motion",
            "type": "BOOL"
        },
        {
            "name": "smoke",
            "type": "DOUBLE"
        },
        {
            "name": "temp",
            "type": "DOUBLE"
        }
    ]
}'
