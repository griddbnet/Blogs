#!/usr/bin/python

import griddb_python as griddb
import pandas as pd

factory = griddb.StoreFactory.get_instance()

try:
    # Create gridstore object
    gridstore = factory.get_store(
        host='239.0.0.1', 
        port=31999, 
        cluster_name='defaultCluster', 
        username='admin', 
        password='admin'
    )

    # Define the container name
    races_container = "races"

    # Get the container
    races_data = gridstore.get_container(races_container)

    # Select all rows from races_container
    query = races_data.query("select *")
    rs = query.fetch()

    # Create a list
    retrieved_data= []
    while rs.has_next():
        data = rs.next()
        retrieved_data.append(data)

    # Convert the list to a pandas data frame
    races_dataframe = pd.DataFrame(retrieved_data, columns=['raceID', 'year', 'round', 'circuitId', 'name', 'url', 'raceTime'])

    # Print the count
    print(races_dataframe['name'].value_counts())
    
    # Obtain the count of each venue
    plotdata = races_dataframe['name'].value_counts()
    
    # Plot the data as a bar chart
    plotdata.plot(kind="bar", figsize=(20,10), title="Race Locations", xlabel="Races", ylabel="No of Races")

except griddb.GSException as e:
    for i in range(e.get_error_stack_size()):
        print("[", i, "]")
        print(e.get_error_code(i))
        print(e.get_location(i))
        print(e.get_message(i))