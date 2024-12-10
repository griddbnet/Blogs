import requests
import os
import json
base=os.environ['GRIDDB_CLOUD_URL']
creds=os.environ['USER_PASS']


url = base+"/containers/device1/rows"

payload = json.dumps({
  "offset": 0,
  "limit": 100,
  "condition": "temp >= 30",
  "sort": "temp desc"
})
headers = {
  'Content-Type': 'application/json',
  'Authorization': 'Basic '+creds,
  'User-Agent': 'PostmanRuntime/7.29.0' 
}

response = requests.request("POST", url, headers=headers, data=payload)

print(response.text)
