import requests
import os
import json
base=os.environ['GRIDDB_WEBAPI_URL']
creds=os.environ['USER_PASS']


url = base+"/containers/deviceMaster/rows"

payload = json.dumps([
  [
    "device1",
    "01",
    "CA",
    "23412",
    "2023-12-15T10:45:00.032Z",
    "working"
  ]
])
headers = {
  'Content-Type': 'application/json',
  'Authorization': 'Basic '+creds,
  'User-Agent': 'PostmanRuntime/7.29.0' 
}

response = requests.request("PUT", url, headers=headers, data=payload)

print(response.text)
