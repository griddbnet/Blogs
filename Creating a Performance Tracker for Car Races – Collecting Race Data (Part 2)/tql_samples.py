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
    circuits_container = "circuits"
    races_container = "races"

    # Get the containers
    circuits_data = gridstore.get_container(circuits_container)
    races_data = gridstore.get_container(races_container)


    #=======================================================#
    # CIRCUITS DATA TQL
    #=======================================================#

    # Select all rows from circuits_container
    query = circuits_data.query("select *")
    rs = query.fetch(False)
    
    # Create a list
    retrieved_data= []
    while rs.has_next():
        data = rs.next()
        retrieved_data.append(data)

    print(retrieved_data)

    query = circuits_data.query("select * where country = 'Germany'")
    rs = query.fetch(False)
    
    # Create a list
    retrieved_data= []
    while rs.has_next():
        data = rs.next()
        retrieved_data.append(data)

    print(retrieved_data)

    query = circuits_data.query("select COUNT(*)")
    rs = query.fetch(False)
    
    # Create a list
    while rs.has_next():
        data = rs.next()
        count = data.get(griddb.Type.LONG)
        print(count)

    query = circuits_data.query("select * where country = 'UK' OR country = 'France' OR country = 'Spain' ORDER BY name DESC")
    rs = query.fetch(False)
    
    # Create a list
    retrieved_data= []
    while rs.has_next():
        data = rs.next()
        retrieved_data.append(data)

    print(retrieved_data)

    query = circuits_data.query("select * where name LIKE '%Park%' OR name LIKE '%Speedway%' ORDER BY name DESC")
    rs = query.fetch(False)
    
    # Create a list
    retrieved_data= []
    while rs.has_next():
        data = rs.next()
        retrieved_data.append(data)

    print(retrieved_data)

    #=======================================================#
    # RACES DATA TQL
    #=======================================================#

    query = races_data.query("select * where name = 'Monaco Grand Prix'")
    rs = query.fetch()

    # Create a list
    retrieved_data= []
    while rs.has_next():
        data = rs.next()
        retrieved_data.append(data)

    print(retrieved_data)

    query = races_data.query("select * where year >= 2009 AND year <= 2015 ORDER BY raceTime DESC")
    rs = query.fetch()

    # Create a list
    retrieved_data= []
    while rs.has_next():
        data = rs.next()
        retrieved_data.append(data)

    print(retrieved_data)

    query = races_data.query("select * where raceTime >= TIMESTAMP('2009-01-01T00:00:00.000Z') AND raceTime <= TIMESTAMP('2015-12-31T23:59:59.999Z')")
    rs = query.fetch()

    # Create a list
    retrieved_data= []
    while rs.has_next():
        data = rs.next()
        retrieved_data.append(data)

    print(retrieved_data)


    query = races_data.query("select * where raceTime < TIMESTAMPADD(YEAR, NOW(), -10)")
    rs = query.fetch()

    # Create a list
    retrieved_data= []
    while rs.has_next():
        data = rs.next()
        retrieved_data.append(data)

    print(retrieved_data)


    query = races_data.query("select COUNT(*) where year = 2009")
    #query = circuits_data.query("select COUNT(*)")

    # Obtain the calculated value
    rs = query.fetch(False)
    
    while rs.has_next():
        data = rs.next()
        count = data.get(griddb.Type.LONG)
        print(count)


    query = races_data.query("select * LIMIT 100")
    rs = query.fetch()

    # Create a list
    retrieved_data= []
    while rs.has_next():
        data = rs.next()
        retrieved_data.append(data)

    print(retrieved_data)


except griddb.GSException as e:
    for i in range(e.get_error_stack_size()):
        print("[", i, "]")
        print(e.get_error_code(i))
        print(e.get_location(i))
        print(e.get_message(i))