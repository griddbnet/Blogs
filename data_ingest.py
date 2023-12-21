import pandas as pd
import numpy as np
import json
import requests
from datetime import datetime, timezone



headers = {
  'Content-Type': 'application/json',
  'Authorization': 'Basic aXNyYWVsOmlzcmFlbA==',
  "User-Agent":"PostmanRuntime/7.29.0"
}
base_url = 'https://cloud1.griddb.com/trial1602/griddb/v2/gs_clustertrial1602/dbs/public/'

data_obj = {
    "container_name": "iot_data2",
    "container_type": "TIME_SERIES",
    "rowkey": True,
    "columns": []
}
input_variables = [
    "ts","device","co","humidity","light","lpg","motion","smoke","temp"
]
data_types = [
    "TIMESTAMP", "STRING", "DOUBLE", "DOUBLE", "BOOL", "DOUBLE", "BOOL", "DOUBLE","DOUBLE"
]

for variable, data_type in zip(input_variables, data_types):
    column = {
        "name": variable,
        "type": data_type
    }
    data_obj["columns"].append(column)

# Create Container
url = base_url + 'containers'
r = requests.post(url, json = data_obj, headers = headers)

iot_data = pd.read_csv('iot_telemetry_data.csv')

iot_data['ts'] = pd.to_datetime(iot_data['ts'], unit='s').dt.strftime('%Y-%m-%dT%H:%M:%S.%fZ')
print(iot_data["ts"])
iot_data["device"] = iot_data["device"].astype("string")
print(iot_data.dtypes)

iot_subsets = np.array_split(iot_data, 200)

# Ingest Data
url = base_url + 'containers/iot_data/rows'

for subset in  iot_subsets:
    #Convert the data in the dataframe to the JSON format
    iot_subsets_json = subset.to_json(orient='values')

    request_body_subset = iot_subsets_json
    r = requests.put(url, data=request_body_subset, headers=headers)
    print('~~~~~~~~~~~~~~~~~~~~~~~~~~~~~')
    print('_______________',r.text,'___________')
    print('~~~~~~~~~~~~~~~~~~~~~~~~~~~~~')
    if r.status_code > 299: 
        print(r.status_code)
        break
    else:
        print('Success for chunk..')
