import griddb_python as griddb
import sys
import pandas as pd

factory = griddb.StoreFactory.get_instance()

argv = sys.argv

try:
    
    
    apps_with_duplicates = pd.read_csv('datasets/apps.csv', index_col=0)
    
    apps = apps_with_duplicates.drop_duplicates()
    apps = apps.drop(['Genres', 'Content Rating', 'Last Updated', 'Current Ver', 'Android Ver'], axis = 1)
    apps= apps.dropna(inplace=True)
    apps.reset_index(drop=True, inplace=True)
    apps.index.name = 'ID'
    
        # List of characters to remove
    chars_to_remove = ['+', ',', 'M', '$']
    
    # List of column names to clean
    cols_to_clean = ['Installs', 'Size', 'Price']
    
    
    
    # Loop for each column
    for col in cols_to_clean:
        # Replace each character with an empty string
        for char in chars_to_remove:
            #print(col)
            #print(char)
            apps[col] = apps[col].astype(str).str.replace(char, '')
        apps[col] = pd.to_numeric(apps[col]) 
    
    apps.to_csv("apps_processed.csv")

    #read the cleaned data from csv
    apps = pd.read_csv("apps_processed.csv")
    

    for row in apps.itertuples(index=False):
            print(f"{row}")

    # View the structure of the data frames
    apps.info()

    # Provide the necessary arguments
    gridstore = factory.get_store(
        host=argv[1], 
        port=int(argv[2]), 
        cluster_name=argv[3], 
        username=argv[4], 
        password=argv[5]
    )

    #Create container 
    app_container = "app_container3200"

    # Create containerInfo
    app_containerInfo = griddb.ContainerInfo(app_container,
                    [["ID", griddb.Type.INTEGER],
        		    ["App", griddb.Type.STRING],
         		    ["Category", griddb.Type.STRING],
                    ["Rating", griddb.Type.FLOAT],
                    ["Reviews", griddb.Type.INTEGER],
         		    ["Size", griddb.Type.FLOAT],
                    ["Installs", griddb.Type.INTEGER],
                    ["Type", griddb.Type.STRING],
                    ["Price", griddb.Type.FLOAT]],
                    griddb.ContainerType.COLLECTION, True)
    
    app_columns = gridstore.put_container(app_containerInfo)
    
    # Put rows
    app_columns.put_rows(apps)
    
    print("App Data Inserted using the DataFrame")

except griddb.GSException as e:
    print(e)
    for i in range(e.get_error_stack_size()):
        print(e)
        # print("[", i, "]")
        # print(e.get_error_code(i))
        # print(e.get_location(i))
        print(e.get_message(i))
