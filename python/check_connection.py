import requests
import os
base=os.environ['GRIDDB_WEBAPI_URL']
creds=os.environ['USER_PASS']

url = base+"/checkConnection"

payload = {}
headers = {
  'Authorization': 'Basic '+creds,
  'User-Agent': 'PostmanRuntime/7.29.0'
}

response = requests.request("GET", url, headers=headers, data=payload)

print(response.status_code)

