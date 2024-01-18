import requests
import json

url = "https://cloud5197.griddb.com/griddb/v2/gs_clustermfcloud5197/dbs/B2xcGQJy/tql"

payload = json.dumps([
  {
    "name": "deviceMaster",
    "stmt": "select * limit 100",
    "columns": None
  },
  {
    "name": "device1",
    "stmt": "select * where temp>=24",
    "columns": [
      "temp",
      "co"
    ]
  }
])
headers = {
  'Content-Type': 'application/json',
  'Authorization': 'Basic TTAxMU1sd0MxYS1pc3JhZWw6aXNyYWVs',
  'User-Agent': 'PostmanRuntime/7.29.0' 
}

response = requests.request("POST", url, headers=headers, data=payload)

print(response.text)
