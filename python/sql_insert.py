import requests
import os
import json
base=os.environ['GRIDDB_WEBAPI_URL']
creds=os.environ['USER_PASS']


url = base+"/sql/update"

payload = json.dumps([
  {
    "stmt": "insert into deviceMaster(equipment, equipmentID, location, serialNumber, lastInspection, information) values('device2', '02', 'MA', '34412', TIMESTAMP('2023-12-21T10:45:00.032Z'), 'working')"
  }
])
headers = {
  'Content-Type': 'application/json',
  'Authorization': 'Basic '+creds,
  'User-Agent': 'PostmanRuntime/7.29.0' 
}

response = requests.request("POST", url, headers=headers, data=payload)

print(response.text)