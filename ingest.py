import pandas as pd
import streamlit as st
import griddb_python as griddb
import datetime
import traceback
import requests
import http
http.client.HTTPConnection.debuglevel = 1
import json
import re

#header_obj = {"Authorization":"Basic XXX","Content-Type":"application/json; charset=UTF-8","User-Agent":"PostmanRuntime/7.29.0"}
#base_url = 'https://[host]:[port]/griddb/v2/[clustername]/dbs/[database_name]/'
header_obj = {"Authorization":"Basic aXNyYWVsOmlzcmFlbA==","Content-Type":"application/json; charset=UTF-8","User-Agent":"PostmanRuntime/7.29.0"}
base_url = 'https://cloud1.griddb.com/trial1602/griddb/v2/gs_clustertrial1602/dbs/streamlit/'

#Construct an object to hold the request body (i.e., the container that needs to be created)

data_obj ={
   "container_name":"Playoff_Scorers",
   "container_type":"COLLECTION", 
   "rowkey":False,
   "columns":[
      {
         "name":"rank",
         "type":"INTEGER"
      },
      {
         "name":"player",
         "type":"STRING"
      },
      {
         "name":"position",
         "type":"STRING"
      },
      {
         "name":"teams",
         "type":"STRING"
      },
      {
         "name":"total_points",
         "type":"INTEGER"
      },
      {
         "name":"total_games",
         "type":"INTEGER"
      },
      {
         "name":"total_points_pre_game",
         "type":"FLOAT"
      },
      {
         "name":"field_goals",
         "type":"INTEGER"
      },
      {
         "name":"three_point_goals",
         "type":"INTEGER"
      },
      {
         "name":"free_shots",
         "type":"INTEGER"
      },
      {
         "name":"born",
         "type":"STRING"
      },
      {
         "name":"active_player",
         "type":"INTEGER"
      },
      {
         "name":"country",
         "type":"STRING"
      },
      {
         "name":"recording_year",
         "type":"INTEGER"
      }
   ]
}

#Set up the GridDB WebAPI URL
url = base_url + 'containers'
x = requests.post(url, json = data_obj, headers = header_obj)


df = pd.read_csv('nba_playoffs.csv')
df = df.drop('hall_of_fame', axis=1)

#scorers.put_rows(df)

scores_json = df.to_json(orient="values")
print(scores_json)


#Invoke the GridDB WebAPI using the request constructed
url = base_url + 'containers/Playoff_Scorers/rows'
x = requests.put(url, data=scores_json, headers=header_obj)
