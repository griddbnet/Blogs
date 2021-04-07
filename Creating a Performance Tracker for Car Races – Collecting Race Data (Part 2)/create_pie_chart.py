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
    circuits_container = "circuits"

    # Get the container
    circuits_data = gridstore.get_container(circuits_container)
    
    # Fetch all rows - circuits_container
    query = circuits_data.query("select *")
    rs = query.fetch(False)
    
    # Iterate and create a list
    retrieved_data= []
    while rs.has_next():
        data = rs.next()
        retrieved_data.append(data)
   
    # Convert the list to a pandas data frame
    circuits_dataframe = pd.DataFrame(retrieved_data, columns=['circuitId', 'circuitRef', 'name', 'location', 'country', 'lat', 'lng', 'url'])

    # Print the count
    print(circuits_dataframe['country'].value_counts())

    # Obtain the count of each venue
    plotdata = circuits_dataframe['country'].value_counts()

    # Plot the data as a pie chart
    # Disable the legend and ylabel
    plotdata.plot(kind="pie", figsize=(30,15), title="Circuit Locations", legend=False, ylabel="")

except griddb.GSException as e:
    for i in range(e.get_error_stack_size()):
        print("[", i, "]")
        print(e.get_error_code(i))
        print(e.get_location(i))
        print(e.get_message(i))