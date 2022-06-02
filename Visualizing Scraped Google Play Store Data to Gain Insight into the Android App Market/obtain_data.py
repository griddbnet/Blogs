
import numpy as np
import griddb_python as griddb
import sys
import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns

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
    appdata_container = "appdata_container"

    # Get the containers
    obtained_appdata = gridstore.get_container(appdata_container)
    
    # Fetch all rows
    query = obtained_appdata.query("select *")
    
    rs = query.fetch(False)
    print(f"{appdata_container} Data")

    
    # Iterate and create a list
    retrieved_data= []
    while rs.has_next():
        data = rs.next()
        retrieved_data.append(data)

    # Convert the list to a pandas data frame
    apps = pd.DataFrame(retrieved_data,
                        columns=["ID","App","Category","Rating","Reviews","Size","Installs","Type","Price"])

    # Get the data frame details
    print(apps)
    apps.info()

    
    
except griddb.GSException as e:
    for i in range(e.get_error_stack_size()):
        print("[", i, "]")
        print(e.get_error_code(i))
        print(e.get_location(i))
        print(e.get_message(i))
 
##Analysis

# Print the total number of apps
print('Total number of apps in the dataset = ', len(apps))

Expensiveapp = apps.iloc[apps['Price'].idxmax()]

# Print the total number of unique categories
num_categories = len(apps['Category'].unique())
print('Number of categories = ', num_categories)

avg_app_rating = apps['Rating'].mean()
print('Average app rating = ', avg_app_rating)

# Count the number of apps in each 'Category' and sort them in descending order
num_apps_in_category = apps['Category'].value_counts().sort_values(ascending = False)

num_apps_in_category.plot.bar()
plt.show()

type_installs = apps[["Installs","Type"]].groupby(by = "Type").mean()
sns.barplot(data=type_installs, x=type_installs.index , y = "Installs" )

# Select a few popular app categories
popular_app_cats = apps[apps.Category.isin(['GAME', 'FAMILY', 'PHOTOGRAPHY',
                                            'MEDICAL', 'TOOLS', 'FINANCE',
                                            'LIFESTYLE','BUSINESS'])]

# Select apps priced below $100
apps_under_100 = popular_app_cats[popular_app_cats['Price']<100]

fig, ax = plt.subplots()
fig.set_size_inches(15, 8)

# Examine price vs category with the authentic apps (apps_under_100)
ax = sns.stripplot(x='Price', y='Category', data=apps_under_100,
                   jitter=True, linewidth=1)
ax.set_title('App pricing trend across categories after filtering for outliers')
plt.show()