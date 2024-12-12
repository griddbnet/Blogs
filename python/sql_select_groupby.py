import requests
import os
import json
base=os.environ['GRIDDB_WEBAPI_URL']
creds=os.environ['USER_PASS']


url = base+"/sql"

payload = json.dumps([
  {
    "type": "sql-select",
    "stmt": "SELECT temp, co FROM device1 WHERE ts > TO_TIMESTAMP_MS(1594515625984) AND ts < TO_TIMESTAMP_MS(1595040779336) GROUP BY RANGE (ts) EVERY (1, HOUR)"
  }
])
headers = {
  'Content-Type': 'application/json',
  'Authorization': 'Basic '+creds,
  'User-Agent': 'PostmanRuntime/7.29.0'
}

response = requests.request("POST", url, headers=headers, data=payload)

print(response.text)
