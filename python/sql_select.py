import requests
import os
import json
base=os.environ['GRIDDB_CLOUD_URL']
creds=os.environ['USER_PASS']


url = base+"/sql"

payload = json.dumps([
  {
    "type": "sql-select",
    "stmt": "SELECT * FROM deviceMaster"
  },
  {
    "type": "sql-select",
    "stmt": "SELECT temp, co FROM device1 WHERE temp>=24"
  }
])
headers = {
  'Content-Type': 'application/json',
  'Authorization': 'Basic '+creds,
  'User-Agent': 'PostmanRuntime/7.29.0' 
}

response = requests.request("POST", url, headers=headers, data=payload)

print(response.text)
