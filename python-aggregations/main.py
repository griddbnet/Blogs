import griddb_python as griddb
from griddb_connector import GridDB
from griddb_sql import GridDBJdbc
from datetime import datetime
import pyarrow as pa
import pandas as pd
import sys


if __name__ == "__main__":
    nosql = None
    store = None
    ra = None
    griddb_jdbc = None
    
    try:
        print("Attempting to connect to GridDB...")
        nosql = GridDB()
        store = nosql.get_store()
        ra = griddb.RootAllocator(sys.maxsize)
        if not store:
            print("Connection failed. Exiting script.")
            sys.exit(1) 

        griddb_jdbc = GridDBJdbc()    
        if griddb_jdbc.conn:
            averages =  griddb_jdbc.calculate_avg()
            nosql.pushAvg(averages)
        

        print("\nScript finished successfully.")

    except Exception as e:
        print(f"A critical error occurred in main: {e}")
    
    finally:
        print("Script execution complete.")

