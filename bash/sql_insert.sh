curl -i -X POST --location "${GRIDDB_CLOUD_URL}/sql/update" \
--header 'Content-Type: application/json' \
--header "Authorization: Basic ${USER_PASS}"  \
--data '[ 
  {"stmt" : "insert into deviceMaster(equipment, equipmentID, location, serialNumber, lastInspection, information) values('\''device2'\'', '\''02'\'', '\''MA'\'', '\''34412'\'', TIMESTAMP('\''2023-12-21T10:45:00.032Z'\''), '\''working'\'')"}
]'