#!/usr/bin/python

import griddb_python as griddb
import folium
from folium.plugins import MarkerCluster
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
    
    print(circuits_dataframe)

    # Create map object
    world_map= folium.Map()

    # Create marker cluster
    marker_cluster = MarkerCluster().add_to(world_map)

    # Create marker for each coordinates
    for i in range(len(circuits_dataframe)):
            # Get latitude and longitude from data frame
            latitude = circuits_dataframe.iloc[i]['lat']
            longitude = circuits_dataframe.iloc[i]['lng']

            # Set circle radius
            radius=5

            # Create the popup
            popup_text = """Country : {}<br> Circuit Name : {}<br>"""
            popup_text = popup_text.format(
                                            circuits_dataframe.iloc[i]['country'],
                                            circuits_dataframe.iloc[i]['name'])
            test = folium.Html(popup_text, script=True)
            popup = folium.Popup(test, max_width=250,min_width=250)

            # Add marker to marker_cluster
            folium.CircleMarker(location=[latitude, longitude], radius=radius, popup=popup, fill =True).add_to(marker_cluster)

    # View the Map
    world_map

except griddb.GSException as e:
    for i in range(e.get_error_stack_size()):
        print("[", i, "]")
        print(e.get_error_code(i))
        print(e.get_location(i))
        print(e.get_message(i))