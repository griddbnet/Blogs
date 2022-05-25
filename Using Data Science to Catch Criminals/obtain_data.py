import numpy as np
import griddb_python as griddb
import sys
import pandas as pd
import matplotlib.pyplot as plt
from sklearn.cluster import KMeans


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
    incident_container = "incident_container"

    # Get the containers
    obtained_data = gridstore.get_container(incident_container)
    
    # Fetch all rows - language_tag_container
    query = obtained_data.query("select *")
    
    rs = query.fetch(False)
    print(f"{incident_container} Data")

    
    # Iterate and create a list
    retrieved_data= []
    while rs.has_next():
        data = rs.next()
        retrieved_data.append(data)

    # Convert the list to a pandas data frame
    incidents = pd.DataFrame(retrieved_data,
                        columns=['ID', 'Category', 'Descript', 'DayOfWeek', 'Date', 
                                 'PdDistrict', 'Resolution','Address'])

    # Get the data frame details
    print(incidents)
    incidents.info()
    
    # Define the container names
    call_container = "call_container"

    # Get the containers
    obtained_data = gridstore.get_container(call_container)
    
    # Fetch all rows - language_tag_container
    query = obtained_data.query("select *")
    
    rs = query.fetch(False)
    print(f"{call_container} Data")

    
    # Iterate and create a list
    retrieved_data= []
    while rs.has_next():
        data = rs.next()
        retrieved_data.append(data)

    # Convert the list to a pandas data frame
    calls = pd.DataFrame(retrieved_data,
                        columns=['ID', 'Descript', 'Date'])

    # Get the data frame details
    print(calls)
    calls.info()
    
    
except griddb.GSException as e:
    for i in range(e.get_error_stack_size()):
        print("[", i, "]")
        print(e.get_error_code(i))
        print(e.get_location(i))
        print(e.get_message(i))
        
incidents['NumOfIncidents'] = np.zeros(len(incidents))
calls['NumOfCalls'] = np.zeros(len(calls))

incident_categories = incidents.groupby(["Category"]).count()
n_largest = incident_categories['NumOfIncidents'].nlargest(n=10)
incident_categories.reset_index(inplace = True)
incident_categories = incident_categories[["Category","NumOfIncidents"]]
n_largest.plot(kind = 'barh')
plt.show()


Theft_address = incidents[incidents['Category']=="LARCENY/THEFT"]
Theft_address = Theft_address.groupby(["Address"]).count()
n_largest = Theft_address['NumOfIncidents'].nlargest(n=10)
Theft_address.reset_index(inplace = True)
Theft_address = Theft_address[["Address","NumOfIncidents"]]
n_largest.plot(kind = 'barh')
plt.show()

daily_incidents = incidents.groupby(["Date"]).count()
daily_incidents.reset_index(inplace = True)
daily_incidents = daily_incidents[["Date","NumOfIncidents"]]


daily_calls = calls.groupby(["Date"]).count()
daily_calls.reset_index(inplace = True)
daily_calls = daily_calls[["Date","NumOfCalls"]]

shared_dates = pd.merge(daily_incidents, daily_calls, on='Date', how = 'inner')
shared_dates.reset_index()

#shared_dates.plot(y='NumOfCalls')
shared_dates['Day'] = shared_dates.index

d1 = np.polyfit(shared_dates.index,shared_dates['NumOfCalls'],1)
f1 = np.poly1d(d1)
shared_dates.insert(3,'Treg1',f1(shared_dates.index))
ax = shared_dates.plot.scatter(x = 'Day', y='NumOfCalls')
shared_dates.plot(y='Treg1',color='Red',ax=ax)

d2 = np.polyfit(shared_dates.index,shared_dates['NumOfIncidents'],1)
f2 = np.poly1d(d2)
shared_dates.insert(4,'Treg2',f2(shared_dates.index))
ax = shared_dates.plot.scatter(x='Day' ,y='NumOfIncidents')
shared_dates.plot(y='Treg2',color='Red',ax=ax)

correlation = shared_dates['NumOfIncidents'].corr(shared_dates['NumOfCalls'])