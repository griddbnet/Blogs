We have written before about using Apache Kafka to load real-time data directly into your GridDB server. You can read our previous blogs with the following links: [Using Kafka With GridDB](https://griddb.net/en/blog/using-kafka-with-griddb/) & [Using GridDB as a source for Kafka with JDBC](https://griddb.net/en/blog/using-griddb-as-a-source-for-kafka-with-jdbc/).

Apache Kafka is a tool which allows for "real-time processing of record streams". What this means is that you can send real-time data from your sensors or various other pieces of tools directly into something else, and in this case into GridDB.

For this article, we will again be using Kafka in conjunction with GridDB with the newly released GridDB Kafka Connector. In our previous articles, we had been marrying GridDB and Kafka via JDBC, because with the help of the connector, JDBC is no longer a piece of the equation. There are two pieces which effectively connect GridDB with Kafka: the source and the sink. The GridDB Kafka sink connector pushes data from Apache Kafka topics and persists the data to GridDB database tables. And the source connector works in the opposite fashion, pulling data from GridDB and putting it into Kafka topics.

To showcase this, we will be showing off the sink connector, that is, pushing data from our kafka "topics" directly into our running GridDB server. We do this because this can be a real-world showcase of pushing data from on-the-field IoT devices to our DB.


## Setting up Kafka with GridDB

Continuing on with this article, we will install all prereqs and get the proper servers/scripts running, create our Kafka "topics", and finally push and save the data to our GridDB server.

### Preqs

To follow along, please have the following ready:

- Java
- Maven
- [Kafka](https://kafka.apache.org/downloads)
- [GridDB](https://docs.griddb.net/latest/gettingstarted/using-apt/)

### GridDB Kafka Connector

Once you have downloaded the Kafka connector, we can build it and move the resulting file into the proper location. To build: 

```bash
$ mvn clean install
```

From there, simply copy over the `.jar` file from `./target/griddb-kafka-connector-X.X.X.jar` to your kafka directory `./libs` directory.

### Starting Kafka

Once you have the griddb connector in the Kafka libs directory, we can start the kafka zookeeper and server.

```bash
bin/zookeeper-server-start.sh config/zookeeper.properties
```

And then in another terminal: 

```bash
$ bin/kafka-server-start.sh config/server.properties
```

And now our Kafka is mostly ready to go. To continue, please open up a third terminal.

### Setting up GridDB Sink Config File

Please edit the config file to enter in your running GridDB's servers credentials as well as the topics we aim to take in via Kafka topics. The file in question is: `GRIDDB_KAFKA_CONNECTOR_FOLDER/config/griddb-sink.properties`

While editing this file, first change the connector.class: 

```bash
connector.class=com.github.griddb.kafka.connect.GriddbSinkConnector
```

And nopw we can submit our GridDB server information. And because we are running in FIXED_LIST mod, we will edit the notification member as well and remove the host and port. Lastly, let's add in the topics we mean to ingest into our GridDB server: 

```bash
#host=239.0.0.1
#port=31999
cluster.name=myCluster
user=admin
password=admin
notification.member=127.0.0.1:10001
notification.provider.url=

#topics.regex=csh(.*)
#topics.regex=topic.(.*)
topics=topic_DEVICE7,topic_DEVICE8,topic_DEVICE9,topic_DEVICE10
```

Here we are explicitly telling our GridDB Sink Connector to look for these exact topics. We of course could simply use regex if we wanted to be more general, but for demo purposes, this is okay.


### Setting Up Our Simulated Sensor Data

In a real world example, our sensors would be individually setting up Kafka topics for our Kafka server to pick up on and send to GridDB, but because this is a demo, we will simply simulate the topic generation portion using a bash script and a `.txt` file.

If you're following along, create a `.txt` file called `simulate_sensor.txt`: 

```bash
2020-07-12 00:01:34.735 DEVICE7 0.0028400886071015706 76.0 false 0.005114383400977071 false 0.013274836704851536 19.700000762939453
2020-07-12 00:02:02.785 DEVICE8 0.0029050147565559603 75.80000305175781 false 0.005198697479294309 false 0.013508733329556249 19.700000762939453
2020-07-12 00:02:11.476 DEVICE9 0.0029381156266604295 75.80000305175781 false 0.005241481841731117 false 0.013627521132019194 19.700000762939453
2020-07-12 00:02:15.289 DEVICE10 0.0028400886071015706 76.0 false 0.005114383400977071 false 0.013274836704851536 19.700000762939453
2020-07-12 00:02:19.641 DEVICE7 0.0028400886071015706 76.0 false 0.005114383400977071 false 0.013274836704851536 19.799999237060547
2020-07-12 00:02:28.818 DEVICE8 0.0029050147565559603 75.9000015258789 false 0.005198697479294309 false 0.013508733329556249 19.700000762939453
2020-07-12 00:02:33.172 DEVICE9 0.0028400886071015706 76.0 false 0.005114383400977071 false 0.013274836704851536 19.799999237060547
2020-07-12 00:02:39.145 DEVICE10 0.002872341154862943 76.0 false 0.005156332935627952 false 0.013391176782176004 19.799999237060547
2020-07-12 00:02:47.256 DEVICE7 0.0029050147565559603 75.9000015258789 false 0.005198697479294309 false 0.013508733329556249 19.700000762939453
```

This is the data we aim to publish to GridDB.

Next create a bash script called `script_sink.sh`

```bash
#!/bin/bash

function echo_payload {
    echo '{ "payload": {  "datetime": "'$1 $2'",  "sensor": "'$3'",  "translate01": "'$4'",  "translate02": "'$5'",  "message": "'$6'",  "sensoractivity": "'$7'" }, "schema": {  "fields": [   {    "field": "datetime",    "optional": false,    "type": "string"   },   {    "field": "sensor",    "optional": false,    "type": "string"   },   {    "field": "translate01",    "optional": false,    "type": "string"   },   {    "field": "translate02",    "optional": false,    "type": "string"   },   {    "field": "message",    "optional": false,    "type": "string"   },   {    "field": "sensoractivity",    "optional": false,    "type": "string"   }   ],  "name": "sample",  "optional": false,  "type": "struct" }}'

}

TOPICS=()

for file in `find $1 -name \*rawdata.txt` ; do
    echo $file
    LOCATION="topic"
    head -10 $file |while read -r line ; do
        SENSOR=`echo ${line} | awk '{ print $3 }'`
        if [[ ! " ${TOPICS[@]} " =~ " ${LOCATION}_${SENSOR} " ]]; then
            echo Creating topic ${LOCATION}_${SENSOR}
            kafka-topics.sh --bootstrap-server 127.0.0.1:9092 --create --topic  ${LOCATION}_${SENSOR} 2&>1 /dev/null
            TOPICS+=(${LOCATION}_${SENSOR})
        fi
        echo_payload ${line} | kafka-console-producer.sh --topic ${LOCATION}_${SENSOR} --bootstrap-server localhost:9092
    done
done
```

This script will read in our raw data text file and generate our topics with our data and send it to the proper kafka process.

## Running And Reading

### Running

Now let's finally get this running.

First, to get this to work, you will also need to add the kafka directory to your path: 

```bash
$ export PATH=$PATH:/path/to/kafka_2.13-3.2.1/bin
```

Now add the proper permissions to your newly made script and we can run the script to feed into our kafka process. This will allow the data to be queued up and available for ingesting once the GridDB sink connector becomes available.

```bash
$ +x chmod script_sink.sh
$ ./script_sink.sh
```

    Creating topic topic_DEVICE7
    Creating topic topic_DEVICE8
    Creating topic topic_DEVICE9
    Creating topic topic_DEVICE10

And now that Kafka has these topics and their payloads queued up and ready to go, we can finally start up the Kafka server with the GridDB sink connector. 

To do, from the kafka directory, run: 

```bash 
$ ./bin/connect-standalone.sh config/connect-standalone.properties PATH_TO_GRIDDB_KAFKA/config/griddb-sink.properties
```

From the large amounts of output this command will generate, you should be able to see somethign resembling topics being placed into GridDB: 

    Put records to GridDB with number records 9 (com.github.griddb.kafka.connect.sink.GriddbSinkTask:54)

### Reading 

And with the succesful transfer of data to GridDB, we can directly query our GridDB server using the [GridDB CLI](https://github.com/griddb/cli) like so: 

```bash
$ sudo su gsadm
$ gs_sh
gs[public]> sql select * from topic_DEVICE7;
```

    2 results. (4 ms)

## Using GridDB Source Connector

If you wanted to do the reverse of what was described (ie. pushing saved GridDB containers out onto Kafka), you will instead use the GridDB source connector. Firstly, you will need to edit the config file: griddb-source.properties in  GRIDDB_KAFKA_CONNECTOR_FOLDER/config. You will need to change the GridDB connection details as well as the containers/topics.

For the containers section, let's change them directly to these large datasets we ingested from [Kaggle](https://www.kaggle.com/code/rjconstable/environmental-sensor-telemetry-dataset).

```bash
containers=device1,device2,device3
```

And one of the major differences between these two files (sink vs source) is that the source will need the following parameter (mandatory) `timestamp.column.name`. For this, we set it to our Timestamp row key, which in the topic_DEVICE7 is `ts`

```bash
timestamp.column.name=ts
mode=timestamp
```

Note: you could also use `mode=batch` which will not use the `timestamp.column.name` parameter and will loop and update your dataset many times. Whereas the former mode (timestamp) will grab the data just one time (til the queue is exhausted).

### Running the Source Connector

Running this will be similar to the sink connector: 

```bash 
$ ./bin/connect-standalone.sh config/connect-standalone.properties PATH_TO_GRIDDB_KAFKA/config/griddb-source.properties
```

If successful, you will see something like this in the output after running this: 

    [2022-09-23 18:51:44,262] INFO [griddb-kafka-source|task-0] Get Container info of container device1 (com.github.griddb.kafka.connect.dialect.GriddbDatabaseDialect:130)
    [2022-09-23 18:51:44,276] INFO [griddb-kafka-source|task-0] Get Container info of container device2 (com.github.griddb.kafka.connect.dialect.GriddbDatabaseDialect:130)
    [2022-09-23 18:51:44,277] INFO [griddb-kafka-source|task-0] Get Container info of container device3 (com.github.griddb.kafka.connect.dialect.GriddbDatabaseDialect:130)

### Reading From the Source Connector

And now with that run, you can look at the topics like so 

```bash
$ ./bin/kafka-topics.sh --list --bootstrap-server localhost:9092
```

    device1
    device2
    device3

And now let's actually take a look at the data: 

```bash
$ ./bin/kafka-console-consumer.sh --topic device2 --from-beginning --bootstrap-server localhost:9092
```


And then this was the output before I stopped it after ~3 seconds

    {"schema":{"type":"struct","fields":[{"type":"int64","optional":false,"name":"org.apache.kafka.connect.data.Timestamp","version":1,"field":"ts"},{"type":"double","optional":true,"field":"co"},{"type":"double","optional":true,"field":"humidity"},{"type":"boolean","optional":true,"field":"light"},{"type":"double","optional":true,"field":"lpg"},{"type":"boolean","optional":true,"field":"motion"},{"type":"double","optional":true,"field":"smoke"},{"type":"double","optional":true,"field":"temp"}],"optional":false,"name":"device2"},"payload":{"ts":1594615046659,"co":0.004940912471056381,"humidity":75.5,"light":false,"lpg":0.007634034459861942,"motion":false,"smoke":0.020363432603022532,"temp":19.399999618530273}}

    Processed a total of 16560 messages

## Conclusion

And with that, we have successfully used Kafka with GridDB, which is immensely useful for getting real-time data from your devices directly into your GridDB database and vice-versa.
