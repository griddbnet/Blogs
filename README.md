Apache Kafka is a tool which allows for "real-time processing of record streams". What this means is that you can send real-time data from your sensors or various other pieces of tools directly into something else, and in this case, directly into GridDB. You can also do the opposite: you can stream data from your GridDB containers directly into some other tool like logging analytics or various other tools.

We have written before about using Apache Kafka to load real-time data directly into your GridDB server using a JDBC connection. You can read our previous blogs with the following links: [Using Kafka With GridDB][1] & [Using GridDB as a source for Kafka with JDBC][2].

For this article, we will again be using Kafka in conjunction with GridDB with the newly released GridDB Kafka Connector. In our previous articles, we had been marrying GridDB and Kafka via JDBC, because with the help of the connector, JDBC is no longer a piece of the equation. There are two pieces which effectively connect GridDB with Kafka: the source and the sink. The GridDB Kafka sink connector pushes data from Apache Kafka topics and persists the data to GridDB database tables. And the source connector works in the opposite fashion, pulling data from GridDB and putting it into Kafka topics.

To showcase this, we will be showing off both types of connector, that is, pushing data from our kafka "topics" directly into our running GridDB server (Sink) and then vice-versa (Source).

And because it does get a bit confusing trying to follow along with this written content, I will say up front that you will eventually need to have 5 different terminals open: 

- Terminal 1: For GridDB gs_sh (to verify GridDB operations)
- Terminal 2 & 3: for running Kafka Zookeper and Kafka server
- Terminal 4: for running GridDB Kafka sink and source connector
- Terminal 5: for running and reading the script, and checking data

## Installing and Setting up Environment/Terminals

Continuing on with this article, we will install all prereqs and get the proper servers/scripts running, create our Kafka ["topics"](https://kafka.apache.org/intro#intro_concepts_and_terms), push and save the data to our GridDB server (Sink Connector), do a live showcase, and then finally pull data from GridDB directly into our Kafka topics (Source Connector)

### Preqs

To follow along, please have the following ready:

*   Java
*   Maven (only if building the GridDB Kafka Connector from source)
*   [Kafka][3]
*   [GridDB][4]

 As another resource, you can also take a look at the source code here: https://github.com/griddbnet/Blogs/tree/kafka. From within this repo, you will have access to the basics (ie. the config files, the data, and the bash script), but will still need to download GridDB, Kafka and the GridDB Kafka connector.

<div class="clipboard">
  <pre><code class="language-sh">$ git clone https://github.com/griddbnet/Blogs.git --branch kafka</code></pre>
</div>

Another thing I would recommend to do is to add the path to your kafka bin directory into your PATH. You can do everytime you open up a  terminal, or you can add it to your profile: `$ vim ~/.bashrc` And add to your path: 

```bash
export PATH=$PATH:/home/israel/kafka_project/kafka_2.13-3.2.1/bin
```
And now you should have the kafka script available whenever you open up a new terminal. And if the worst case it doesn't work, you can force it: `$ source ~/.bashrc`.

### GridDB Kafka Connector (Installation)

Head over to the Connector's repo and clone it: 

<div class="clipboard">
  <pre><code class="language-sh">$ git clone https://github.com/griddb/griddb-kafka-connect.git
$ cd griddb-kafka-connect/</code></pre>
</div>

Once you have downloaded the Kafka connector, we can build the needed `.jar` file or take it directly from the repository shared earlier in this article and move the resulting file into the proper location. To build:

<div class="clipboard">
  <pre><code class="language-sh">$ mvn clean install</code></pre>
</div>

If you don't want to build your own file, you can also grab a copy of the file that has been included with the repository on GitHub.

From there, simply copy over the `.jar` file (`griddb-kafka-connector-0.5.jar`) to your kafka directory `./libs` directory.

### Installing Kafka

To install kafka: 

<div class="clipboard">
  <pre><code class="language-sh">$ wget https://archive.apache.org/dist/kafka/3.2.0/kafka_2.12-3.2.0.tgz
$ tar xzvf kafka_2.12-3.2.0.tgz
$ cd kafka_2.12-3.2.0
$ export PATH=$PATH:$PWD/bin</code></pre>
</div>

A quick note: adding kafka to your PATH environment variable is only done in that specific terminal session; if you open up a second (or third, etc) terminal, you will need to re-export the environment variable or just manually use the enter path to the scripts. Of course, you can also add it your user's `.basrc` or `.bash_profile` file.

### Setting up GridDB Sink Config File

To set up your GridDB configuration, you can copy griddb-sink.properties from the Blogs folder (from the GitHub repository shared earlier) to kafka/config. If you would like to edit this portion manually, read on.

Please edit the config file to enter in your running GridDB's servers credentials as well as the topics we aim to take in via Kafka topics. The file in question is: `griddb-sink.properties`

And now we can enter in our GridDB server information. And because we are running in FIXED_LIST mode, we will edit the notification member as well and remove the host and port. Lastly, let's add in the topics we mean to ingest into our GridDB server:

<div class="clipboard">
  <pre><code class="language-sh">#host=239.0.0.1
#port=31999
cluster.name=myCluster
user=admin
password=admin
notification.member=127.0.0.1:10001
#notification.provider.url=

#topics.regex=csh(.*)
#topics.regex=topic.(.*)
topics=device7,device8,device9,device10

transforms=TimestampConverter
transforms.TimestampConverter.type=org.apache.kafka.connect.transforms.TimestampConverter$Value
transforms.TimestampConverter.format=yyyy-MM-dd hh:mm:ss.SSS
transforms.TimestampConverter.field=ts
transforms.TimestampConverter.target.type=Timestamp</code></pre>
</div>

Here we are explicitly telling our GridDB Sink Connector to look for these exact topics. We of course could simply use regex if we wanted to be more general, but for demo purposes, this is okay.

We also need to change our format for the timestamp to include milliseconds at the end and we need to set our timestamp's column name, in this case `ts`.

Once this file is edited, please make a copy into your `kafka_2.13-3.2.1/config` directory.

### Setting up GridDB Source Config File

If you wanted to do the reverse of what was described (ie. pushing saved GridDB containers out onto Kafka), you will instead use the GridDB source connector. Firstly, you will need to edit the config file: `griddb-source.properties` in GRIDDB_KAFKA_CONNECTOR_FOLDER/config. You will need to change the GridDB connection details as well as the containers/topics. Of course, similiar to the previous config file, the version we used for this article is included with the GitHub repository: [here](https://github.com/griddbnet/Blogs/blob/kafka/griddb-source.properties)

For the containers section, let's change them directly to these large datasets we ingested from [Kaggle][6].

<div class="clipboard">
  <pre><code class="language-sh">containers=device1,device2,device3,device4</code></pre>
</div>

And one of the major differences between these two files (sink vs source) is that the source will need the following parameter (mandatory) `timestamp.column.name`. For this, we set it to our Timestamp row key, which in the device7 is `ts`

<div class="clipboard">
  <pre><code class="language-sh">timestamp.column.name=ts
mode=timestamp</code></pre>
</div>

Note: you could also use `mode=batch` which will not use the `timestamp.column.name` parameter and will loop and update your dataset many times. Whereas the former mode (timestamp) will grab the data just one time ('til the queue is exhausted).

And with that, we should be ready to start our processes/servers.

Again, please make a copy into your `kafka_2.13-3.2.1/config` directory.

## Starting Up Necessary Servers/Systems

Since everything is in place now, let's do a quick recap of our current dir structure. So, if following along, there should be three directories ready for use: 

/home/you/kafka_project/
├─ kafka_2.13-3.2.1/
├─ griddb-kafka-connect/
├─ Blogs/

The first directory (`kafka_2.13-3.2.1`) is the main kafka directory. The 2nd directory (`griddb-kafka-connect/`) is the GridDB kafka connector directory; this directory contains our GridDB-specific config files (either manually edited or taken directly from GitHub, though these files should be copied over to your kafka directory). And the third directory (`Blogs`) is the one built for this blog, it contains the kafka settings which should be copied over to the Kafka directory where applicable. 

Now let's finally get this running.

### Start GridDB Server (Terminal 1)

First and foremost, we will need to run GridDB. To do so, you can follow the documentation: [https://docs.griddb.net/gettingstarted/using-apt/](https://docs.griddb.net/gettingstarted/using-apt/)

Once installed, start GridDB.

    $ sudo systemctl start gridstore

You can keep this terminal open to run `gs_sh` to be able to interact with GridDB through the terminal. To do so:

```bash
$ sudo su gsadm
$ gs_sh
gs> 
```

### Kafka Prep

Next, to get this to work, you will also need to add the kafka directory to your path as mentioned above in the first section where we installed Kafka. Here is that command again:

<div class="clipboard">
  <pre><code class="language-sh">$ export PATH=$PATH:/path/to/kafka_2.13-3.2.1/bin</code></pre>
</div>

### Starting Kafka (Terminal 2 & 3)

Let's start the kafka zookeeper and server with terminal 2. From the `kafka_2.13-3.2.1` directory: 

<div class="clipboard">
  <pre><code class="language-sh">$ zookeeper-server-start.sh config/zookeeper.properties</code></pre>
</div>

And then in another terminal (3) (again, from the `kafka_2.13-3.2.1` directory):

<div class="clipboard">
  <pre><code class="language-sh">$ kafka-server-start.sh config/server.properties</code></pre>
</div>

And now our Kafka is mostly ready to go. To continue, please open up a fourth terminal.

### Run GridDB Kafka Connectors (Terminal 4)

Let's now start up our we can finally start up the GridDB sink and source connectors in the same terminal with one command.

To do so, from the kafka directory, run:

<div class="clipboard">
  <pre><code class="language-sh"> 
$ connect-standalone.sh config/connect-standalone.properties PATH_TO_KAFKA/config/griddb-sink.properties PATH_TO_KAFKA/config/griddb-source.properties</code></pre>
</div>
    

## Using the GridDB Sink Connector

As explained before, the SINK connector pulls data from Kafka topics directly INTO GridDB. Keep that in mind to understand how this section works and then remember it again for the SOURCE Connector section. For this portion, we will go through and do a batch ingestion and then showcase a live ingestion using the SINK.

### Batch Ingestion (Sink Connector)

Now with the sink connector running, let's take a look at batch usage. First, let's get some data going.

####  Setting Up Our Simulated Sensor Data

Because Kafka operates with its topics as its data payloads, we have prepared some sample topics to really ensure you can grasp and understand what is going on. So before we move on, let's create some topics using the included script and .txt file.

#### Creating Kafka Topics (Using Script)

To use the included files for this part of the project, please copy the `script_sink.sh` and `simulate_sensor.txt` from the Blogs folder to the kafka root folder.

In a real world example, our sensors would be individually setting up Kafka topics for our Kafka server to pick up on and send to GridDB, but because this is a demo, we will simply simulate the topic generation portion using a bash script and a `.txt` file.

The following is the content of our `simulate_sensor.txt` file:

<div class="clipboard">
  <pre><code class="language-sh">2020-07-12 00:01:34.735 device7 0.0028400886071015706 76.0 false 0.005114383400977071 false 0.013274836704851536 19.700000762939453
2020-07-12 00:02:02.785 device8 0.0029050147565559603 75.80000305175781 false 0.005198697479294309 false 0.013508733329556249 19.700000762939453
2020-07-12 00:02:11.476 device9 0.0029381156266604295 75.80000305175781 false 0.005241481841731117 false 0.013627521132019194 19.700000762939453
2020-07-12 00:02:15.289 device10 0.0028400886071015706 76.0 false 0.005114383400977071 false 0.013274836704851536 19.700000762939453
2020-07-12 00:02:19.641 device7 0.0028400886071015706 76.0 false 0.005114383400977071 false 0.013274836704851536 19.799999237060547
2020-07-12 00:02:28.818 device8 0.0029050147565559603 75.9000015258789 false 0.005198697479294309 false 0.013508733329556249 19.700000762939453
2020-07-12 00:02:33.172 device9 0.0028400886071015706 76.0 false 0.005114383400977071 false 0.013274836704851536 19.799999237060547
2020-07-12 00:02:39.145 device10 0.002872341154862943 76.0 false 0.005156332935627952 false 0.013391176782176004 19.799999237060547
2020-07-12 00:02:47.256 device7 0.0029050147565559603 75.9000015258789 false 0.005198697479294309 false 0.013508733329556249 19.700000762939453</code></pre>
</div>

This is the data we aim to publish to GridDB.

Next let's take a look at the content of bash script called `script_sink.sh`

<div class="clipboard">
  <pre><code class="language-sh">#!/bin/bash

function echo_payload {
    echo '{"payload": {"ts": "'$1 $2'","sensor": "'$3'","co": '$4',"humidity": '$5',"light": "'$6'","lpg": '$7',"motion": "'$8'","smoke": '$9',"temp": '${10}'},"schema": {"fields": [{"field": "ts","optional": false,"type": "string"},{"field": "sensor","optional": false,"type": "string"},{"field": "co","optional": false,"type": "double"},{"field": "humidity","optional": false,"type": "double"},{"field": "light","optional": false,"type": "boolean"},{"field": "lpg","optional": false,"type": "double"},{"field": "motion","optional": false,"type": "boolean"},{"field": "smoke","optional": false,"type": "double"},{"field": "temp","optional": false,"type": "double"}],"name": "iot","optional": false,"type": "struct"}}'
}

TOPICS=()

for file in `find $1 -name \*simulate_sensor.txt` ; do
    echo $file
    head -10 $file |while read -r line ; do
        SENSOR=`echo ${line} | awk '{ print $3 }'`
        if [[ ! " ${TOPICS[@]} " =~ " ${SENSOR} " ]]; then
            echo Creating topic ${SENSOR}
            kafka-topics.sh --bootstrap-server 127.0.0.1:9092 --create --topic  ${SENSOR} 2&>1 /dev/null
            TOPICS+=(${SENSOR})
        fi
        echo_payload ${line} | kafka-console-producer.sh --topic ${SENSOR} --bootstrap-server localhost:9092
    done
done</code></pre>
</div>

This script will read in our raw data text file and generate our topics with our data and send it to the proper kafka process.

So essentially with one step we are creating the topics (device7, device8, device9, device10) and then also sending some payloads of data into them to play around with them.

Now add the proper permissions to the script file and then we can run the script to feed into our kafka process. This will allow the data to be queued up and available for ingesting once the GridDB sink connector becomes available.

<div class="clipboard">
  <pre><code class="language-sh">$ chmod +x script_sink.sh
$ ./script_sink.sh</code></pre>
</div>

    Creating topic device7
    Creating topic device8
    Creating topic device9
    Creating topic device10



When using Kafka in this manner, we are basically sending payloads to a Kafka topic and letting them build up, and then once Kafka is available (or in this case, we run the Kafka process), it will receive the topics and push it directly to GridDB.

From the large amounts of output this command will generate, you should be able to see something resembling topics being placed into GridDB:

    Put records to GridDB with number records 9 (com.github.griddb.kafka.connect.sink.GriddbSinkTask:54)

A small tip: if a topic ends up malformed and does not allow you to fix it, you can delete a topic to restart the process: 

<div class="clipboard">
  <pre><code class="language-sh">$ kafka-topics.sh --bootstrap-server localhost:9092 --delete --topic device7</code></pre>
</div>

#### Single Usage (Sink Connector)

Next, we will try sending off payloads after topic creation and after we get our GridDB sink running. The goal will be showcasing live payloads being inserted into GridDB. So leave the Sink running and let's try to create a payload to send.

First, if your `/griddb-kafka-connect/config/griddb-sink.properties` matches mine (ie you are using *explicit* container names in the topic section) you will need to update the topic portion. If you wish to add let's say device30 as a topic, you will need to include that into your sink config file and then re-run the connector.

Once your connector knows to look out for the new topic we intend to make, let's actually run the command to make the topic. 

<div class="clipboard">
  <pre><code class="language-sh">kafka-console-producer.sh --topic device30 --bootstrap-server 127.0.0.1:9092</code></pre>
</div>

And then the producer will sit there and listen for new payloads to send. Now we can send a payload and check with our running GridDB Sink to see if it receives the data: 

<div class="clipboard">
  <pre><code class="language-sh">> { "payload": { "ts": "2022-07-12 08:01:34.126", "sensor": "device8", "co": 0.0028400886071015706, "humidity": 76.0, "light": "false", "lpg": 0.005114383400977071, "motion": "false", "smoke": 0.013274836704851536, "temp": 19.700000762939453 }, "schema": { "fields": [ { "field": "ts", "optional": false, "type": "string" }, { "field": "sensor", "optional": false, "type": "string" }, { "field": "co", "optional": false, "type": "double" }, { "field": "humidity", "optional": false, "type": "double" }, { "field": "light", "optional": false, "type": "boolean" }, { "field": "lpg", "optional": false, "type": "double" }, { "field": "motion", "optional": false, "type": "boolean" }, { "field": "smoke", "optional": false, "type": "double" }, { "field": "temp", "optional": false, "type": "double" } ], "name": "iot", "optional": false, "type": "struct" } }</code></pre>
</div>

If you send this, in the running GridDB Sink, it should receive the change to the topic and register it directly to GridDB: 

    [2022-11-18 17:40:07,168] INFO [griddb-kafka-sink|task-0] Put 1 record to buffer of container device30 (com.github.griddb.kafka.connect.sink.GriddbBufferedRecords:75)
    [2022-11-18 17:40:07,169] INFO [griddb-kafka-sink|task-0] Get Container info of container device30 (com.github.griddb.kafka.connect.dialect.GriddbDatabaseDialect:130)
    [2022-11-18 17:40:07,201] INFO [griddb-kafka-sink|task-0] Get Container info of container device30 (com.github.griddb.kafka.connect.dialect.GriddbDatabaseDialect:130)


## Running the GridDB Source Connector

The Source Connector will do the opposite of the Sink Connector -- it will pull data from GridDB and deliver them into our Kafka Topics. For this demo, we already have three rather large GridDB containers (device1, device2, device3) which contain data from [Kaggle](https://www.kaggle.com/datasets/garystafford/environmental-sensor-data-132k) which have been imported from PostgreSQL; you can ingest this data if you follow along in our [previous blog](https://griddb.net/en/blog/using-the-griddb-import-export-tools-to-migrate-from-postgresql-to-griddb/).

If you remember, when we were editing our Source Connector config file, we explicitly stated we wanted to take from those specific containers. The idea is that once we run the Source Connector, Kafka will pull in all relevant data from our GridDB Database into topics. 

#### Batch Usage (Source Connector)

If successful, you will see something like this in the output after running this:

    [2022-09-23 18:51:44,262] INFO [griddb-kafka-source|task-0] Get Container info of container device1 (com.github.griddb.kafka.connect.dialect.GriddbDatabaseDialect:130)
    [2022-09-23 18:51:44,276] INFO [griddb-kafka-source|task-0] Get Container info of container device2 (com.github.griddb.kafka.connect.dialect.GriddbDatabaseDialect:130)
    [2022-09-23 18:51:44,277] INFO [griddb-kafka-source|task-0] Get Container info of container device3 (com.github.griddb.kafka.connect.dialect.GriddbDatabaseDialect:130)
    
We will verify in the READING section, but our Kafka topics should now have these containers and their contents in our Kafka. This is essentially the extent of batch processing using the source; Kafka simply pulls directly from our database.

### Live Ingestion (Source Connector)

Now let's try the live version of what we did above -- leaving the process running and inserting data into GridDB to see if the topic updates it.

Let's leave the terminal running which reads directly from the Kafka topics. And now we can try inputting data directly into the container which is being read by the console consumer and you can see it live update. So, to insert data into your container, you can use a Python script or the shell. 

With python: 

<div class="clipboard">
  <pre><code class="language-python">import griddb_python as griddb
from datetime import datetime, timedelta

factory = griddb.StoreFactory.get_instance()
DB_HOST = "127.0.0.1:10001"
DB_CLUSTER = "myCluster"
DB_USER = "admin"
DB_PASS = "admin"

try:
    # (1) Connect to GridDB
    # Fixed list method
    gridstore = factory.get_store(
        notification_member=DB_HOST, cluster_name=DB_CLUSTER, username=DB_USER, password=DB_PASS)

    # (2) Create a timeseries container - define the schema
    conInfo = griddb.ContainerInfo(name="device4",
                                   column_info_list=[["ts", griddb.Type.TIMESTAMP],
                                                     ["co", griddb.Type.DOUBLE],
                                                     ["humidity", griddb.Type.DOUBLE],
                                                     ["light", griddb.Type.BOOL],
                                                     ["lpg", griddb.Type.DOUBLE],
                                                     ["motion", griddb.Type.BOOL],
                                                     ["smoke", griddb.Type.DOUBLE],
                                                     ["temperature", griddb.Type.DOUBLE]],
                                   type=griddb.ContainerType.TIME_SERIES)
    # Create the container
    ts = gridstore.put_container(conInfo)
    print(conInfo.name, "container succesfully created")

    now = datetime.utcnow()

    device4 = gridstore.get_container("device4")
    if device4 == None:
        print("ERROR Container not found.")

    device4.put([now, 0.004978, 51.0, True, 0.00764837, True, 0.0204566, 55.2])

except griddb.GSException as e:
    for i in range(e.get_error_stack_size()):
        print("[", i, "]")
        print(e.get_error_code(i))
        print(e.get_location(i))
        print(e.get_message(i))</code></pre>
</div>

Once you insert the data, it will live load: 

    {"schema":{"type":"struct","fields":[{"type":"int64","optional":false,"name":"org.apache.kafka.connect.data.Timestamp","version":1,"field":"ts"},{"type":"double","optional":true,"field":"co"},{"type":"double","optional":true,"field":"humidity"},{"type":"boolean","optional":true,"field":"light"},{"type":"double","optional":true,"field":"lpg"},{"type":"boolean","optional":true,"field":"motion"},{"type":"double","optional":true,"field":"smoke"},{"type":"double","optional":true,"field":"temperature"}],"optional":false,"name":"device4"},"payload":{"ts":1664308679012,"co":0.004978,"humidity":51.0,"light":true,"lpg":0.00764837,"motion":true,"smoke":0.0204566,"temperature":55.2}}

Alternatively you could use the GridDB CLI:

<div class="clipboard">
  <pre><code class="language-sh">$ gs_sh
  gs> putrow device4 2022-09-30T12:30:01.234Z 0.003551 22.0 False 0.00754352 False 0.0232432 33.3</code></pre>
</div>

    {"schema":{"type":"struct","fields":[{"type":"int64","optional":false,"name":"org.apache.kafka.connect.data.Timestamp","version":1,"field":"ts"},{"type":"double","optional":true,"field":"co"},{"type":"double","optional":true,"field":"humidity"},{"type":"boolean","optional":true,"field":"light"},{"type":"double","optional":true,"field":"lpg"},{"type":"boolean","optional":true,"field":"motion"},{"type":"double","optional":true,"field":"smoke"},{"type":"double","optional":true,"field":"temperature"}],"optional":false,"name":"device4"},"payload":{"ts":1664308679229,"co":0.003551,"humidity":22.0,"light":false,"lpg":0.00754352,"motion":false,"smoke":0.0232432,"temperature":34.3}}
    

## Reading Data

Next, let's take a look at reading the data that is being moved.

### Batch Reading from GridDB

Let's try reading the data pulled from GridDB into our Kafka topics (via the Source connector)

<div class="clipboard">
  <pre><code class="language-sh">$ kafka-topics.sh --list --bootstrap-server localhost:9092</code></pre>
</div>

    device1
    device2
    device3
    device4

And now let's actually take a look at the data:

<div class="clipboard">
  <pre><code class="language-sh">$ kafka-console-consumer.sh --topic device4 --from-beginning --bootstrap-server localhost:9092</code></pre>
</div>

And then this was the output:

    {"schema":{"type":"struct","fields":[{"type":"int64","optional":false,"name":"org.apache.kafka.connect.data.Timestamp","version":1,"field":"ts"},{"type":"double","optional":true,"field":"co"},{"type":"double","optional":true,"field":"humidity"},{"type":"boolean","optional":true,"field":"light"},{"type":"double","optional":true,"field":"lpg"},{"type":"boolean","optional":true,"field":"motion"},{"type":"double","optional":true,"field":"smoke"},{"type":"double","optional":true,"field":"temp"}],"optional":false,"name":"device2"},"payload":{"ts":1594615046659,"co":0.004940912471056381,"humidity":75.5,"light":false,"lpg":0.007634034459861942,"motion":false,"smoke":0.020363432603022532,"temp":19.399999618530273}}

    Processed a total of 2 messages

### Querying our Data from GridDB

We can also try reading the data inserted into GridDB from our Kafka topics. We can do by directly querying our GridDB server using the [GridDB CLI][5] like so:

<div class="clipboard">
  <pre><code class="language-sh">$ sudo su gsadm
$ gs_sh
gs[public]> sql select * from device7;</code></pre>
</div>

    3 results. (25 ms)
    gs[public]> get
    ts,sensor,co,humidity,light,lpg,motion,smoke,temp
    2020-07-12T00:01:34.735Z,device7,0.0028400886071015706,76.0,false,0.005114383400977071,false,0.013274836704851536,19.700000762939453
    2020-07-12T00:02:19.641Z,device7,0.0028400886071015706,76.0,false,0.005114383400977071,false,0.013274836704851536,19.799999237060547
    2020-07-12T00:02:47.256Z,device7,0.0029050147565559603,75.9000015258789,false,0.005198697479294309,false,0.013508733329556249,19.700000762939453
    The 3 results had been acquired.

As an example here, we are checking to see if our Kafka was able to successfully insert data from Kafka into GridDB. At this point, each of our topics (deviceX ) should have an equivalent GridDB container.


## Conclusion

And with that, we have successfully used Kafka with GridDB, which is immensely useful for getting real-time data from your devices directly into your GridDB database and vice-versa.

 [1]: https://griddb.net/en/blog/using-kafka-with-griddb/
 [2]: https://griddb.net/en/blog/using-griddb-as-a-source-for-kafka-with-jdbc/
 [3]: https://kafka.apache.org/downloads
 [4]: https://docs.griddb.net/latest/gettingstarted/using-apt/
 [5]: https://github.com/griddb/cli
 [6]: https://www.kaggle.com/code/rjconstable/environmental-sensor-telemetry-dataset