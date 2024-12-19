import pandas as pd
import numpy as np
import os
import requests
import plotly.express as px
from IPython.display import Image

base=os.environ['GRIDDB_WEBAPI_URL']
creds=os.environ['USER_PASS']

# ts           object
# co          float64
# humidity    float64
# light          bool
# lpg         float64
# motion         bool
# smoke       float64
# temp        float64


headers = {
  'Content-Type': 'application/json',
  'Authorization': 'Basic '+creds,
  "User-Agent":"PostmanRuntime/7.29.0"
}

sql_query1 = (f"""SELECT * from device2 WHERE co < 0.0019050147565559603 """)

url = base + '/sql'
request_body = '[{"type":"sql-select", "stmt":"'+sql_query1+'"}]'


data_req1 = requests.post(url, data=request_body, headers=headers)
myJson = data_req1.json()

dataset = pd.DataFrame(myJson[0]["results"],columns=[myJson[0]["columns"][0]["name"],myJson[0]["columns"][1]["name"],myJson[0]["columns"][2]["name"],
myJson[0]["columns"][3]["name"],myJson[0]["columns"][4]["name"],myJson[0]["columns"][5]["name"],myJson[0]["columns"][6]["name"],myJson[0]["columns"][7]["name"]])

print(dataset)

lowest_col = dataset.sort_values('co', ascending=False).head(20000)

scatter_plot = px.scatter(lowest_col, x='ts', y='co', size='co', color='co',
                          color_continuous_scale='plasma', hover_name='co')
# Customize the plot
scatter_plot.update_layout(
    title='Data Analysis',
    xaxis_title='CO2 Emissions',
    yaxis_title='Time'
)

scatter_plot.update_layout(template='plotly_dark')


# Show the plot
scatter_plot.show()