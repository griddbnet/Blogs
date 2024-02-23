With the release of GridDB v5.5, GridDB has added SQL batch inserts. This release is great for many reasons, but a very clear benefit is being able to hook up a generic Kafka JDBC connector with GridDB out of the box. Prior to this release, we could only insert one Kafka message at a time, but now we can batch update up to 1,000 data points at a time. 

In this article, we will be walking through setting up Kafka with GridDB. We have covered this topic before as seen here: [Data Ingestion](https://docs.griddb.net/tutorial/kafka/#setup-kafka). In those docs, we set up GridDB with Kafka but you were required to download a slightly altered version of the JDBC connector (not the official release) and you were also limited to setting the batch size to only 1. This time around, we wil-l set the batch size to 1,000 and we will simply use the official GridDB JDBC Connector with the official kafka-connect-jdbc driver. 

So, to get this project up and running, we will need to install/set up kafka (broker), kafka-connect, kafka (zookeeper), kafka schema registry, and GridDB. And then once those are running and installed, we will also need to use the kafka-connect service to add the generic JDBC library and the GridDB JDBC library. Next, we will need to set up our Kafka Sink properties so that our Kafka instances know what kind of data we are handling and where it should go (to GridDB via JDBC). And lastly, we will need to create our Kafka topics, push some simulated data onto there, and hope that the data flows out into GridDB.

## How to Run Project

If you would like to run this project, you can simply use docker compose to run all of the associated kafka services; these containers also come bundled up with their respective library files. Which brings us to our prereqs.

### Prerequisites
 
You can of course run this project by manually installing kafka, zookeeper, etc, but this project was built using Docker, so the only true requirement for this project is Docker.

You can download Docker from their website: [https://docs.docker.com/get-docker/](https://docs.docker.com/get-docker/).

### Building & Running

Running this project will require the following steps: you will need to build the docker images, start them, and finally push some data into the relevant topics to see them be flushed out into your GridDB server (which is also running via Docker container).

```bash
$ docker compose build
$ docker compose up -d
```

And then once everything is running (namely the broker), you can run the python script to make some kafka topics and push data onto them: 

```bash
$ cd gateway-sim
$ python3 -m pip install kafka-python
$ python3 kafka_producer.py
```

If all goes well, you should be able to see new tables created for you in your docker GridDB container

```bash
$ docker exec -it griddb-server gs_sh
gs> searchcontainer
```

And these tables should be populated with the data created by our python script.

## Overview

As explained above, all of these various services are being built and run via docker compose; the nice thing about using a singluar compose file for all of our services is that Docker will automatically have them all on the same shared network. This means that our kafka broker service can already commnicate with the zookeeper and vice versa. This also means that our GridDB server is available to connected to via port `20001` (its SQL port) so that we may flush our Kafka data directly into it. 

You can take a look at the `docker-compose.yml` file to see how these various services are started, what images they are pulled from, and what kind of configuration we have set up. Mostly you just need to know that Kafka is doing most of the heavy lifting here. To allow Kafka to know where to push its data topics onto, we need to create what is known as a JDBC Sink Configuration file. This file contains all of the parameters we wish employ when setting up our data flow. So next, let's take a look at how we create and apply this config file.

### JDBC Sink Config

Our kafka-connect service is responsible for handling our third party integrations (JDBC in this case) and so we will need to push our config file there and apply it. The service ships with a REST API which allows us to push JSON files onto it. When it receives a JSON file, it will apply that config to your Kafka service. It will then handle all of our data flow. 

You can push up a JSON file whenever you'd like, but we will set it up so that once the kafka-connect service is ready, it will push the json file. Here's a look at the docker compose entry: 

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
        /usr/share/java,/etc/kafka-connect/jars,/usr/share/confluent-hub-components,/usr/share/confluent-hub-components/confluentinc-kafka-connect-jdbc/lib
      CLASSPATH: >-
        /usr/share/java,/etc/kafka-connect/jars,/usr/share/confluent-hub-components,/usr/share/confluent-hub-components/confluentinc-kafka-connect-jdbc/lib
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

        /scripts/create-griddb-sink.sh

        sleep infinity     
```

In the command section of this entry, you can see that we are checking kafka-connect (itself!) and waiting for the service to be ready (a 200 response to our HTTP Request). Once it's ready, we will run a script which will send over a json object in the body of an HTTP request. Here is what that script looks like: 

```bash
#!/bin/sh
curl -s \
     -X "POST" "http://localhost:8083/connectors/" \
     -H "Content-Type: application/json" \
     -d '{
            "name": "test-sink", 
            "config": {
                "connector.class":"io.confluent.connect.jdbc.JdbcSinkConnector",
                "tasks.max":"1",
                "topics.regex": "meter.(.*)",
                "table.name.format": "kafka_${topic}",
            	"dialect.name": "PostgreSqlDatabaseDialect",
                "transforms": "TimestampConverter",
        	    "transforms.TimestampConverter.type": "org.apache.kafka.connect.transforms.TimestampConverter$Value",
        	    "transforms.TimestampConverter.format": "yyyy-MM-dd hh:mm:ss.SSS",
        	    "transforms.TimestampConverter.field": "timestamp",
        	    "transforms.TimestampConverter.target.type": "string",
                "time.precision.mode": "connect",
                "connection.url":"jdbc:gs://griddb-server:20001/myCluster/public",
                "connection.user": "admin",
                "connection.password": "admin",
                "batch.size": "1000",
                "auto.create":"true",
                "pk.mode" : "none",
                "insert.mode": "insert",
                "auto.evolve": "true"
            }
}'
```

This is the information that will connect our GridDB server (container) with our running Kafka service through jdbc. Some of these entries are self-explanatory, such as connection url, user, pass, etc. I will go over some of the lesser known options.

For `topics.regex`, we are telling our Sink connector which topics to subscribe to. We will push data onto these topics via other means, and we will suspect that our Sink connector will find that data and push it out to our connection url. The entries related to `transforms` are about taking a string value of timestamp from the meter topic and converting it into an explicit timestamp data before pushing to the database.

Once you have pushed this information to the kafka-connect, you can make sure it's there by querying port 8083: 

```bash
$ curl http://localhost:8083/connectors
["test-sink"]
```

### Producing Data for Kafka Topics

We have successfully `subscribed` our Kafka sink connector to any topics which start with `meter`. Now let's `produce` some data and send that data to our topic. You can do this in any variety of ways, but here we will simply use a simple python script which will make 10 different topics and push timestamp data to all of those topics. Because our JDBC connector is subscribed to those topics, it will detect changes in those topics and eventually push that into GridDB.

```python
def produce(meterid, usagemodel=None):
    time = datetime.datetime.now()-datetime.timedelta(days=100)
    register_device(meterid)

    base_temp = random.uniform(-10,40)
    base_kwh = random.uniform(0,2)
    while True:
        now = time.strftime('%Y-%m-%d %H:%M:%S.%f')
        data= {
            "payload": 
            {
                'timestamp': now,
                'kwh': base_kwh+random.uniform(-.2, 2),
                'temp': base_temp+random.uniform(-5, 5) 
            },
            "schema": 
            {
                "fields": [ 
                    { "field": "timestamp", "optional": False, "type": "string" },
                    { "field": "kwh", "optional": False, "type": "double" }, 
                    { "field": "temp", "optional": False, "type": "double" } 
                ], 
                "name": "iot", "optional": False, "type": "struct" 
            }    
         }
        time = time + datetime.timedelta(minutes=60)
        if time > datetime.datetime.now():
            time.sleep(3600)

        m=json.dumps(data, indent=4, sort_keys=True, default=str)
        p.send("meter_"+str(meterid), m.encode('utf-8'))
        print("meter_"+str(meterid), data['payload'])
```

This is our function which will make our data and send to topics labeled `meter_${num}`. Our fields entry will be the schema which is pushed onto GridDB. 

Once you run this script, before checking GridDB itself, you can the topic like so: 

```bash
$ docker-compose exec broker kafka-console-consumer --bootstrap-server broker:9092 --topic meter_0 --from-beginning
```

This will show all of your data from the python script. 

And then next, we can of course check our actual GridDB instance: 

```bash
$ docker exec -it griddb-server gs_sh
gs[public]> searchcontainer
kafka_meter_0
kafka_meter_1
kafka_meter_2
kafka_meter_3
kafka_meter_4
kafka_meter_5
kafka_meter_6
kafka_meter_7
kafka_meter_8
kafka_meter_9
kafka_meters
gs[public]> select * from kafka_meter_0;
2,400 results. (4 ms)
gs[public]> get 10
+-------------------------+--------------------+--------------------+
| timestamp               | kwh                | temp               |
+-------------------------+--------------------+--------------------+
| 2023-11-15 01:43:52.299 | 2.8875713817453637 | 38.9091116816826   |
| 2023-11-15 02:43:52.299 | 1.8928477563702992 | 37.183344440257784 |
| 2023-11-15 03:43:52.299 | 1.3057612343055085 | 37.9251109201419   |
| 2023-11-15 04:43:52.299 | 1.1172883739759085 | 40.43478215590419  |
| 2023-11-15 05:43:52.299 | 1.6667172633034288 | 36.82843364324471  |
| 2023-11-15 06:43:52.299 | 2.5131139241648173 | 38.50469053566042  |
| 2023-11-15 07:43:52.299 | 2.0608077564559095 | 38.62901305523018  |
| 2023-11-15 08:43:52.299 | 2.9945117256967295 | 39.854084974922834 |
| 2023-11-15 09:43:52.299 | 1.8693091828037747 | 41.15482986965948  |
| 2023-11-15 10:43:52.299 | 1.0284230878567477 | 37.05776090626771  |
+-------------------------+--------------------+--------------------+
The 10 results had been acquired.
```

## Conclusion

In this article we have gone over how to set up Kafka to push data into your GridDB server using just JDBC and Docker.