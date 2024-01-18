import requests
import json

url = "https://cloud5197.griddb.com/griddb/v2/gs_clustermfcloud5197/dbs/B2xcGQJy/containers/deviceMaster/rows"

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
  'Authorization': 'Basic TTAxMU1sd0MxYS1pc3JhZWw6aXNyYWVs',
  'User-Agent': 'PostmanRuntime/7.29.0' 
}

response = requests.request("PUT", url, headers=headers, data=payload)

print(response.text)
