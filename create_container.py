import http.client
import json

conn = http.client.HTTPSConnection("cloud5197.griddb.com")
payload = json.dumps({
  "container_name": "aqdata",
  "container_type": "TIME_SERIES",
  "rowkey": True,
  "columns": [
    {
      "name": "ts",
      "type": "TIMESTAMP"
    },
    {
      "name": "pm1",
      "type": "DOUBLE"
    },
    {
      "name": "pm25",
      "type": "DOUBLE"
    },
    {
      "name": "pm10",
      "type": "DOUBLE"
    },
    {
      "name": "pm1e",
      "type": "DOUBLE"
    },
    {
      "name": "pm25e",
      "type": "DOUBLE"
    },
    {
      "name": "pm10e",
      "type": "DOUBLE"
    },
    {
      "name": "particles03",
      "type": "DOUBLE"
    },
    {
      "name": "particles05",
      "type": "DOUBLE"
    },
    {
      "name": "particles10",
      "type": "DOUBLE"
    },
    {
      "name": "particles25",
      "type": "DOUBLE"
    },
    {
      "name": "particles50",
      "type": "DOUBLE"
    },
    {
      "name": "particles100",
      "type": "DOUBLE"
    }
  ]
})
headers = {
  'Content-Type': 'application/json',
  'Authorization': 'Basic TTAxMU1sd0MxYS1pc3JhZWw6aXNyYWVs'
}
conn.request("POST", "/griddb/v2/gs_clustermfcloud5197/dbs/B2xcGQJy/containers", payload, headers)
res = conn.getresponse()
data = res.read()
print(data.decode("utf-8"))
