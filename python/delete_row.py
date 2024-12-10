import requests
import os
import json
base=os.environ['GRIDDB_CLOUD_URL']
creds=os.environ['USER_PASS']


url = base+"/containers/deviceMaster/rows"

payload = json.dumps([
  "device1"
])
headers = {
  'Content-Type': 'application/json',
  'Authorization': 'Basic '+creds,
  'User-Agent': 'PostmanRuntime/7.29.0' 
}

response = requests.request("DELETE", url, headers=headers, data=payload)

print(response.status_code)