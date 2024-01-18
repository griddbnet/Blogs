#!/bin/bash

curl -i -X POST --location 'https://cloud5197.griddb.com/griddb/v2/gs_clustermfcloud5197/dbs/B2xcGQJy/tql' \
--header 'Content-Type: application/json' \
--header 'Authorization: Basic TTAxMU1sd0MxYS1pc3JhZWw6aXNyYWVs' \
--data '
[
  {"name" : "deviceMaster", "stmt" : "select * limit 100", "columns" : null},
  {"name" : "device1", "stmt" : "select * where temp>=24", "columns" : ["temp", "co"]}
]
'