import requests
import json

url = "https://cloud5197.griddb.com/griddb/v2/gs_clustermfcloud5197/dbs/B2xcGQJy/containers/cloud_quickstart/rows"

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
  'Authorization': 'Basic TTAxMU1sd0MxYS1pc3JhZWw6aXNyYWVs',
  'User-Agent': 'PostmanRuntime/7.29.0'
}

response = requests.request("PUT", url, headers=headers, data=payload)

print(response.text)
