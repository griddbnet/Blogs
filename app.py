import re
import json
import numpy as np
import pandas as pd
import streamlit as st
import griddb_python as griddb
import altair as alt

import requests

# header_obj = {"Authorization":"Basic XXX","Content-Type":"application/json; charset=UTF-8","User-Agent":"PostmanRuntime/7.29.0"}
# base_url = 'https://[host]:[port]/griddb/v2/[clustername]/dbs/[database_name]/'
header_obj = {"Authorization": "Basic aXNyYWVsOmlzcmFlbA==",
              "Content-Type": "application/json; charset=UTF-8", "User-Agent": "PostmanRuntime/7.29.0"}
base_url = 'https://cloud1.griddb.com/trial1602/griddb/v2/gs_clustertrial1602/dbs/streamlit/'


scoring_threshold = st.slider("Total_Points", 3000, 8000, 3000, 200)
total_points_query_str = ("select * where total_points > " + str(scoring_threshold))

url_total_points = base_url + 'tql'
request_body_total_pts = '[{"name":"Playoff_Scorers", "stmt":"' + \
    total_points_query_str+'", "columns":[]}]'

res = requests.post(url_total_points, data=request_body_total_pts, headers=header_obj)
total_points_data = res.json()

dataset_columns = ['rank', 'player', 'position', 'teams', 'total_points', 'total_games', 'total_points_pre_game',
                   'field_goals', 'three_point_goals', 'free_shots', 'born', 'active_player', 'country', 'recording_year']


scorers = pd.DataFrame(total_points_data[0]["results"], columns=dataset_columns)

st.header("Num of Players: " + str(scorers.shape[0]))
st.bar_chart(data=scorers, x='player', y='total_points')


amt_of_games = st.slider("Total_Playoff_Games", 100, 300, 100, 10)
amt_of_games_query_str = ("select * where total_games > " + str(amt_of_games))

url_amt_of_games = base_url + 'tql'
request_body_amt_games = '[{"name":"Playoff_Scorers", "stmt":"' + \
    amt_of_games_query_str+'", "columns":[]}]'

res_amt_of_games = requests.post(url_amt_of_games, data=request_body_amt_games, headers=header_obj)
amt_of_games_data = res_amt_of_games.json()

amt_of_games = pd.DataFrame(amt_of_games_data[0]["results"], columns=dataset_columns)

st.header("Num of Players: " + str(amt_of_games.shape[0]))
st.bar_chart(data=amt_of_games, x='player', y='total_games')


three_pointers = st.slider("Amount of Three Pointers", 0, 600, 0, 10)
field_goals	 = st.slider("Field Goals", 1000, 3000, 1000, 50)
three_pointers_query_str = ("select * where three_point_goals >= " + str(three_pointers) + " AND  field_goals > " +  str(field_goals) )

url_three_pointers = base_url + 'tql'
request_body_three_pointers = '[{"name":"Playoff_Scorers", "stmt":"' + \
    three_pointers_query_str+'", "columns":[]}]'

res_three_pointers = requests.post(url_three_pointers, data=request_body_three_pointers, headers=header_obj)
three_pointers_data = res_three_pointers.json()

three_pointers = pd.DataFrame(three_pointers_data[0]["results"], columns=dataset_columns)

st.header("Num of Players: " + str(three_pointers.shape[0]))
three_percentage = three_pointers["three_point_goals"]/three_pointers["field_goals"]
three_pointers["three_percentage"] = (three_percentage * 100)

st.bar_chart(data=three_pointers, x='player', y="three_percentage")