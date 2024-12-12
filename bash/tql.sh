#!/bin/bash

curl -i -X POST --location "${GRIDDB_WEBAPI_URL}/tql" \
--header 'Content-Type: application/json' \
--header "Authorization: Basic ${USER_PASS}"  \
--data '
[
  {"name" : "deviceMaster", "stmt" : "select * limit 100", "columns" : null},
  {"name" : "device1", "stmt" : "select * where temp>=24", "columns" : ["temp", "co"]}
]
'