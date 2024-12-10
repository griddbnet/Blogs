#!/bin/bash

curl -i --location --request DELETE "${GRIDDB_CLOUD_URL}/containers" \
--header 'Content-Type: application/json' \
--header "Authorization: Basic ${USER_PASS}"  \
--data '[
  "deviceMaster"
]'