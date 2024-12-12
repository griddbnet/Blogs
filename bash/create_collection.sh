curl -i -X POST --location "${GRIDDB_WEBAPI_URL}/containers" \
--header 'Content-Type: application/json' \
--header "Authorization: Basic ${USER_PASS}" \
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