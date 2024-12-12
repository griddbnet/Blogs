import requests
import os
import json
base=os.environ['GRIDDB_WEBAPI_URL']
creds=os.environ['USER_PASS']


url = base+"/containers"

payload = json.dumps({
  "container_name": "device1",
  "container_type": "TIME_SERIES",
  "rowkey": True,
  "columns": [
    {
      "name": "ts",
      "type": "TIMESTAMP"
    },
    {
      "name": "co",
      "type": "DOUBLE"
    },
    {
      "name": "humidity",
      "type": "DOUBLE"
    },
    {
      "name": "light",
      "type": "BOOL"
    },
    {
      "name": "lpg",
      "type": "DOUBLE"
    },
    {
      "name": "motion",
      "type": "BOOL"
    },
    {
      "name": "smoke",
      "type": "DOUBLE"
    },
    {
      "name": "temp",
      "type": "DOUBLE"
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

