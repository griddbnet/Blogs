curl -i -X POST --location 'https://cloud5197.griddb.com/griddb/v2/gs_clustermfcloud5197/dbs/B2xcGQJy/containers' \
--header 'Content-Type: application/json' \
--header 'Authorization: Basic TTAxMU1sd0MxYS1pc3JhZWw6aXNyYWVs' \
--data '{
    "container_name": "deviceMaster",
    "container_type": "COLLECTION",
    "rowkey": true,
    "columns": [
        {
            "name": "equipment",
            "type": "STRING"
        },
        {
            "name": "equipmentID",
            "type": "STRING"
        },
        {
            "name": "location",
            "type": "STRING"
        },
        {
            "name": "serialNumber",
            "type": "STRING"
        },
        {
            "name": "lastInspection",
            "type": "TIMESTAMP"
        },
        {
            "name": "information",
            "type": "STRING"
        }
    ]
}'