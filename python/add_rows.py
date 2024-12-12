import requests
import os
import json
base=os.environ['GRIDDB_WEBAPI_URL']
creds=os.environ['USER_PASS']


url = base+"/containers/device1/rows"

payload = json.dumps([
  [
    "2024-01-09T10:00:01.234Z",
    0.003551,
    50,
    False,
    0.00754352,
    False,
    0.0232432,
    21.6
  ],
  [
    "2024-01-09T11:00:01.234Z",
    0.303551,
    60,
    False,
    0.00754352,
    True,
    0.1232432,
    25.3
  ],
  [
    "2024-01-09T12:00:01.234Z",
    0.603411,
    70,
    True,
    0.00754352,
    True,
    0.4232432,
    41.5
  ]
])
headers = {
  'Content-Type': 'application/json',
  'Authorization': 'Basic '+creds,
  'User-Agent': 'PostmanRuntime/7.29.0'
}

response = requests.request("PUT", url, headers=headers, data=payload)

print(response.text)
