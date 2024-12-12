#!/bin/bash


curl -i --location "${GRIDDB_WEBAPI_URL}/checkConnection" \
--header "Authorization: Basic ${USER_PASS}"
