import pandas as pd
import numpy as np
import json
import requests
import plotly.express as px
import plotly.graph_objects as go
import plotly.figure_factory as ff
from IPython.display import Image

# ts           object
# device       string
# co          float64
# humidity    float64
# light          bool
# lpg         float64
# motion         bool
# smoke       float64
# temp        float64


headers = {
  'Content-Type': 'application/json',
  'Authorization': 'Basic aXNyYWVsOmlzcmFlbA==',
  "User-Agent":"PostmanRuntime/7.29.0"
}
base_url = 'https://cloud5197.griddb.com/griddb/v2/gs_clustermfcloud5197/dbs/B2xcGQJy/'

sql_query1 = (f"""SELECT * from iot_data WHERE co < 0.0019050147565559603 """)

#Setup the URL to be used to invoke the GridDB WebAPI to retrieve data from the container
url = base_url + 'sql'

#Construct the request body
request_body = '[{"type":"sql-select", "stmt":"'+sql_query1+'"}]'

#Validate the constructed request body
request_body

data_req1 = requests.post(url, data=request_body, headers=headers)
#Process the response received and construct a Pandas dataframe with the data from the response
myJson = data_req1.json()

dataset = pd.DataFrame(myJson[0]["results"],columns=[myJson[0]["columns"][0]["name"],myJson[0]["columns"][1]["name"],myJson[0]["columns"][2]["name"],
myJson[0]["columns"][3]["name"],myJson[0]["columns"][4]["name"],myJson[0]["columns"][5]["name"],myJson[0]["columns"][6]["name"],myJson[0]["columns"][7]["name"],myJson[0]["columns"][8]["name"]])

print(dataset)

lowest_col = dataset.sort_values('co', ascending=False).head(20000)

scatter_plot = px.scatter(lowest_col, x='ts', y='co', size='co', color='co',
                          color_continuous_scale='plasma', hover_name='co') #viridis

# Customize the plot
scatter_plot.update_layout(
    title='Data Analysis',
    xaxis_title='CO2 Emissions',
    yaxis_title='Time'
)

scatter_plot.update_layout(template='plotly_dark')


# Show the plot
scatter_plot.show()