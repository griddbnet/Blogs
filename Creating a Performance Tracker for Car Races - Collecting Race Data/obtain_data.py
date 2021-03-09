#!/usr/bin/python

import numpy as np
import griddb_python as griddb
import sys
import pandas as pd

factory = griddb.StoreFactory.get_instance()

argv = sys.argv

try:
    # Get GridStore object
    # Provide the necessary arguments
    gridstore = factory.get_store(
        host=argv[1], 
        port=int(argv[2]), 
        cluster_name=argv[3], 
        username=argv[4], 
        password=argv[5]
    )

    # Define the container names
    circuits_container = "circuits"
    races_container = "races"

    # Get the containers
    circuits_data = gridstore.get_container(circuits_container)
    races_data = gridstore.get_container(races_container)
    
    # Fetch all rows - circuits_container
    query = circuits_data.query("select *")
    rs = query.fetch(False)
    print(f"{circuits_container} Data")
    
    # Iterate and create a list
    retrieved_data= []
    while rs.has_next():
        data = rs.next()
        retrieved_data.append(data)
   
    print(retrieved_data)

    # Convert the list to a pandas data frame
    circuits_dataframe = pd.DataFrame(retrieved_data, columns=['circuitId', 'circuitRef', 'name', 'location', 'country', 'lat', 'lng', 'url'])

    # Get the data frame details
    print(circuits_dataframe )
    circuits_dataframe.info()
    
    # Fetch all rows - races_container
    query = races_data.query("select * where raceTime >= TIMESTAMP('2010-12-31T10:10:00.000Z')")
    rs = query.fetch()
    print(f"{races_container} Data")

    # Iterate and create a list
    retrieved_data= []
    while rs.has_next():
        data = rs.next()
        retrieved_data.append(data)
  
    print(retrieved_data)

    # Convert the list to a pandas data frame
    races_dataframe = pd.DataFrame(retrieved_data, columns=['raceID', 'year', 'round', 'circuitId', 'name', 'url', 'raceTime'])

    # Get the data frame details
    print(races_dataframe)
    races_dataframe.info()

    # Rename columns in circuits_dataframe
    circuits_dataframe.rename(
        columns={
            'name' : 'circuit_name',
            'url' : 'circuit_info'
        }, inplace=True
    )

    # Rename columns in races_dataframe
    races_dataframe.rename(
        columns={
            'name' : 'race_name',
            'url' : 'race_info'
        }, inplace=True
    )

    # Merge the two data sets
    result = pd.merge(races_dataframe, circuits_dataframe, on='circuitId')

    # Print the result
    print(result)
    
except griddb.GSException as e:
    for i in range(e.get_error_stack_size()):
        print("[", i, "]")
        print(e.get_error_code(i))
        print(e.get_location(i))
        print(e.get_message(i))