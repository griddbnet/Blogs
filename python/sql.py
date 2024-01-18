import requests
import json

url = "https://cloud5197.griddb.com/griddb/v2/gs_clustermfcloud5197/dbs/B2xcGQJy/sql"

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
  'Authorization': 'Basic TTAxMU1sd0MxYS1pc3JhZWw6aXNyYWVs',
  'User-Agent': 'PostmanRuntime/7.29.0' 
}

response = requests.request("POST", url, headers=headers, data=payload)

print(response.text)
