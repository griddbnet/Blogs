#!/bin/bash

curl --location --request PUT "${GRIDDB_WEBAPI_URL}/containers/device1/rows" \
--header 'Content-Type: application/json' \
--header "Authorization: Basic ${USER_PASS}"  \
--data '[
  ["2024-01-09T10:00:01.234Z", 0.003551, 50.0, false, 0.00754352, false, 0.0232432, 21.6],
  ["2024-01-09T11:00:01.234Z", 0.303551, 60.0, false, 0.00754352, true, 0.1232432, 25.3],
  ["2024-01-09T12:00:01.234Z", 0.603411, 70.0, true, 0.00754352, true, 0.4232432, 41.5]
]'
