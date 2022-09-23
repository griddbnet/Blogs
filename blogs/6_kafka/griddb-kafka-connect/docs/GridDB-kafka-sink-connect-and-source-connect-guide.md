# Using Apache Kafka with GridDB database

## Introduction

```text
Today’s applications and IoT devices are generating a lot of big data. Most of this data is unstructured in nature. For a very long time, relational database management systems have been used to store structured data. These database management systems organize data into tables, which are rows and columns. However, unstructured data cannot be organized well in rows and columns, thus, relational databases are not the best option for storage of unstructured data. NoSQL databases were developed to fill the gaps created by relational databases. They are easy to use for the storage of unstructured data. GridDB is a good example of a NoSQL database. It is well optimized for IoT and Big Data. When using GridDB, you will need to stream data in and out. Apache Kafka is a data streaming platform that can help you to achieve this. In this article, we will be walking you through how to organize communication between different elements of the software system when using Kafka and GridDB database.
```

### Apache Kafka connector
```text
Apache Kafka is a distributed event streaming platform used for data integration, data pipelines, and streaming analytics. It can help you to ingest and process streaming data in real-time. Streaming data refers to the data generated continuously by data sources, sending data records simultaneously. Apache Kafka provides its users with the following functions:
```
* Publish and subscribe to record streams.
* Effective storage of record streams in the same order they were generated.
* Real-time processing of record streams.

Apache Kafka combines storage, messaging, and stream processing to facilitate the analysis of real-time and historical data.

### GridDB database

* GridDB is a database for IoT with both NoSQL interface and SQL interface.
    * GridDB’s Key Container data model and Time Series functions are built for IoT.
    * GridDB’s hybrid composition of In-Memory and Disk architecture is designed for maximum performance.
    * GridDB scales linearly and horizontally on commodity hardware maintaining excellent performance.
    * Hybrid cluster management and high fault-tolerant system of GridDB are exceptional for mission-critical applications.

### GridDB Kafka sink connector
* The GridDB Kafka sink connector pushes data from Apache Kafka topics and persists the data to GridDB database tables.
* This guide explains how to configure and use the GridDB Kafka sink connector.

### GridDB Kafka source connector
* The GridDB Kafka source connector pulls data from GridDB database tables and persists the data to Apache Kafka topics.
* This guide explains how to configure and use the GridDB Kafka source connector.

### The mapping between Apache Kafka data type and GridDB data type

* The data mapping is automatic mapping in source connector. Table type mapping:

    | Apache Kafka data type  | GridDB data type |
    |-------------------------|------------------|
    | INT8                    | GSType.BYTE      |
    | INT16                   | GSType.SHORT     |
    | INT32                   | GSType.INTEGER   |
    | INT64                   | GSType.LONG      |
    | FLOAT32                 | GSType.FLOAT     |
    | FLOAT64                 | GSType.DOUBLE    |
    | BOOLEAN                 | GSType.BOOL      |
    | STRING                  | GSType.STRING    |
    | BYTES                   | GSType.BLOB      |
    | Timestamp               | GSType.TIMESTAMP |

* Type mapping in sink connector:

    | Apache Kafka data type  | GridDB data type |
    |-------------------------|------------------|
    | INT8                    | GSType.BYTE      |
    | INT16                   | GSType.SHORT     |
    | INT32                   | GSType.INTEGER   |
    | INT64                   | GSType.LONG      |
    | FLOAT32                 | GSType.FLOAT     |
    | FLOAT64                 | GSType.DOUBLE    |
    | BOOLEAN                 | GSType.BOOL      |
    | STRING                  | GSType.STRING    |
    | BYTES                   | GSType.BLOB      |
    | Decimal, Date, Time, Timestamp | GSType.TIMESTAMP |

* In Kafka-connector-JDBC, the primary can be any column, so it is configurable. But in GridDB, the rowkey is the first column, so it is not configurable with GridDB Kafka sink/source connector.

### The relationship between Apache Kafka and GridDB database

* Concept

    | Concept of Apache Kafka | Concept of GridDB |
    |-------------------------|-------------------|
    | topic                   | container (table)  |
    | record                  | row               |
    | field                   | column            |

    Note: Please refer to an example in [GridDB write as below](#relations). 

* Mode
    * From GridDB database tables to Apache Kafka(GridDB Kafka source connector)
        | Container type of GridDB | Mode of Apache Kafka  |
        |--------------------------|-----------------------|
        | COLLECTION,TIMESERIES    |    bulk               |
        | COLLECTION,TIMESERIES    |    timestamp(should be used for column timestamp) |
    Suggestion: If GridDB database tables have column names, use type timestamp. Users should use mode timestamp(GridDB source connector configure) to get data from GridDB database tables.

Note: Please refer to an example in GridDB written for 2 modes as below.

## Quick start

### Operating environment

Building of the library and execution of the sample programs have been checked in the following environment.

    OS: CentOS 7.9(x64)/Ubuntu 20.04
    Java: 1.8
    Maven: 3.5.0
    Kafka: 2.12-3.2.0
    GridDB server: V5.0 CE

### Install and start Apache Kafka

```console
$ wget https://dlcdn.apache.org/kafka/3.2.0/kafka_2.12-3.2.0.tgz
$ tar xzvf kafka_2.12-3.2.0.tgz
$ cd kafka_2.12-3.2.0
$ export PATH=$PATH:/path/to/kafka_2.12-3.2.0/bin
$ zookeeper-server-start.sh -daemon config/zookeeper.properties # Start zookeeper server
$ kafka-server-start.sh config/server.properties # Start Apache Kafka server
```

Note:
* Keep terminal to keep starting Apache Kafka server.
* If Apache Kafka broker is not available when starting Apache Kafka server, please fix this error as below:
    * Open server.properties file and uncomment this line "#listeners=PLAINTEXT://:9092"
    * Change to "listeners=PLAINTEXT://127.0.0.1:9092"

### Install GridDB server
1. [Using package or source](https://github.com/griddb/griddb)
2. [Using docker](https://github.com/griddb/griddb-docker/tree/main/griddb)
    * Quick docker command for starting GridDB server
        ```console
        $ docker pull griddb/griddb
        $ docker run --network="host" -e GRIDDB_CLUSTER_NAME=griddb griddb/griddb
        ```

Note: 
* If using docker, please open a new terminal for install and start GridDB server.

### Build GridDB Kafka sink connector and GridDB Kafka source connector

1. Build GridDB Kafka sink/source connector

    ```console
    $ cd GRIDDB_KAFKA_CONNECTOR_FOLDER
    $ mvn clean install -DskipTests
    ```

2. After using command `$ mvn clean install -DskipTests`, file griddb-kafka-connector-1.0-SNAPSHOT.jar will be created in "target/" folder.

3. Copy the griddb-kafka-connector-1.0-SNAPSHOT.jar file to /path/to/kafka_2.12-3.2.0/libs/

## About GridDB Kafka sink connector

### Creating Apache Kafka topics and events

1. Prepare data
* Create new file "rawdata.txt" for GridDB Kafka sink connector
    * File "rawdata.txt" has data as below:
        ```txt
        2012-07-18 12:54:45.126257  D001    Ignore      Ignore      CLOSE   Control4-Door
        2012-07-18 12:54:45.196564  D002    OutsideDoor FrontDoor   OPEN    Control4-Door
        2012-07-18 12:54:45.247825  T102    Ignore      FrontDoorTemp   78  Control4-Temperature
        2012-07-18 12:54:45.302398  BATP102 Ignore      Ignore      85  Control4-BatteryPercent
        2012-07-18 12:54:45.399416  T103    Ignore      BathroomTemp    25  Control4-Temperature
        2012-07-18 12:54:45.472391  BATP103 Ignore      Ignore      82  Control4-BatteryPercent
        2012-07-18 12:54:45.606580  T101    Ignore      Ignore      31  Control4-Temperature
        2012-07-18 12:54:45.682577  MA016   Kitchen     Kitchen     OFF Control4-MotionArea
        2012-07-18 12:54:45.723461  D003    Bathroom    BathroomDoor    OPEN    Control4-Door
        2012-07-18 12:54:45.767498  M009    Bedroom     Bedroom     ON  Control4-Motion
        ```

2. Prepare script for running Apache Kafka

* Create new file script "script_sink.sh"
    * Source code in file script_sink.sh is as below
        ```sh
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

3. Run script

* Set permission to execute for script_sink.sh
    ```console
    $ chmod a+x script_sink.sh
    ```
* Export PATH for executing script_sink.sh
    ```console
    $ export PATH=$PATH:/path/to/kafka_2.12-3.2.0/bin
    ```
* Run script_sink.sh for Apache Kafka

    ```console
    $ ./script_sink.sh
    ```
    => output:
    ```console
    ./rawdata.txt
    Creating topic topic_D001
    >>Creating topic topic_D002
    >>Creating topic topic_T102
    >>Creating topic topic_BATP102
    >>Creating topic topic_T103
    >>Creating topic topic_BATP103
    >>Creating topic topic_T101
    >>Creating topic topic_MA016
    >>Creating topic topic_D003
    >>Creating topic topic_M009
    ```

### Running GridDB Kafka sink connector

1. Open griddb-sink.properties file at GRIDDB_KAFKA_CONNECTOR_FOLDER and configure property as below:
* Use property topics.regex:
    ```properties
    topics.regex=topic.(.*)
    ```
* Use property topic:
    ```properties
    topics=topic_D001,topic_D002
    ```
2. Open a new terminal for executing this command:
    ```console
    $ cd kafka_2.12-3.2.0
    $ ./bin/connect-standalone.sh config/connect-standalone.properties GRIDDB_KAFKA_CONNECTOR_FOLDER/config/griddb-sink.properties
    ```
3. After finishing the command above, data/topic was pushed into GridDB database.

### Using GridDB CLI command to inspect data for GridDB Kafka sink connector

```console
$ git clone https://github.com/griddb/cli.git
$ cd cli && ant # Refer to https://github.com/griddb/cli to build it
$ CP=.
$ CP=$CP:common/lib/commons-io-2.4.jar:release/griddb-cli.jar:common/lib/gridstore.jar:common/lib/gridstore-jdbc.jar:common/lib/jackson-annotations-2.2.3.jar:common/lib/jackson-core-2.2.3.jar:common/lib/jackson-databind-2.2.3.jar:common/lib/javax.json-1.0.jar:common/lib/jersey-client-1.17.1.jar:common/lib/jersey-core-1.17.1.jar:common/lib/orion-ssh2-214.jar:lib/commons-beanutils-1.9.3.jar:lib/commons-cli-1.2.jar:lib/commons-collections-3.2.2.jar:lib/commons-lang3-3.5.jar:lib/commons-logging-1.2.jar:lib/jline-3.17.1.jar:lib/logback-classic-1.0.13.jar:lib/logback-core-1.0.13.jar:lib/opencsv-3.9.jar:lib/slf4j-api-1.7.5.jar
$ java -Xmx1024m -Dlogback.configurationFile=gs_sh_logback.xml -classpath "$CP:$CLASSPATH"  com.toshiba.mwcloud.gs.tools.shell.GridStoreShell $*
$ setcluster cluster0 griddb 239.0.0.1 31999
$ setclustersql cluster0 griddb 239.0.0.1 41999
$ setuser admin admin
$ connect $cluster0
$ gs[public]> sql select * from topic_D001;
1 results. (2 ms)
$ gs[public]> get
datetime,sensor,translate01,translate02,message,sensoractivity
2012-07-18T00:54:45.000Z,D001,Ignore,Ignore,CLOSE,Control4-Door
The 1 results had been acquired.
```

### Config parameters

*  GridDB Kafka sink connector in the config file

    |Parameter   | Description  | Default Value   |
    |---|---|---|
    |connector.class|the sink connector class|com.github.griddb.GriddbSinkConnector|
    |name|the connector name|   |
    |topics.regex|The list of topics to be format, It is used by the sink connector|   |
    |topics|The name of topics is used by the sink connector|   |
    |host|GridDB host (can use with both multicast and master node mode)|   |
    |port|GridDB port (can use with both multicast and master node mode)|   |
    |cluster.name|GridDB cluster name|   |
    |user|GridDB username|   |
    |password|GridDB user password|   |
    |notification.member|GridDB notification member list in fix list method|   |
    |batch.size|the size of write buffer to GridDB|3000|
    |multiput|using multiput or single put in write buffer|true|
    |container.name.format|using it to change to topic name from GridDB container|$(topic): The default container name is topic name |

    Note:
    * In file config/griddb-sink.properties: config values (connector.class, name, topics.regex or topics, transforms) are the properties used by Apache Kafka, not the connector).
    * Just configure one property between "topics.regex" and "topics".

## About GridDB Kafka source connector

### Prepare data for GridDB database

* Create new file "topic.py" for GridDB Python API and push data into GridDB database
    * The source code in file "topic.py" is as below:
        ```python
        #!/usr/bin/python

        import griddb_python as griddb
        import sys

        factory = griddb.StoreFactory.get_instance()

        argv = sys.argv
        try:
            #Get GridStore object
            gridstore = factory.get_store(host=argv[1], port=int(argv[2]), cluster_name=argv[3], username=argv[4], password=argv[5])

            #Create Timeseries
            name_topic = ["D001", "D002", "T102", "BATP102", "T103", "BATP103", "T101", "MA016", "D003", "M009"]
            for name in name_topic:
                conInfo = griddb.ContainerInfo(name,
                                [["name", griddb.Type.TIMESTAMP],
                                ["status", griddb.Type.BOOL],
                                ["count", griddb.Type.LONG],
                                ["sign", griddb.Type.STRING]],
                                griddb.ContainerType.TIME_SERIES, True)
                col = gridstore.put_container(conInfo)
                #Change auto commit mode to false
                col.set_auto_commit(False)
                #Put row: RowKey is "name01"
                ret = col.put(['2020-10-01T15:00:00.000Z', False, 1, "griddb"])
                col.commit()
        except griddb.GSException as e:
            for i in range(e.get_error_stack_size()):
                print("[", i, "]")
                print(e.get_error_code(i))
                print(e.get_location(i))
                print(e.get_message(i))
        ```

* Run sample "topic.py"
    * Install GridDB Python API
        * [Using package or source](https://github.com/griddb/python_client)
        * [Using Pypi](https://pypi.org/project/griddb-python/)
        * Quick install GridDB Python API. OS machine should use python3.10

            ```console
            $ python3 -m pip install numpy
            $ python3 -m pip install pandas
            $ python3 -m pip install griddb-python==0.8.5
            ```
            ```console
            $ python3 topic.py 239.0.0.1 31999 griddb admin admin
            ```
        Note: GridDB Python API is developed using GridDB C API and SWIG. Install and run the sample GridDB Python API, the OS needs to install [GridDB C API V5.0 CE](https://github.com/griddb/c_client) and [SWIG V4.0.2](http://www.swig.org/).
* <a id="relations"></a>Simple relationship in source code between Apache Kafka and GridDB database:

    | Apache Kafka | GridDB database  | Value  |
    |--------------|------------------|--------|
    | topic        | container(table) |"D001", "D002", "T102",....|
    | record       | row              | ['2020-10-01T15:00:00.000Z', False, 1, "griddb"],... |
    | field        | column           |"name","status","count","sign"|

### Running GridDB Kafka source connector

1. Open griddb-source.properties file at GRIDDB_KAFKA_CONNECTOR_FOLDER, find properties similar and edit them as below:
    * Timestamp mode configure
        ```properties
        containers=D001,D002,T102,BATP102,T103,BATP103,T101,MA016,D003,M009
        mode=timestamp
        timestamp.column.name=name
        ```

    * Bulk mode configure
        ```properties
        containers=D001,D002,T102,BATP102,T103,BATP103,T101,MA016,D003,M009
        mode=bulk
        ```
Note:
* The differences between both modes are:
    * The mode timestamp: It needs at least one column is type timestamp (need configure property: "timestamp.column.name") to get data from GridDB database tables. It gets data just one time.

    * The mode bulk: It needn't be configured for "timestamp.column.name", but it gets loops and updates data many times.

2. Open new terminal for executing commands:
    ```console
    $ cd kafka_2.12-3.2.0
    $ ./bin/connect-standalone.sh config/connect-standalone.properties GRIDDB_KAFKA_CONNECTOR_FOLDER/config/griddb-source.properties
    ```
Note:
* GridDB Kafka sink connector and GridDB Kafka source connector default run on the same port (9092) so if you want to run both sink connector and source connector, please configure the port for Apache Kafka before running them. 
* Besides, stopping the current process before running other methods of Apache Kafka.

### Using the Apache Kafka command to check the topic on the GridDB database

1. The listing of the available Topics in Apache Kafka:
    ```console
    $ ./bin/kafka-topics.sh --list --bootstrap-server localhost:9092
    BATP102
    BATP103
    D001
    D002
    D003
    M009
    MA016
    T101
    T102
    T103
    ```
2. Check and get data about the topic:

* Timestamp mode
    ```console
    $ ./bin/kafka-console-consumer.sh --topic BATP102 --from-beginning --bootstrap-server localhost:9092
    {"schema":{"type":"struct","fields":[{"type":"int64","optional":false,"name":"org.apache.kafka.connect.data.Timestamp","version":1,"field":"name"},{"type":"boolean","optional":true,"field":"status"},{"type":"int64","optional":true,"field":"count"},{"type":"string","optional":true,"field":"sign"}],"optional":false,"name":"D001"},"payload":{"name":1601564400000,"status":false,"count":1,"sign":"griddb"}}
    ```

* Bulk mode
    ```console
    $ ./bin/kafka-console-consumer.sh --topic BATP102 --from-beginning --bootstrap-server localhost:9092
    {"schema":{"type":"struct","fields":[{"type":"int64","optional":false,"name":"org.apache.kafka.connect.data.Timestamp","version":1,"field":"name"},{"type":"boolean","optional":true,"field":"status"},{"type":"int64","optional":true,"field":"count"},{"type":"string","optional":true,"field":"sign"}],"optional":false,"name":"D002"},"payload":{"name":1601564400000,"status":false,"count":1,"sign":"griddb"}}
    ```

### Config parameters

* GridDB Kafka source connector config parameters in the config file

    |Parameter   | Description  | Default Value   |
    |---|---|---|
    |connector.class|the source connector class|com.github.griddb.GriddbSourceConnector|
    |name|the connector name|   |
    |host|GridDB host (can use with both multicast and master node mode)|   |
    |port|GridDB port (can use with both multicast and master node mode)|   |
    |cluster.name|GridDB cluster name|   |
    |user|GridDB username|   |
    |password|GridDB user password|   |
    |notification.member|GridDB notification member list in fix list method|   |
    |containers|list of GridDB containers used by the source connector|   |
    |mode|the mode to import (bulk/timestamp)|   |
    |timestamp.column.name|the list of timestamp column in timestamp mode|   |
    |topic.prefix|the prefix of output topic|   |
    |mode|the mode to import (bulk/timestamp)|   |
    |polling.interval.ms|interval time for GridDB Kafka source connector to poll data|5000|

    Note: 
    * In file config/griddb-source.properties: the config values (connector.class, name is the properties used by Kafka, not the connector).

