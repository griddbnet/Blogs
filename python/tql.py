import requests
import os
import json
base=os.environ['GRIDDB_WEBAPI_URL']
creds=os.environ['USER_PASS']


url = base+"/tql"

payload = json.dumps([
  {
    "name": "deviceMaster",
    "stmt": "select * limit 100",
    "columns": None
  },
  {
    "name": "device1",
    "stmt": "select * where temp>=24",
    "columns": [
      "temp",
      "co"
    ]
  }
])
headers = {
  'Content-Type': 'application/json',
  'Authorization': 'Basic '+creds,
  'User-Agent': 'PostmanRuntime/7.29.0' 
}

response = requests.request("POST", url, headers=headers, data=payload)

print(response.text)
