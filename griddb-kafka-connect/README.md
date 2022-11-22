GridDB Kafka Connector

# Overview

The Griddb Kafka Connector is a Kafka Connector for loading data to and from GridDB database with [Apache Kafka](https://kafka.apache.org/).

# Operating environment

Building of the library and execution of the sample programs have been checked in the following environment.

    OS: CentOS 7.9(x64) / Ubuntu 20.04(x64)
    Java: 1.8
    Maven: 3.5.0
    Kafka: 2.12-2.5.0
    GridDB server: V5.0 CE

# Build

```console
cd GRIDDB_KAFKA_CONNECTOR_FOLDER
mvn clean install
```

# Run

After build, put jar file GRIDDB_KAFKA_CONNECTOR_FOLDER/target/griddb-kafka-connector-X.X.X.jar to KAFKA_FOLDER/libs.
Start Kafka server.

Note: X.X.X is the software version.

There are 2 sample config files in config folder to run Sink and Source connector.

## Run the sink connector with the sample config file

```console
cd KAFKA_FOLDER/
./bin/connect-standalone.sh config/connect-standalone.properties GRIDDB_KAFKA_CONNECTOR_FOLDER/config/griddb-sink.properties
```

## Run the source connector with the sample config file

```console
cd KAFKA_FOLDER/
./bin/connect-standalone.sh config/connect-standalone.properties GRIDDB_KAFKA_CONNECTOR_FOLDER/config/griddb-source.properties
```

# Config parameters for sink connector in the config file

|Parameter   | Description  | Default Value   |
|---|---|---|
|connector.class|the sink connector class|com.github.griddb.GriddbSinkConnector|
|name|the connector name|   |
|topics.regex|list of topics is used by the sink connector|   |
|host|GridDB host (can use with both multicase and master node mode)|   |
|port|GridDB port (can use with both multicase and master node mode)|   |
|clusterName|GridDB cluster name|   |
|user|GridDB username|   |
|password|GridDB user password|   |
|notificationMember|GridDB notifiction member list in fix list method|   |
|batch.size|the size of write buffer to GridDB|3000|
|multiput|using multiput or single put in write buffer|true|
|container.name.format|using it to change to topic name from GridDB container|$(topic): The default container name is topic name |

In file config/griddb-sink.properties : the config values (connector.class, name, topics.regex, transforms) is the properties use by Kafka, not the connector).

# Config parameters for source connector in the config file

|Parameter   | Description  | Default Value   |
|---|---|---|
|connector.class|the source connector class|com.github.griddb.GriddbSourceConnector|
|name|the connector name|   |
|host|GridDB host (can use with both multicase and master node mode)|   |
|port|GridDB port (can use with both multicase and master node mode)|   |
|clusterName|GridDB cluster name|   |
|user|GridDB username|   |
|password|GridDB user password|   |
|notificationMember|GridDB notifiction member list in fix list method|   |
|containers|list of GridDB containers used by the source connector|   |
|mode|the mode to import (bulk/timestamp)|   |
|timestamp.column.name|the list of timestamp column in timestamp mode|   |
|topic.prefix|the prefix of output topic|   |
|mode|the mode to import (bulk/timestamp)|   |

In file config/griddb-source.properties : the config values (connector.class, name is the properties use by Kafka, not the connector).

# Function

(available)
- STRING, BOOL, BYTE, SHORT, INTEGER, LONG, FLOAT, DOUBLE, TIMESTAMP, BLOB type for GridDB

(not available)
- GEOMETRY, ARRAY type for GridDB

# Community

  * Issues  
    Use the GitHub issue function if you have any requests, questions, or bug reports. 
  * PullRequest  
    Use the GitHub pull request function if you want to contribute code.
    You'll need to agree GridDB Contributor License Agreement(CLA_rev1.1.pdf).
    By using the GitHub pull request function, you shall be deemed to have agreed to GridDB Contributor License Agreement.

# License
  
  This GridDB Kafka Connector source is licensed under the Apache License, version 2.0.
  
# Trademarks
  
  Apache Kafka, Kafka are either registered trademarks or trademarks of the Apache Software Foundation in the United States and/or other countries.
