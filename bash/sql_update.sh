#!/bin/bash

curl -i -X POST --location "${GRIDDB_WEBAPI_URL}/sql/update" \
--header 'Content-Type: application/json' \
--header "Authorization: Basic ${USER_PASS}"  \
--data '[ 
  {"stmt" : "update deviceMaster set location = '\''LA'\'' where equipmentID = '\''01'\''"}
]'