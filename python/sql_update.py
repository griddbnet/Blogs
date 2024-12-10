import requests
import os
import json
base=os.environ['GRIDDB_CLOUD_URL']
creds=os.environ['USER_PASS']


url = base+"/sql/update"

payload = json.dumps([
  {
    "stmt": "update deviceMaster set location = 'LA' where equipmentID = '01'"
  }
])
headers = {
  'Content-Type': 'application/json',
  'Authorization': 'Basic '+creds,
  'User-Agent': 'PostmanRuntime/7.29.0' 
}

response = requests.request("POST", url, headers=headers, data=payload)

print(response.text)