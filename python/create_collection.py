import requests
import json

url = "https://cloud5197.griddb.com/griddb/v2/gs_clustermfcloud5197/dbs/B2xcGQJy/containers"

payload = json.dumps({
  "container_name": "deviceMaster",
  "container_type": "COLLECTION",
  "rowkey": True,
  "columns": [
    {
      "name": "equipment",
      "type": "STRING"
    },
    {
      "name": "equipmentID",
      "type": "STRING"
    },
    {
      "name": "location",
      "type": "STRING"
    },
    {
      "name": "serialNumber",
      "type": "STRING"
    },
    {
      "name": "lastInspection",
      "type": "TIMESTAMP"
    },
    {
      "name": "information",
      "type": "STRING"
    }
  ]
})
headers = {
  'Content-Type': 'application/json',
  'Authorization': 'Basic TTAxMU1sd0MxYS1pc3JhZWw6aXNyYWVs',
  'User-Agent': 'PostmanRuntime/7.29.0'
}

response = requests.request("POST", url, headers=headers, data=payload)

print(response.status_code)
