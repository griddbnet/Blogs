#!/bin/bash


curl -i --location "${GRIDDB_CLOUD_URL}/checkConnection" \
--header "Authorization: Basic ${USER_PASS}"
