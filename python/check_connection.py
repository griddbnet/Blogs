import requests

url = "https://cloud5197.griddb.com/griddb/v2/gs_clustermfcloud5197/dbs/B2xcGQJy/checkConnection"

payload = {}
headers = {
  'Authorization': 'Basic TTAxMU1sd0MxYS1pc3JhZWw6aXNyYWVs',
  'User-Agent': 'PostmanRuntime/7.29.0'
}

response = requests.request("GET", url, headers=headers, data=payload)

print(response.status_code)

