#!/usr/bin/python

import griddb_python as griddb
import pandas as pd
import matplotlib.pyplot as plt

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
    query = circuits_data.query("select * LIMIT 10")
    rs = query.fetch(False)
    
    # Iterate and create a list
    retrieved_data= []
    while rs.has_next():
        data = rs.next()
        retrieved_data.append(data)
   
    # Convert the list to a pandas data frame
    circuits_dataframe = pd.DataFrame(retrieved_data, columns=['circuitId', 'circuitRef', 'name', 'location', 'country', 'lat', 'lng', 'url'])

    # Create the plot object
    fig, ax =plt.subplots(
        figsize=(20, 6),
        linewidth=2,
        tight_layout={'pad':1})

    # Turn off the axis
    ax.axis('off')
    # Set background color
    ax.set_facecolor("#ffffff")
    
    table = ax.table(
        # Define cell values
        cellText=circuits_dataframe.values,
        # Define column headings
        colLabels=circuits_dataframe.columns,
        # Column Options - color and width
        colColours=['skyblue']*8,
        colWidths=[0.03,0.06,0.1,0.05,0.04,0.06,0.06,0.135],
        loc="center")

    # Set the font size
    table.auto_set_font_size(False)
    table.set_fontsize(10)

    # Scale the table
    table.scale(1.8, 1.8)

    # Add title
    plt.suptitle("Circuits Collection", size=25, weight='light')

    # Add footer
    plt.figtext(0.95, 0.05, "GridDB Data Collection", horizontalalignment='right', size=6, weight='light')

    # Draw and save the table as a png
    plt.draw()
    plt.savefig('table.png',dpi=150)

except griddb.GSException as e:
    for i in range(e.get_error_stack_size()):
        print("[", i, "]")
        print(e.get_error_code(i))
        print(e.get_location(i))
        print(e.get_message(i))