import griddb_python as griddb
import sys
import pandas as pd
import matplotlib.pyplot as plt
import numpy as np
from sklearn.cluster import KMeans


factory = griddb.StoreFactory.get_instance()

argv = sys.argv

try:
    
    incidents = pd.read_csv("datasets/downsample_police-department-incidents.csv")
    calls = pd.read_csv("datasets/downsample_police-department-calls-for-service.csv")
    
    
    incidents = incidents.drop(['IncidntNum', 'Time', 'X', 'Y', 'Location', 'PdId'], axis = 1)
    calls = calls.drop(['Crime Id', 'Report Date', 'Offense Date', 'Call Time', 'Call Date Time', 
                        'Disposition', 'Address', 'City', 'State', 'Agency Id','Address Type', 
                        'Common Location'], axis = 1)
      
    incidents.reset_index(drop=True, inplace=True)
    incidents.index.name = 'ID'
    incidents.dropna(inplace = True)
    
    
    calls.reset_index(drop=True, inplace=True)
    calls.index.name = 'ID'
    calls.dropna(inplace = True)
        
    #save it into csv
    incidents.to_csv("preprocessed_incidents.csv")
    calls.to_csv("preprocessed_calls.csv")
    
    #read the cleaned data from csv
    incident_processed = pd.read_csv("preprocessed_incidents.csv")
    calls_processed = pd.read_csv("preprocessed_calls.csv")
    
    for row in incident_processed.itertuples(index=False):
            print(f"{row}")
            
    for row in calls_processed.itertuples(index=False):
            print(f"{row}")

    # View the structure of the data frames
    incident_processed.info()
    calls_processed.info()

    # Provide the necessary arguments
    gridstore = factory.get_store(
        host=argv[1], 
        port=int(argv[2]), 
        cluster_name=argv[3], 
        username=argv[4], 
        password=argv[5]
    )

    #Create container 
    incident_container = "incident_container"

    # Create containerInfo
    incident_containerInfo = griddb.ContainerInfo(incident_container,
                    [["ID", griddb.Type.INTEGER],
        		    ["Category", griddb.Type.STRING],
         		    ["Descript", griddb.Type.STRING],
                    ["DayOfWeek", griddb.Type.STRING],
                    ["Date", griddb.Type.TIMESTAMP],
         		    ["PdDistrict", griddb.Type.STRING],
                    ["Resolution", griddb.Type.STRING]],
                    griddb.ContainerType.COLLECTION, True)
    
    incident_columns = gridstore.put_container(incident_containerInfo)

    print("container created and columns added")
    
    
    # Put rows
    incident_columns.put_rows(incident_processed)
    
    print("Data Inserted using the DataFrame")
    
    #Create container 
    calls_container = "calls_container"

    # Create containerInfo
    calls_containerInfo = griddb.ContainerInfo(calls_container,
                    [["ID", griddb.Type.INTEGER],
         		    ["Descript", griddb.Type.STRING],
                    ["Date", griddb.Type.TIMESTAMP]],
                    griddb.ContainerType.COLLECTION, True)
    
    calls_columns = gridstore.put_container(calls_containerInfo)

    print("container created and columns added")
    
    
    # Put rows
    calls_columns.put_rows(calls_processed)
    
    print("Data Inserted using the DataFrame")

except griddb.GSException as e:
    print(e)
    for i in range(e.get_error_stack_size()):
        print(e)
        # print("[", i, "]")
        # print(e.get_error_code(i))
        # print(e.get_location(i))
        print(e.get_message(i))
