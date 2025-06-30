The GridDB Python client has been updated to now use the native GridDB Java interface with [JPype](https://pypi.org/project/jpype1/) and [Apache Arrow](https://pypi.org/project/pyarrow/). Prior to this release, the python client relied on the c_client, and translated some of those commands with swig and other tools. 

The main benefit of committing to this change in underlying technology is being able to query GridDB and get back an Apache Arrow recordbatch object in return. We will go over how this change can directly affect your python workflows with a concrete example later on in this article.

Another benefit is how SQL is now handled. With the c_client as the base, you could query the database only using TQL, but not SQL, meaning that certain partitioned tables were simply not accessible to your python client. There were workarounds, for example: [Pandas with Python GridDB SQL Queries](https://griddb.net/en/blog/pandas-with-python-griddb-sql-queries/), but now with this new client, this sort of thing will work out of the box.

So with that out of the way, let's see how we can install the new GridDB Python Client and explore some of the changes in this new version

## Installation And Prereqs

To install, as explained briefly above, you will need to have Java installed and set to your environment variable `JAVA_HOME`. You will also need `maven` and `python3.12`

Here is how I installed these packages and set the Java home on Ubuntu 22.04:

```bash
$ sudo apt install maven python3.12 default-jdk
$ export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
```


### Installation 


To grab the source code of the python client, navigate to its [github](https://github.com/griddb/python_client) page and clone the repo. Once cloned, we can run a maven install to build our apache library and then install the python client.


```bash
git clone https://github.com/griddb/python_client.git
cd python_client/java
mvn install
cd ..
cd python
python3.12 -m pip install .
cd .. #this puts you back in the root directory of python_client
```

### Jar Files and Your Environment Variables

On top of having the JAVA HOME set and having Java installed, there are a couple of `.jar` files you will need to be in your `CLASSPATH` as well. Specifically we need `pyarrow`, `gridstore` and in some cases, `py-memory-netty`. Two of these three we can just download generic versions from the maven repository, but Apache Arrow will rely on a modified version which we built in the previous step. So let's download and set these jar files. 

```bash
$ mkdir lib && cd lib
$ curl -L -o gridstore.jar https://repo1.maven.org/maven2/com/github/griddb/gridstore/5.8.0/gridstore-5.8.0.jar
$ curl -L -o arrow-memory-netty.jar https://repo1.maven.org/maven2/org/apache/arrow/arrow-memory-netty/18.3.0/arrow-memory-netty-18.3.0.jar
$ cp ../java/target/gridstore-arrow-5.8.0.jar gridstore-arrow.jar
$ export CLASSPATH=$CLASSPATH:./gridstore.jar:./gridstore-arrow.jar:./arrow-memory-netty.jar
```

If you are unsure if your CLASSPATH is set, you can always run an echo:

```bash
$ echo $CLASSPATH
:./gridstore.jar:./gridstore-arrow.jar:./arrow-memory-netty.jar
```

With this set in your CLASSPATH, you can start your python3 griddb scripts without explicity setting the classpath options when starting the JVM at the top of the file. If you don't set the CLASSPATH, you can use the option like the sample code does: 

```python
import jpype

# If no CLASSPATH Set in the environment, you can force the JVM to start with these jars explicitly
jpype.startJVM(classpath=["./gridstore.jar", "./gridstore-arrow.jar", "./arrow-memory-netty.jar"])
import griddb_python as griddb
import sys
```

or if you set the CLASSPATH and make this permanent (for example, editing your `.bashrc file`), you can get away with not importing jpype at all as the modified pyarrow will do it for you.

```python

# No jpype set here; still works
import griddb_python as griddb
import sys
```

## Running Samples

To ensure everything is working properly, we should try running the sample code. Navigate to the sample dir, make some changes, and then run!

```bash 
# from the root of this python client repo
$ cd sample
```

We will need to change the connection details as GridDB CE runs mostly in FIXED_LIST mode now, meaning we need a notification member, not a host/port combo: 

```python
try:
	#Get GridStore object
    # Changed here to notification_member vs port & address
	gridstore = factory.get_store(notification_member=argv[1], cluster_name=argv[2], username=argv[3], password=argv[4])
```

And depending on you have your CLASSPATH variables set, you can either add `arrow-memory-netty` to your jypype start jvm method, or set your classpath as explained. The code should now run just fine: 

```bash
python_client/sample$ python3.12 sample1.py 127.0.0.1:10001 myCluster admin admin
Person: name=name02 status=False count=2 lob=[65, 66, 67, 68, 69, 70, 71, 72, 73, 74]
```

## API Differences & Usage

The README for the Python client page explains what features are still currently missing (when compared to the previous v.0.8.5): 

    - Array type for GridDB
    - Timeseries-specific function
    - Implicit data type conversion

But there is also some functionality that is gained with this move: 

    - Compsite RowKey, Composite Index GEOMETRY type and TIMESTAMP(micro/nano-second) type 
    - Put/Get/Fetch with Apache Arrow 
    - Operations for Partitioning table

### Using SQL

These new features basically come from the ability to use Java and then by extension JDBC and SQL. 

To use SQL, you can simply add the [GridDB JDBC jar](https://github.com/griddb/jdbc) to your `CLASSPATH` (or jvm start options). From there, you can use SQL and use them on partition tables. Taken from the samples, here's what SQL can look like: 

```python
import jpype
import jpype.dbapi2
jpype.startJVM(classpath=["./gridstore.jar", "./gridstore-arrow.jar", "./gridstore-jdbc.jar"])
import griddb_python as griddb
import sys

### SQL create table/insert

url = "jdbc:gs://127.0.0.1:20001/myCluster/public"
conn = jpype.dbapi2.connect(url, driver="com.toshiba.mwcloud.gs.sql.Driver",
	driver_args={"user":"admin", "password":"admin"})

curs = conn.cursor()

curs.execute("DROP TABLE IF EXISTS Sample")
curs.execute("CREATE TABLE IF NOT EXISTS Sample ( id integer PRIMARY KEY, value string )")
print('SQL Create Table name=Sample')

curs.execute("INSERT INTO Sample values (0, 'test0'),(1, 'test1'),(2, 'test2'),(3, 'test3'),(4, 'test4')")
print('SQL Insert')
```

For the most part, this stuff is the same as before as we could always start up the jpype jvm with python and run JDBC. What is truly new is using Apache Arrow.

### Using Apache Arrow with GridDB/Python/Nodejs

Part of what makes Arrow so useful in the modern era is its ability to "[allow] for zero-copy reads and fast data access and interchange without serialization overhead between these languages and systems."(https://en.wikipedia.org/wiki/Apache_Arrow). To showcase this, we will create a python script to generate 10000 rows of 'random' data, then we will query the result directly in an [Arrow RecordBatch object](https://arrow.apache.org/docs/python/generated/pyarrow.RecordBatch.html), and once that obj exists, we will stream it over tcp (without serialzing/deserializing) directly over to nodejs (so called Zero-Copying).

First, let's generate 10000 rows of data. We will create a timeseries container with 'random' data in all of the columns.

```python
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
```

Next let's insert with multiput. First we'll format the list of rows into a dataframe. Then we'll convert the dataframe into a recordbatch and then use multiput to insert the batch into GridDB: 

```python
df = pd.DataFrame(rows, columns=["ts", "name", "status", "count", "lob"])
col = gridstore.put_container(conInfo)
rb = pa.record_batch(df)
col.multi_put(rb, ra)
```

Now that our data is inside GridDB, let's query it (this is for the sake of education, obviously this makes no sense in the 'real world').

```python
col = gridstore.get_container("col01")
q = col.query("select *")
q.set_fetch_options(root_allocator=ra)
rs = q.fetch()
result = []
rb = rs.next_record_batch() #gets all of the rows as a recordbatch obj
```

And now finally, let's stream our record batch over to some other programming environment to showcase Apache Arrow's supreme flexibility. We will use nodejs as the consumer here.

```python
#stream our rows through socket

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
```

Run the python script and it will create the container, add the rows of data, query those rows of data, and then start a server which is listening for a consumer to connect to it, which will then send all of the rows to the consumer.

Nodejs will now connect to our producer and print out the records

```javascript
const net = require('net');
const { RecordBatchReader } = require('apache-arrow');

const HOST = '127.0.0.1';
// Ensure this port matches your Python producer's port
const PORT = 2828;

const client = new net.Socket();

client.connect(PORT, HOST, async () => {
    console.log(`Connected to Python producer at ${HOST}:${PORT}`);

    try {
        const reader = await RecordBatchReader.from(client);

        let schemaPrinted = false;

        for await (const recordBatch of reader) {
            if (!schemaPrinted) {
                console.log("Successfully parsed schema from stream.");
                console.log(`Schema:`, reader.schema.fields.map(f => `${f.name}: ${f.type}`).join(', '));
                console.log("--- Processing data batches ---");
                schemaPrinted = true;
            }

            // Convert the record batch to a more familiar JavaScript object format
            const data = recordBatch.toArray().map(row => row.toJSON());
            console.log("Received data batch:", data);
        }

        console.log("-------------------------------");
        console.log("Stream finished.");

    } catch (error) {
        console.error("Error processing Arrow stream:", error);
    }
});

client.on('close', () => {
    console.log('Connection closed');
});

client.on('error', (err) => {
    console.error('Connection error:', err.message);
});
```

Run this nodejs script like so:

```bash
$ npm install
$ node consumer.js

Connected to Python producer at 127.0.0.1:2828
Successfully parsed schema from stream.
Schema: ts: Timestamp<MILLISECOND>, name: Utf8, status: Bool, count: Int64, lob: Binary
--- Processing data batches ---
Received data batch: [
  {
    ts: 1733047200000,
    name: '65d16ce6-55d3-11f0-8070-8bbd0177d9e6',
    status: false,
    count: 820633n,
    lob: Uint8Array(10) [
      65, 66, 67, 68, 69,
      70, 71, 72, 73, 74
    ]
  },
  {
    ts: 1733047500000,
    name: '65d16ce7-55d3-11f0-8070-8bbd0177d9e6',
    status: false,
    count: 931837n,
    lob: Uint8Array(10) [
      65, 66, 67, 68, 69,
      70, 71, 72, 73, 74
    ]
  },

  ....cutoff
  ```

  ## Conclusion

  And with that, we have successfully showcased the new GridDB Python client!