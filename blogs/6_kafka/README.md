We have written before about using Apache Kafka to load real-time data directly into your GridDB server using a JDBC connection. You can read our previous blogs with the following links: [Using Kafka With GridDB][1] & [Using GridDB as a source for Kafka with JDBC][2].

Apache Kafka is a tool which allows for "real-time processing of record streams". What this means is that you can send real-time data from your sensors or various other pieces of tools directly into something else, and in this case into GridDB. You can also do the opposite: you can stream data from your GridDB containers directly into some other tool like logging analytics or various other tools.

For this article, we will again be using Kafka in conjunction with GridDB with the newly released GridDB Kafka Connector. In our previous articles, we had been marrying GridDB and Kafka via JDBC, because with the help of the connector, JDBC is no longer a piece of the equation. There are two pieces which effectively connect GridDB with Kafka: the source and the sink. The GridDB Kafka sink connector pushes data from Apache Kafka topics and persists the data to GridDB database tables. And the source connector works in the opposite fashion, pulling data from GridDB and putting it into Kafka topics.

To showcase this, we will be showing off the sink connector, that is, pushing data from our kafka "topics" directly into our running GridDB server.

## Setting up Kafka with GridDB

Continuing on with this article, we will install all prereqs and get the proper servers/scripts running, create our Kafka "topics", and finally push and save the data to our GridDB server.

### Preqs

To follow along, please have the following ready:

*   Java
*   Maven
*   [Kafka][3]
*   [GridDB][4]

### Installing Kafka

To install kafka: 

<div class="clipboard">
  <pre><code class="language-sh">$ wget https://archive.apache.org/dist/kafka/3.2.0/kafka_2.12-3.2.0.tgz
$ tar xzvf kafka_2.12-3.2.0.tgz
$ cd kafka_2.12-3.2.0
$ export PATH=$PATH:$PWD/bin</code></pre>
</div>

### GridDB Kafka Connector

Head over to the Connector's repo and clone it: 

<div class="clipboard">
  <pre><code class="language-sh">$ git clone https://github.com/griddb/griddb-kafka-connect.git
$ cd griddb-kafka-connect/</code></pre>
</div>

Once you have downloaded the Kafka connector, we can build it and move the resulting file into the proper location. To build:

<div class="clipboard">
  <pre><code class="language-sh">$ mvn clean install</code></pre>
</div>

From there, simply copy over the `.jar` file from `./target/griddb-kafka-connector-X.X.X.jar` to your kafka directory `./libs` directory.

### Starting Kafka

Once you have the griddb connector in the Kafka libs directory, we can start the kafka zookeeper and server.

<div class="clipboard">
  <pre><code class="language-sh">$ bin/zookeeper-server-start.sh config/zookeeper.properties</code></pre>
</div>

And then in another terminal:

<div class="clipboard">
  <pre><code class="language-sh">$ bin/kafka-server-start.sh config/server.properties</code></pre>
</div>

And now our Kafka is mostly ready to go. To continue, please open up a third terminal.

### Setting up GridDB Sink Config File

Please edit the config file to enter in your running GridDB's servers credentials as well as the topics we aim to take in via Kafka topics. The file in question is: `GRIDDB_KAFKA_CONNECTOR_FOLDER/config/griddb-sink.properties`

And now we can submit our GridDB server information. And because we are running in FIXED_LIST mod, we will edit the notification member as well and remove the host and port. Lastly, let's add in the topics we mean to ingest into our GridDB server:

<div class="clipboard">
  <pre><code class="language-sh">#host=239.0.0.1
#port=31999
cluster.name=myCluster
user=admin
password=admin
notification.member=127.0.0.1:10001
notification.provider.url=

#topics.regex=csh(.*)
#topics.regex=topic.(.*)
topics=device7,device8,device9,device10</code></pre>
</div>

Here we are explicitly telling our GridDB Sink Connector to look for these exact topics. We of course could simply use regex if we wanted to be more general, but for demo purposes, this is okay.

### Setting Up Our Simulated Sensor Data

In a real world example, our sensors would be individually setting up Kafka topics for our Kafka server to pick up on and send to GridDB, but because this is a demo, we will simply simulate the topic generation portion using a bash script and a `.txt` file.

If you're following along, create a `.txt` file called `simulate_sensor.txt`:

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

Next create a bash script called `script_sink.sh`

<div class="clipboard">
  <pre><code class="language-sh">#!/bin/bash

function echo_payload {
    echo '{"payload": {"ts": "'$1 $2'","co": "'$4'","humidity": "'$5'","light": "'$6'","lpg": "'$7'","motion": "'$8'","smoke": "'$9'","temp": "'{$10}'"},"schema": {"fields": [{"field": "ts","optional": false,"type": "string"},{"field": "co","optional": false,"type": "string"},{"field": "humidity","optional": false,"type": "string"},{"field": "light","optional": false,"type": "string"},{"field": "lpg","optional": false,"type": "string"},{"field": "motion","optional": false,"type": "string"},{"field": "smoke","optional": false,"type": "string"},{"field": "temp","optional": false,"type": "string"}],"name": "iot","optional": false,"type": "struct"}}'
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
done
</code></pre>
</div>

This script will read in our raw data text file and generate our topics with our data and send it to the proper kafka process.

A small tip: if a topic ends up malformed and does not allow you to fix it, you can delete a topic to restart the process: 

<div class="clipboard">
  <pre><code class="language-sh">$ ./bin/kafka-topics.sh --bootstrap-server localhost:9092 --delete --topic device7</code></pre>
</div>

## Running And Reading

### Running

Now let's finally get this running.

First, to get this to work, you will also need to add the kafka directory to your path as mentioned above in the first section where we installed Kafka. Here is that command again:

<div class="clipboard">
  <pre><code class="language-sh">$ export PATH=$PATH:/path/to/kafka_2.13-3.2.1/bin</code></pre>
</div>

Now add the proper permissions to your newly made script and we can run the script to feed into our kafka process. This will allow the data to be queued up and available for ingesting once the GridDB sink connector becomes available.

<div class="clipboard">
  <pre><code class="language-sh">$ chmod +x script_sink.sh
$ ./script_sink.sh</code></pre>
</div>

    Creating topic device7
    Creating topic device8
    Creating topic device9
    Creating topic device10
    

And now that Kafka has these topics and their payloads queued up and ready to go, we can finally start up the Kafka server with the GridDB sink connector.

To do, from the kafka directory, run:

<div class="clipboard">
  <pre><code class="language-sh"> 
$ ./bin/connect-standalone.sh config/connect-standalone.properties PATH_TO_GRIDDB_KAFKA/config/griddb-sink.properties</code></pre>
</div>

From the large amounts of output this command will generate, you should be able to see something resembling topics being placed into GridDB:

    Put records to GridDB with number records 9 (com.github.griddb.kafka.connect.sink.GriddbSinkTask:54)
    

### Reading

And with the successful transfer of data to GridDB, we can directly query our GridDB server using the [GridDB CLI][5] like so:

<div class="clipboard">
  <pre><code class="language-sh">$ sudo su gsadm
$ gs_sh
gs[public]> sql select * from device7;</code></pre>
</div>

    2 results. (4 ms)

### Inserting Live Payloads

Next, we will try sending off payloads after topic creation and after we get our GridDB sink running. The goal will be showcaasing live payloads being inserted into GridDB. So leave the Sink running and let's try to create a payload to send: 
    

## Using GridDB Source Connector

If you wanted to do the reverse of what was described (ie. pushing saved GridDB containers out onto Kafka), you will instead use the GridDB source connector. Firstly, you will need to edit the config file: griddb-source.properties in GRIDDB_KAFKA_CONNECTOR_FOLDER/config. You will need to change the GridDB connection details as well as the containers/topics.

For the containers section, let's change them directly to these large datasets we ingested from [Kaggle][6].

<div class="clipboard">
  <pre><code class="language-sh">containers=device1,device2,device3</code></pre>
</div>

And one of the major differences between these two files (sink vs source) is that the source will need the following parameter (mandatory) `timestamp.column.name`. For this, we set it to our Timestamp row key, which in the device7 is `ts`

<div class="clipboard">
  <pre><code class="language-sh">timestamp.column.name=ts
mode=timestamp</code></pre>
</div>

Note: you could also use `mode=batch` which will not use the `timestamp.column.name` parameter and will loop and update your dataset many times. Whereas the former mode (timestamp) will grab the data just one time ('til the queue is exhausted).

### Running the Source Connector

Running this will be similar to the sink connector:

<div class="clipboard">
  <pre><code class="language-sh"> $ ./bin/connect-standalone.sh config/connect-standalone.properties PATH_TO_GRIDDB_KAFKA/config/griddb-source.properties</code></pre>
</div>

If successful, you will see something like this in the output after running this:

    [2022-09-23 18:51:44,262] INFO [griddb-kafka-source|task-0] Get Container info of container device1 (com.github.griddb.kafka.connect.dialect.GriddbDatabaseDialect:130)
    [2022-09-23 18:51:44,276] INFO [griddb-kafka-source|task-0] Get Container info of container device2 (com.github.griddb.kafka.connect.dialect.GriddbDatabaseDialect:130)
    [2022-09-23 18:51:44,277] INFO [griddb-kafka-source|task-0] Get Container info of container device3 (com.github.griddb.kafka.connect.dialect.GriddbDatabaseDialect:130)
    

### Reading From the Source Connector

And now with that run, you can look at the topics like so

<div class="clipboard">
  <pre><code class="language-sh">$ ./bin/kafka-topics.sh --list --bootstrap-server localhost:9092</code></pre>
</div>

    device1
    device2
    device3
    

And now let's actually take a look at the data:

<div class="clipboard">
  <pre><code class="language-sh">$ ./bin/kafka-console-consumer.sh --topic device4 --from-beginning --bootstrap-server localhost:9092</code></pre>
</div>

And then this was the output:

    {"schema":{"type":"struct","fields":[{"type":"int64","optional":false,"name":"org.apache.kafka.connect.data.Timestamp","version":1,"field":"ts"},{"type":"double","optional":true,"field":"co"},{"type":"double","optional":true,"field":"humidity"},{"type":"boolean","optional":true,"field":"light"},{"type":"double","optional":true,"field":"lpg"},{"type":"boolean","optional":true,"field":"motion"},{"type":"double","optional":true,"field":"smoke"},{"type":"double","optional":true,"field":"temp"}],"optional":false,"name":"device2"},"payload":{"ts":1594615046659,"co":0.004940912471056381,"humidity":75.5,"light":false,"lpg":0.007634034459861942,"motion":false,"smoke":0.020363432603022532,"temp":19.399999618530273}}
    
    Processed a total of 2 messages


And because Kafka is all about streaming real time data, we can also showcase that. Let's leave the terminal running which reads directly from the Kafkat topics. And now you can try inputting data directly into the container which is being read by the console consumer and you can see it live update. So, to insert data into your container, you can use a Python script or the shell. 

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
    

## Conclusion

And with that, we have successfully used Kafka with GridDB, which is immensely useful for getting real-time data from your devices directly into your GridDB database and vice-versa.

 [1]: https://griddb.net/en/blog/using-kafka-with-griddb/
 [2]: https://griddb.net/en/blog/using-griddb-as-a-source-for-kafka-with-jdbc/
 [3]: https://kafka.apache.org/downloads
 [4]: https://docs.griddb.net/latest/gettingstarted/using-apt/
 [5]: https://github.com/griddb/cli
 [6]: https://www.kaggle.com/code/rjconstable/environmental-sensor-telemetry-dataset