import griddb_python as griddb
from griddb_connector import GridDB  
from datetime import datetime
import pyarrow as pa
import pandas as pd
import sys

def queryContainer(store, containerName):
    """Queries and prints the contents of a given container."""
    try:
        col = store.get_container(containerName)
        query = col.query("SELECT *")
        rs = query.fetch(False)
        print(f"\n--- Reading from {containerName} ---")
        
        row_count = 0
        while rs.has_next():
            data = rs.next()  
            print(data)
            row_count += 1
            
        if row_count == 0:
            print(f"Container '{containerName}' is empty.")
        print(f"--- Finished {containerName} ---")

    except griddb.GSException as e:
        print(f"Error querying {containerName}:")
        for i in range(e.get_error_stack_size()):
            print(f"  [{i}] {e.get_error_code(i)}: {e.get_message(i)} at {e.get_location(i)}")

def createTimeSeries(store):
    """Creates a sample TimeSeries container."""
    try:
        conInfo = griddb.ContainerInfo(name="SamplePython_timeseries1",
                                     column_info_list=
                                     [["date", griddb.Type.TIMESTAMP],
                                      ["value", griddb.Type.DOUBLE]],
                                     type=griddb.ContainerType.TIME_SERIES)
        
        ts = store.put_container(conInfo)
        print("Successfully created TimeSeries: SamplePython_timeseries1")
    except griddb.GSException as e:
        if "Container already exists" in e.get_message(0):
             print("TimeSeries 'SamplePython_timeseries1' already exists. Skipping creation.")
        else:
            print("Error creating TimeSeries:")
            for i in range(e.get_error_stack_size()):
                print(f"  [{i}] {e.get_error_code(i)}: {e.get_message(i)}")

def putTimeSeries(store):
    """Puts a single row into the TimeSeries container."""
    try:
        ts = store.get_container("SamplePython_timeseries1")
        # Define the row to be inserted
        row = [datetime.strptime("2025-10-01T15:00:00.000Z", "%Y-%m-%dT%H:%M:%S.%f%z"), 10.21]
        ts.put(row)
        print(f"Successfully put row into SamplePython_timeseries1: {row}")

    except griddb.GSException as e:
        print("Error putting TimeSeries data:")
        for i in range(e.get_error_stack_size()):
            print(f"  [{i}] {e.get_error_code(i)}: {e.get_message(i)}")

def arrowMultiPut(store, ra):
    """Demonstrates multi_put using PyArrow and Pandas."""
    try:
        df1 = pd.read_csv("data.csv")

        colInfoL = [["id", griddb.Type.LONG],
                    ["c1", griddb.Type.STRING],
                    ["c2", griddb.Type.BOOL]]

        conInfo = griddb.ContainerInfo("p01", colInfoL,
                                     griddb.ContainerType.COLLECTION, True)
        
        store.drop_container("p01")
        col = store.put_container(conInfo)
        print("Successfully created container: p01")

        rb = pa.record_batch(df1)
        col.multi_put(rb, ra)
        print(f"Successfully multi-put {len(df1)} rows into p01")

        q = col.query("select *")
        q.set_fetch_options(root_allocator=ra)
        rs = q.fetch()
        rb_out = rs.next_record_batch()
        df2 = rb_out.to_pandas()
        
        print("\n--- Data queried back from p01 (Arrow) ---")
        print(df2)
        print("------------------------------------------")

    except FileNotFoundError:
         print("Error: data.csv not found. Skipping arrowMultiPut().")
    except griddb.GSException as e:
        print("Error during arrowMultiPut:")
        for i in range(e.get_error_stack_size()):
            print(f"  [{i}] {e.get_error_code(i)}: {e.get_message(i)}")
    except Exception as e:
        print(f"An unexpected error occurred in arrowMultiPut: {e}")


if __name__ == "__main__":
    conn = None
    store = None
    ra = None
    
    try:
        print("Attempting to connect to GridDB...")
        conn = GridDB()
        store = conn.get_store()
        ra = griddb.RootAllocator(sys.maxsize)

        if not store:
            print("Connection failed. Exiting script.")
            sys.exit(1) 

        createTimeSeries(store)
        putTimeSeries(store)
        queryContainer(store, "SamplePython_timeseries1")
        
        arrowMultiPut(store, ra)
        queryContainer(store, "p01")

        print("\nScript finished successfully.")

    except Exception as e:
        print(f"A critical error occurred in main: {e}")
    
    finally:
        print("Script execution complete.")
