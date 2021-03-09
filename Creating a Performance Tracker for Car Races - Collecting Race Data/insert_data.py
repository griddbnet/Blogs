#!/usr/bin/python

import griddb_python as griddb
import sys
import pandas as pd

factory = griddb.StoreFactory.get_instance()

argv = sys.argv

try:
    # Create data frames while defining null values
    circuits_data = pd.read_csv("./dataset/circuits.csv", na_values=['\\N'], nrows=10) 
    races_data = pd.read_csv("./dataset/races.csv", na_values=['\\N'], nrows=10) 

    # View the structure of the data frames
    circuits_data.info()
    races_data.info()

    # Drop the alt colum 
    circuits_data.drop(['alt'], inplace=True, axis=1)

    # Create a combined raceTime colum
    races_data['time'].replace({pd.NaT: "00:00:00"}, inplace=True)
    races_data['raceTime'] = races_data['date'] + " " + races_data['time']
    races_data['raceTime'] = pd.to_datetime(races_data['raceTime'], infer_datetime_format=True)

    # Drop the data and time columns
    races_data.drop(['date', 'time'], inplace=True, axis=1)

    # View the structure of modified data frames
    circuits_data.info()
    races_data.info()

    # Get GridStore object
    # Provide the necessary arguments
    gridstore = factory.get_store(
        host=argv[1], 
        port=int(argv[2]), 
        cluster_name=argv[3], 
        username=argv[4], 
        password=argv[5]
    )

    circuits_container = "circuits"
    races_container = "races"

    # Create Collection circuits
    circuits_containerInfo = griddb.ContainerInfo(circuits_container,
                    [["circuitId", griddb.Type.INTEGER],
                    ["circuitRef", griddb.Type.STRING],
                    ["name", griddb.Type.STRING],
                    ["location", griddb.Type.STRING],
                    ["country", griddb.Type.STRING],
                    ["lat", griddb.Type.FLOAT],
                    ["lng", griddb.Type.FLOAT],
                    ["url", griddb.Type.STRING]],
                    griddb.ContainerType.COLLECTION, True)
    circuits_columns = gridstore.put_container(circuits_containerInfo)
    
    # Create Collection races
    races_containerInfo = griddb.ContainerInfo(races_container,
                    [["raceID", griddb.Type.INTEGER],
                    ["year", griddb.Type.INTEGER],
                    ["round", griddb.Type.INTEGER],
                    ["circuitId", griddb.Type.INTEGER],
                    ["name", griddb.Type.STRING],
                    ["url", griddb.Type.STRING],
                    ["raceTime", griddb.Type.TIMESTAMP]],
                    griddb.ContainerType.COLLECTION, True)
    races_columns = gridstore.put_container(races_containerInfo)


    # Put rows
    # Define the data frames
    circuits_columns.put_rows(circuits_data)
    races_columns.put_rows(races_data)
    
    print("Data Inserted using the DataFrame")

except griddb.GSException as e:
    for i in range(e.get_error_stack_size()):
        print("[", i, "]")
        print(e.get_error_code(i))
        print(e.get_location(i))
        print(e.get_message(i))