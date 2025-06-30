from datetime import datetime, timezone, timedelta
import griddb_python as griddb
import sys
import pandas as pd
import pyarrow as pa
import uuid
import random
import socket
import warnings
warnings.filterwarnings('ignore')


def generate_random_timestamps(start_date_str, num_timestamps, min_interval_minutes=5, max_interval_minutes=30):
    date_format = "%Y-%m-%dT%H:%M:%S"
    
    current_time = datetime.fromisoformat(start_date_str.replace("Z", "")).replace(tzinfo=timezone.utc)
    
    timestamp_list = []

    for _ in range(num_timestamps):
        timestamp_str = current_time.strftime(date_format) + ".000Z"
        timestamp_list.append(timestamp_str)

        random_minutes = random.randint(min_interval_minutes, max_interval_minutes)
        current_time += timedelta(minutes=random_minutes)

    return timestamp_list


start_point = "2024-12-01T10:00:00.000Z"
number_of_stamps = 10000     
min_interval = 5           
max_interval = 20          

generated_datelist = generate_random_timestamps(
    start_point, 
    number_of_stamps, 
    min_interval, 
    max_interval
)

factory = griddb.StoreFactory.get_instance()

gridstore = factory.get_store(
    notification_member="127.0.0.1:10001",
    cluster_name="myCluster",
    username="admin",
    password="admin"
)

col = gridstore.get_container("col01")

ra = griddb.RootAllocator(sys.maxsize)

blob = bytearray([65, 66, 67, 68, 69, 70, 71, 72, 73, 74])
conInfo = griddb.ContainerInfo("col01",
    [["ts", griddb.Type.TIMESTAMP],
    ["name", griddb.Type.STRING],
    ["status", griddb.Type.BOOL],
    ["count", griddb.Type.LONG],
    ["lob", griddb.Type.BLOB]],
    griddb.ContainerType.TIME_SERIES, True)

i=0
rows=[]
while i < 10000:
    rows.append([datetime.strptime(generated_datelist[i], "%Y-%m-%dT%H:%M:%S.%f%z"),str(uuid.uuid1()), False, random.randint(0, 1048576), blob])
    i=i+1


df = pd.DataFrame(rows, columns=["ts", "name", "status", "count", "lob"])

gridstore.drop_container("col01")

start = datetime.now(timezone.utc)
col = gridstore.put_container(conInfo)
rb = pa.record_batch(df)
col.multi_put(rb, ra)

print("multiput with a dataframe turned into a pyarrow record batch took "+  str(datetime.now(timezone.utc)  - start) +" seconds")


start = datetime.now(timezone.utc)
q = col.query("select *")
q.set_fetch_options(root_allocator=ra)
rs = q.fetch()
result = []
rb = rs.next_record_batch()
print("reading pyarrow took: "+  str(datetime.now(timezone.utc) - start) +" seconds")

HOST = '127.0.0.1'
PORT = 2828

with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as server_socket:
    server_socket.bind((HOST, PORT))
    server_socket.listen(1)
    print(f"Python producer listening on {HOST}:{PORT}")
    conn, addr = server_socket.accept()
    print(f"Connected by {addr}")

    with conn:
        with conn.makefile(mode='wb') as f:
            # Use the file-like object as the sink for the stream writer
            with pa.ipc.new_stream(f, rb.schema) as writer:
                writer.write_batch(rb)
