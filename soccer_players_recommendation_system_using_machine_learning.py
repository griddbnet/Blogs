"""Soccer Players Recommendation System Using Machine Learning

## Import Libraries
"""

import numpy as np
import pandas as pd

import matplotlib.pyplot as plt
import seaborn as sns

from sklearn.preprocessing import StandardScaler
from sklearn.neighbors import NearestNeighbors
from sklearn.decomposition import PCA

import griddb_python as griddb

"""## Loading Data"""

data = pd.read_csv('data.csv')

data.head(3)

data.shape

data.columns

data = data.dropna()

data.isnull().sum().sum()

data.dtypes

data['name'] = data['name'].apply(lambda x: x.encode('utf-8').decode('utf-8'))
data[['age', 'position_cat', 'market_value', 'page_views', 'fpl_value', 'fpl_points', 'region', 'new_foreign', 'age_cat', 'club_id', 'big_club', 'new_signing']] = data[['age', 'position_cat', 'market_value', 'page_views', 'fpl_value', 'fpl_points', 'region', 'new_foreign', 'age_cat', 'club_id', 'big_club', 'new_signing']].astype(float)

"""## Setting Up a Container in Griddb to Store the Data"""

factory = griddb.StoreFactory.get_instance()

# Provide the necessary arguments
gridstore = factory.get_store(
    host = '239.0.0.1',
    port = 31999,
    cluster_name = 'defaultCluster',
    username = 'admin',
    password = 'admin'
)

# Define the container info
conInfo = griddb.ContainerInfo(
    "football_players",
    [
        ["name", griddb.Type.STRING],
        ["club", griddb.Type.STRING],
        ["age", griddb.Type.DOUBLE],
        ["position", griddb.Type.STRING],
        ["position_cat", griddb.Type.DOUBLE],
        ["market_value", griddb.Type.DOUBLE],
        ["page_views", griddb.Type.DOUBLE],
        ["fpl_value", griddb.Type.DOUBLE],
        ["fpl_sel", griddb.Type.STRING],
        ["fpl_points", griddb.Type.DOUBLE],
        ["region", griddb.Type.DOUBLE],
        ["nationality", griddb.Type.STRING],
        ["new_foreign", griddb.Type.DOUBLE],
        ["age_cat", griddb.Type.DOUBLE],
        ["club_id", griddb.Type.DOUBLE],
        ["big_club", griddb.Type.DOUBLE],
        ["new_signing", griddb.Type.DOUBLE]
    ],
    griddb.ContainerType.COLLECTION, True
)

# Drop container if it exists
gridstore.drop_container(conInfo.name)

# Create a container
container = gridstore.put_container(conInfo)

# Load the data

# Put rows
for i in range(len(data)):
  row = data.iloc[i].tolist()
  try:
    container.put(row)
  except Exception as e:
    print(f"Error on row {i}: {row}")
    print(e)

cont = gridstore.get_container("football_players")

if cont is None:
  print("Does not exist")

print("connection successful")

# Define the exact columns you need
columns = ["*"]

select_statement = "SELECT " + ", ".join(columns) + " FROM football_players"

# Execute the query
query = container.query(select_statement)
rs = query.fetch(False)

data = rs.fetch_rows()

print(data.head())

"""## Recommendation System"""

sample = data.select_dtypes(include='number')
scaled = StandardScaler()
X = scaled.fit_transform(sample)
recommendations = NearestNeighbors(n_neighbors = 5, algorithm='kd_tree')
recommendations.fit(X)
player_index = recommendations.kneighbors(X)[1]

def find_index(x):
    return data[data['name']==x].index.tolist()[0]

def recommendation_system(player):
    print("Here are four players who are similar to {}: ".format(player))
    index =  find_index(player)
    for i in player_index[index][1:]:
        print("Player Name: {}\nPlayer Market Value: {}\nPlayer Age: {}\nPlayer Current Club: {}\n".format(data.iloc[i]['name'],
                                                                                        data.iloc[i]['market_value'],
                                                                                        data.iloc[i]['age'],
                                                                                        data.iloc[i]['club']))

recommendation_system('Petr Cech')
