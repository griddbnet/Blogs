#!/bin/bash

curl -i --location --request PUT "${GRIDDB_WEBAPI_URL}/containers/deviceMaster/rows" \
--header 'Content-Type: application/json' \
--header "Authorization: Basic ${USER_PASS}"  \
--data '[
  ["device1", "01", "CA", "23412", "2023-12-15T10:45:00.032Z", "working"]
]'