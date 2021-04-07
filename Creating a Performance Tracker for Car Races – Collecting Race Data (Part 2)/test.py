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

    # Define the container names
    circuits_container = "circuits-test001"
    races_container = "races-test001"

    # Get the containers
    circuits_data = gridstore.get_container(circuits_container)
    races_data = gridstore.get_container(races_container)

    # Select all rows from circuits_container
    query = circuits_data.query("select COUNT(*)")
    rs = query.fetch(False)
    
    # Create a list
    retrieved_data= []
    while rs.has_next():
        data = rs.next()
        count = data.get(griddb.Type.LONG)
        print(count)

except griddb.GSException as e:
    for i in range(e.get_error_stack_size()):
        print("[", i, "]")
        print(e.get_error_code(i))
        print(e.get_location(i))
        print(e.get_message(i))