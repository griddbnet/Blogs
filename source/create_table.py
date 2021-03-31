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

    # Create the HTML Page Structure
    # Include the Style Sheet
    html_string = '''
    <html>
        <head><title>Races Collection</title></head>
        <link rel="stylesheet" type="text/css" href="style.css"/>
        <body>
        <h1>Races Collection</h1>
        {table}
        </body>
    </html>
    '''

    # Rename the Columns Headers
    races_dataframe.rename(columns={'raceId': 'Race ID', 'circuitId': 'Circuit ID', 'raceTime': 'Race Time'}, inplace=True)

    # Convert the Headers to Uppercase
    races_dataframe.rename(str.upper, axis='columns', inplace=True)

    # Write to HTML File
    with open('races_table.html', 'w') as f:
        # Pass the DataFrame to the HTML String
        f.write(html_string.format(table=races_dataframe.to_html(index=False, render_links=True, escape=False, justify='left', classes='htmlfilestyle')))

except griddb.GSException as e:
    for i in range(e.get_error_stack_size()):
        print("[", i, "]")
        print(e.get_error_code(i))
        print(e.get_location(i))
        print(e.get_message(i))