import pandas as pd
import numpy as np
import csv
from datetime import datetime as dt

df = pd.read_csv ('iot_telemetry_data.csv')
df.head()

#Check data types
print(df.dtypes)


# convert ts in unix to ISO 8601 timestamp format
df['ts'] = pd.to_datetime(df['ts'], unit = 's').dt.strftime('%Y-%m-%dT%H:%M:%S.%f+0000')
df.head(5)

# split data by device as recommended by GridDB schema (1 time series container per device)
dfs = dict(tuple(df.groupby('device')))

# Separate device1 data and remove device column as it will be stored in collection container
device1 = dfs['b8:27:eb:bf:9d:51']
device1 = device1.drop('device', axis=1)
device1.head()

# Separate device2 data and remove device column as it will be stored in collection container
device2 = dfs['00:0f:00:70:91:0a']
device2 = device2.drop('device', axis=1)
device2.head()

# Separate device3 data and remove device column as it will be stored in collection container
device3 = dfs['1c:bf:ce:15:ec:4d']
device3 = device3.drop('device', axis=1)
device3.head()

device1.to_csv('device1.csv', encoding='utf-8', quotechar='"', index=False, quoting=csv.QUOTE_ALL)
device2.to_csv('device2.csv', encoding='utf-8', quotechar='"', index=False, quoting=csv.QUOTE_ALL)
device3.to_csv('device3.csv', encoding='utf-8', quotechar='"', index=False, quoting=csv.QUOTE_ALL)
