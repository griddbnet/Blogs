In today's article we will be discussing Kafka in conjunction with GridDB, which we have done before: 

- [Stream Data with GridDB and Kafka](https://griddb.net/en/blog/stream-data-with-griddb-and-kafka/)
- [Using GridDB as a source for Kafka with JDBC](https://griddb.net/en/blog/using-griddb-as-a-source-for-kafka-with-jdbc/)
- [Using SQL Batch Inserts with GridDB v5.5, JDBC, and Kafka](https://griddb.net/en/blog/using-sql-batch-inserts-with-griddb-v5-5-jdbc-and-kafka/)
- Udemy course: [Create a working IoT Project - Apache Kafka, Python, GridDB](https://www.udemy.com/course/create-a-working-iot-project-with-iot-database-griddb/)

We will focus in this article on a new feature which allows for use of Kafka with GridDB as a sink resource which will make `TIME_SERIES` containers (meaning we can push time_series data from Kafka topics directly into GridDB with some configuration); prior to v5.6, we were limited to Collection Containers. 

There will be some similarities with the blog last written about using Kafka with GridDB titled: "Stream Data with GridDB and Kafka". The differences here are that we have made all the moving parts of kafka and GridDB into Docker containers for easier portability and ease of use and will, as alluded to earlier, be using Time Series containers. If you follow along with this blog, you will learn how use Kafka to stream time series data directly into a GridDB time series container using Docker containers and Kafka.

## High Level Overview 

Before we get into how to run this project, let's briefly go over what this project does and how it works. We will get Kafka and GridDB running inside of docker containers, and once those are ready, we will run a python script which acts as a kafka `producer` to push up random data to the `broker`. This simulated iot data will then sit in a Kafka queue (though it's more accurately a `distributed log`) until a `consumer` is available to read those values. 

In our case, GridDB will act as the `sink`, meaning it will `consume` the data topics made by our python script and then save that data into tables which will created by Kafka based on our topics' schemas set within our Python script. 

To properly communicate how and where to save the Kafka topics, we will need to set up a GridDB Kafka Sink properties file. But first, we will also need to grab and build the latest version (v5.6) of the GridDB Kafka Connect and somehow share that with our running Kafka installation so that we may save time series data directly into time series containers. 

Within that properties file, we will need to set the container type to `time_series` along with various other important details.

## Getting Started

Let's discuss how to run this project.

### Prerequisites

To follow along with this blog, you will need docker and docker compose for running Kafka and GridDB. We will also need python3 installed to create data to be pushed into Kafka as topics (and then eventually saved into GridDB).

We will also need to grab and build the GridDB Kafka Connect jar file.

#### GridDB Kafka Connect (Optional)

You can download the latest version here: [griddb-kafka-connect](https://github.com/griddb/griddb-kafka-connect). To build, make sure you have `maven` installed and run: 

```bash
$ mvn clean install
```

The `.jar` file will be created inside of the `target` directory under the name: `griddb-kafka-connector-0.6.jar`. 

Note: The jar file is also included in the source code provided by this repo (in the next section). If you clone the repo and run this project via docker compose, you do not need to download/build the jar file yourself.

### Source Code

You can find the source code in the griddbnet github page: 

`$ git clone https://github.com/griddbnet/Blogs.git --branch kakfa_time_series`

### Running Project

Once you have the source code and docker installed, you can simply run: 

```bash
$ docker compose pull && docker compose up -d
```

And then once it's done, you can start checking if the Kafka connector has the GridDB sink properties file in place by running the following: 

```bash
$ curl http://localhost:8083/connectors/

["griddb-kafka-sink"]
```
You can also take a look at the contents of the kafka-sink to see what it contains:

```bash
$ curl http://localhost:8083/connectors/griddb-kafka-sink
```

Once that's done, you can run the python script, which acts as a kafka producer.

```bash
$ python3 -m pip install kafka-python
$ python3 scripts/producer.py
```

### GridDB Sink Properties

In Kafka and other stream/event-driven architectures, the concept of sources and sinks mean to describe the direction of the flow of data. The sink is where data flows *in*, or where the data ends up -- in this case, we want our data payloads to persist inside of GridDB as time series data inside of a time series container. And so we set the properties file as such: 

```bash
        connector.class= com.github.griddb.kafka.connect.GriddbSinkConnector
        name= griddb-kafka-sink
        cluster.name= myCluster
        user= admin
        password= admin
        notification.member= griddb-server=10001
        container.type= TIME_SERIES
        topics= meter_0,meter_1,meter_2,meter_3
        transforms=  TimestampConverter
        transforms.TimestampConverter.type=  org.apache.kafka.connect.transforms.TimestampConverter$Value
        transforms.TimestampConverter.format=  yyyy-MM-dd hh=mm=ss
        transforms.TimestampConverter.field=  timestamp
        transforms.TimestampConverter.target.type=  Timestamp
```

As compared to our previous article, the main changes are the `container.type` designation and the transforms properties. The transforms properties tells our Kafka cluster which string value will be converted into timestamp, along with other useful information to help that process along. The other values are simply allowing for our broker to know where to send the data topics to, which is our GridDB docker container with a hostname of `griddb-server`.

The topics are the name of the data topics and will also be the names of our GridDB time series containers. 

### Python Producer Script

There isn't much to say here that you can't get from simply reading the (simple) source code. The only thing I will add is that if you wished to docker-ize the docker container as well, you would change server location from `localhost` to `broker:9092`

```python
#p=KafkaProducer(bootstrap_servers=['localhost:9092'])
p=KafkaProducer(bootstrap_servers=['broker:9092'])
```

One other thing to note is that though we are making time_series data containers with time_series data as the row key, you still need to set your payload data fields as type `string` (I teased this above when discussing the `transform` property in the sink section). 

```python
"schema": 
{
    "fields": [ 
        { "field": "timestamp", "optional": False, "type": "string" },
        { "field": "kwh", "optional": False, "type": "double" }, 
        { "field": "temp", "optional": False, "type": "double" } 
    ], 
    "name": "iot", "optional": False, "type": "struct" 
}   
```

The key here is that though the type is `string`, we must set the *first* field as our targeted timestamp type. And then in the sink for this dataset, we set the `transforms.TimestampConverter.field` as the name of our field we want to convert to type timestamp. With these things in place, Kafka and GridDB will create your tables with the set schema and the proper container type.

## Running Kafka in Docker Containers

In our previous article about kafka, we simply ran kafka and GridDB on bare metal, meaning simply running the servers throught he CLI with commands. Though it worked well, it's a bit confusing because you need 3-4 terminals open and need to remember to run things in sequence. For this article, we have prepared a docker compose file which allows you to run download and run everything with 2-3 commands! 

### Confluent Docker Containers

First, let's discuss the docker images provided by Confluent, which is a company which provides support and tools pertaining to Kafka for your large corporation. Despite this though, they provide the docker images freely which we will use in our docker compose file. 

Essentially what docker compose does is allow us to create a set of "services" (AKA docker containers) which we can run in unison with a simple command, with rules set in which we can set which containers rely on others. For example, we can set the various kafka containers to rely on each other so that they start up in the correct sequence.

We opted for this because as explained above, running Kafka is not an easy process -- it has many different parts that need to run. For example, to run this seemingly simple project where we push data from python script --> kafka topics --> GridDb it takes 5 services in our Docker compose file. 

### Docker Compose Services

The following are all of the services.

- GridDB
- Kafka Zookeeper
- Kafka Broker
- Kafka Schema Registry
- Kafka-Connect

And another service which we omited but we could include is a kafka data producer. 

The Kafka zookeeper  can be thought of as the brains or the main component of kafka. The Broker is the service which handles the data topics and is often run with many different brokers for failsafes, etc; when we want to point our producer of data topics to Kafka, we point to the broker.

The kafka schema registry enforces schemas to be used for your topics. In our case, it's useful for deserialization of our JSON schema of our data payloads from our python producer. 

The Kafka Connect container is where we add our third party libraries for use with Kafka: GridDB Kafka connect jar and our GridDB sink properties file. The connect container is a bit unique in that we need to make sure that the container is up and running first and then we push to it a json file with the GridDB sink property instructions. The GridDB Kafka Connect jar file though we push to the file system during docker image start up.

#### Docker Compose Instructions

For GridDB there are no special instructions: we simply pull the image from griddbnet and then set some environment variables: 

```bash
  griddb-server:
    image: 'griddbnet/griddb:5.6.0'
    container_name: griddb-server
    expose:
      - '10001'
      - '10010'
      - '10020'
      - '10040'
      - '20001'
      - '41999'
    environment:
      NOTIFICATION_MEMBER: 1
      GRIDDB_CLUSTER_NAME: myCluster
```

The zookeeper is in a similar boat: 

```bash
  zookeeper:
    image: 'confluentinc/cp-zookeeper:7.3.0'
    container_name: zookeeper
    environment:
      ZOOKEEPER_CLIENT_PORT: 2181
      ZOOKEEPER_TICK_TIME: 2000
```

The broker exposes port 9092 so that we can run our python producer script outside of the context of our docker compose network environment (we just point to localhost:9092). There are also more environment variables necessary for pointing to the zookeeper and other cluster rules

```bash
  broker:
    image: 'confluentinc/cp-kafka:7.3.0'
    container_name: broker
    ports:
      - '9092:9092'
    depends_on:
      - zookeeper
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: 'zookeeper:2181'
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: 'PLAINTEXT:PLAINTEXT,PLAINTEXT_INTERNAL:PLAINTEXT'
      KAFKA_ADVERTISED_LISTENERS: 'PLAINTEXT://broker:9092,PLAINTEXT_INTERNAL://broker:29092'
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 1
      KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 1
```

You will also notice that the broker, schema registry, kafka connect all "depend" on the zookeeper. It really makes clear to us who is charge of the entire operation.

```bash
  kafka-schema-registry:
    image: 'confluentinc/cp-schema-registry:7.3.0'
    hostname: kafka-schema-registry
    container_name: kafka-schema-registry
    ports:
      - '8082:8082'
    environment:
      SCHEMA_REGISTRY_KAFKASTORE_BOOTSTRAP_SERVERS: 'PLAINTEXT://broker:9092'
      SCHEMA_REGISTRY_HOST_NAME: kafka-schema-registry
      SCHEMA_REGISTRY_LISTENERS: 'http://0.0.0.0:8082'
    depends_on:
      - zookeeper
```

The kafka connect also grabs from the confluent docker hub and has tons of environment variables, but it also includes volumes with a shared filesystem with the host machine so that we can share our GridDB Kafka Connect jar file. And lastly, we have scipt at the very bottom of the service which allows us to wait until our kafka-connect HTTP endpoints are available. Once we get a 200 status code as a response, we can run our script which sends our GridDB-Sink properties file. 

```bash
  kafka-connect:
    image: confluentinc/cp-kafka-connect:latest
    container_name: kafka-connect
    ports:
      - '8083:8083'
    environment:
      CONNECT_BOOTSTRAP_SERVERS: 'broker:9092'
      CONNECT_REST_PORT: 8083
      CONNECT_GROUP_ID: device
      CONNECT_CONFIG_STORAGE_TOPIC: device-config
      CONNECT_OFFSET_STORAGE_TOPIC: device-offsets
      CONNECT_STATUS_STORAGE_TOPIC: device-status
      CONNECT_KEY_CONVERTER: org.apache.kafka.connect.json.JsonConverter
      CONNECT_VALUE_CONVERTER: org.apache.kafka.connect.json.JsonConverter
      CONNECT_INTERNAL_KEY_CONVERTER: org.apache.kafka.connect.json.JsonConverter
      CONNECT_INTERNAL_VALUE_CONVERTER: org.apache.kafka.connect.json.JsonConverter
      CONNECT_KEY_CONVERTER_SCHEMAS_ENABLE: true
      CONNECT_VALUE_CONVERTER_SCHEMAS_ENABLE: true
      CONNECT_KEY_CONVERTER_SCHEMA_REGISTRY_URL: 'http://kafka-schema-registry:8082'
      CONNECT_VALUE_CONVERTER_SCHEMA_REGISTRY_URL: 'http://kafka-schema-registry:8082'
      CONNECT_REST_ADVERTISED_HOST_NAME: kafka-connect
      CONNECT_LOG4J_APPENDER_STDOUT_LAYOUT_CONVERSIONPATTERN: '[%d] %p %X{connector.context}%m (%c:%L)%n'
      CONNECT_CONFIG_STORAGE_REPLICATION_FACTOR: '1'
      CONNECT_OFFSET_STORAGE_REPLICATION_FACTOR: '1'
      CONNECT_STATUS_STORAGE_REPLICATION_FACTOR: '1'
      CONNECT_PLUGIN_PATH: >-
        /usr/share/java,/etc/kafka-connect/jars
      CLASSPATH: >-
        /usr/share/java,/etc/kafka-connect/jars
    volumes:
      - './scripts:/scripts'
      - './kafka-connect/connectors:/etc/kafka-connect/jars/'
      
    depends_on:
      - zookeeper
      - broker
      - kafka-schema-registry
      - griddb-server
    command:
      - bash
      - '-c'
      - >
        /etc/confluent/docker/run & 

        echo "Waiting for Kafka Connect to start listening on kafka-connect ⏳"

        while [ $$(curl -s -o /dev/null -w %{http_code}
        http://kafka-connect:8083/connectors) -eq 000 ] ; do 
          echo -e $$(date) " Kafka Connect listener HTTP state: " $$(curl -s -o /dev/null -w %{http_code} http://kafka-connect:8083/connectors) " (waiting for 200)"
          sleep 5 
        done

        nc -vz kafka-connect 8083

        echo -e "\n--\n+> Creating Kafka Connect GridDB sink"

        /scripts/create-griddb-sink.sh &&
        /scripts/example-sink.sh

        sleep infinity 
```

This properties file will give explicit instructions to Kafka that when topics with certain names are received by the broker, it should push those out to the instructions in the properties file, which in this case are our GridDB container.

## Conclusion

After you run the producer, you should be able to see all of your data inside of your docker griddb server through use of the GridDB CLI: `$ docker exec -it griddb-server gs_sh`.

And with that, we have successfully pushed IoT-like sensor data through Kafka to a GridDB Time Series container. 