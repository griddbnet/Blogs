import requests
import os
import json
base=os.environ['GRIDDB_WEBAPI_URL']
creds=os.environ['USER_PASS']


url = base+"/containers"

payload = json.dumps({
  "container_name": "deviceMaster",
  "container_type": "COLLECTION",
  "rowkey": True,
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
})
headers = {
  'Content-Type': 'application/json',
  'Authorization': 'Basic '+creds,
  'User-Agent': 'PostmanRuntime/7.29.0'
}

response = requests.request("POST", url, headers=headers, data=payload)

print(response.status_code)
