#!/bin/bash

curl -i --location --request DELETE "${GRIDDB_WEBAPI_URL}/containers/deviceMaster/rows" \
--header 'Content-Type: application/json' \
--header "Authorization: Basic ${USER_PASS}"  \
--data '[
  "device1"
]'