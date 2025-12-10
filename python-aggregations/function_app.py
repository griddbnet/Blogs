import logging
import azure.functions as func
import griddb_python as griddb
from griddb_connector import GridDB
from griddb_sql import GridDBJdbc
from datetime import datetime
import pyarrow as pa
import pandas as pd
import sys

app = func.FunctionApp()

@app.timer_trigger(schedule="0 0 * * * *", arg_name="myTimer", run_on_startup=True,
              use_monitor=False) 
def aggregations(myTimer: func.TimerRequest) -> None:
    if myTimer.past_due:
        logging.info('The timer is past due!')

    logging.info('Python timer trigger function executed.')
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