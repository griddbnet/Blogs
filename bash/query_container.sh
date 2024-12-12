#!/bin/bash

curl -i -X POST --location "${GRIDDB_WEBAPI_URL}/containers/device1/rows" \
--header 'Content-Type: application/json' \
--header "Authorization: Basic ${USER_PASS}"  \
--data '{
  "offset" : 0,
  "limit"  : 100,
  "condition" : "temp >= 30",
  "sort" : "temp desc"
}'
