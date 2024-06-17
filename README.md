GridDB running via Docker containers isn't a new topic. We have covered it before: [https://griddb.net/en/blog/run-a-griddb-server-in-docker-desktop/](https://griddb.net/en/blog/run-a-griddb-server-in-docker-desktop/) & [https://griddb.net/en/blog/improve-your-devops-with-griddb-server-and-client-docker-containers/](https://griddb.net/en/blog/improve-your-devops-with-griddb-server-and-client-docker-containers/). 

In this blog, we want to again touch on using GridDB on Docker, but will focus instead on using GridDB on ARM architecture, namely a Mac with Apple silicon (M1, M2, etc). So, in this blog, we will provide a docker image which works with ARM devices, as well as walk through how to spin up application containers to work in conjunction with your docker container service.

## Running GridDB & GridDB Applications with Docker

First, you can read the source code that accompanies this article here: [https://github.com/griddbnet/griddb-docker-arm](https://github.com/griddbnet/griddb-docker-arm). It contains the docker image itself which you can build to run on your ARM machine. It is also available for pulling from the GridDB.net [Dockerhub](https://hub.docker.com/r/griddbnet/griddb) page.

### Running GridDB Server

To pull and run this image: 

```bash
$ docker network create griddb-net
$ docker pull griddbnet/griddb
$ docker run --name griddb-server \
    --network griddb-net \
    -e GRIDDB_CLUSTER_NAME=myCluster \
    -e GRIDDB_PASSWORD=admin \
    -e NOTIFICATION_MEMBER=1 \
    -d -t griddbnet/griddb:5.5.0
```

These commands will create a network for your GridDB server and any containers you intend to run with it. It will also download the built image and then run the image on your machine. Once you confirm it's running, you can try running application code, using your GridDB container as the data store.

### Running Application Containers

First, here are the commands to run some node.js GridDB code against your containerized server:

```bash
$ git clone https://github.com/griddbnet/griddb-docker-arm.git
$ cd griddb-docker-arm/node-api/centos7_arm/
$ docker build -t griddb_node_app .
$ docker run --name griddb-node   --network griddb-net     -e
GRIDDB_CLUSTER_NAME=myCluster     -e GRIDDB_USERNAME=admin     -e
GRIDDB_PASSWORD=admin     -e IP_NOTIFICATION_MEMBER=griddb-server
griddb_node_app
```

First, we need to grab the source code which contains some modified files when compared to the official source code (changes to allow the C_Client to run on macos/ARM, which is required for non java programming language connectors). Then we build the image and run it, setting some options such as cluster name, user/pass combo, and finally the IP_NOTIFICATION_MEMBER which explictly tells the container the ip address of the GridDB server.

Of course here, when running this, you are simply running the sample code we have provided. But it lays out the framework for running your own GridDB nodejs code. You write your code, build the docker image, and then run it with explict case of choosing the docker network and pointing to the correct hostname/ip address.

To go along with the nodejs application interface, JDBC and Java have also been tested and confirmed to work with an ARM based Mac using an M1.

### Examples of Creating Application Container

To build and run your own application in docker, the process is simple: you write the application in your language of choice, write the Dockerfile for that application, and then finally build & run the container, ensuring the use the same network as used when running the GridDB container.

#### Node.js

For example, let's say you wrote a quick node.js script to ingest data as we did here: [previous blog](). NOTE: Source code for this example nodejs script is also included in the source code for this article.

To keep the application connection agnostic, you can keep the connection details as command line arguments, meaning when you run your docker container, you can simply enter in the docker container you wish to connect to. For example, here's a Dockerfile of a nodejs application we want to use with a docker griddb server: 

```bash
FROM node:18

# download c_client
WORKDIR /
RUN wget --no-check-certificate https://github.com/griddb/c_client/releases/download/v5.6.0/griddb-c-client_5.6.0_amd64.deb
RUN dpkg -i griddb-c-client_5.6.0_amd64.deb


WORKDIR /app
COPY package.json /app/package.json
COPY package-lock.json /app/package-lock.json
COPY gen-data.js /app/gen-data.js

RUN npm i

ENTRYPOINT ["node", "gen-data.js"]
```
The instructions are straight forward, we want to copy all source code and package information and build it into a docker container. The code itself expects command line arguments for the connection details: 

```javascript
var fs = require('fs');
var factory = griddb.StoreFactory.getInstance();
var store = factory.getStore({
    "notificationMember": process.argv[2],
    "clusterName": "myCluster",
    "username": "admin",
    "password": "admin"
});
```
So when we build this docker container, we can specify the connection details. Here are the full instructions of getting this running: 

```bash
$ docker build -t nodejs-gen-griddb .
```

We are building our current Dockerfile with the tag of `nodejs-gen-griddb`. Then we run it, specifying the connection details: 

```bash
$ docker run  --network griddb-net nodejs-gen-griddb griddb-server:10001
```

#### JDBC 

Here is another example, connecting to our GridDB server using Java and JDBC so that we can run SQL commands.

First, we create out java program. In this case, we simply want to make a connection and then create a new table. 

```java
    String notificationMember = args[0];
    String clusterName = args[1];
    String databaseName = args[2];
    // String notificationMember = "griddb-server:20001";
    // String clusterName = "myCluster";
    // String databaseName = "public";
    String username = "admin";
    String password = "admin";
    String encodeClusterName = URLEncoder.encode(clusterName, "UTF-8");
    String encodeDatabaseName = URLEncoder.encode(databaseName, "UTF-8");
    String jdbcUrl = "jdbc:gs://" + notificationMember + "/" + encodeClusterName + "/" + encodeDatabaseName;
    System.out.println(jdbcUrl);

    Properties prop = new Properties();
    prop.setProperty("user", username);
    prop.setProperty("password", password);

    con = DriverManager.getConnection(jdbcUrl, prop);

    System.out.println("Connected to cluster via SQL Interface");

    String SQL = "CREATE TABLE IF NOT EXISTS devices (ts TIMESTAMP PRIMARY KEY, co DOUBLE, humidity DOUBLE,light BOOL,lpg DOUBLE,motion BOOL,smoke DOUBLE,temp DOUBLE) USING TIMESERIES WITH (expiration_type='PARTITION',expiration_time=90,expiration_time_unit='DAY') PARTITION BY RANGE (ts) EVERY (60, DAY)SUBPARTITION BY HASH (ts) SUBPARTITIONS 64;";

    Statement stmt = con.createStatement();
    stmt.executeUpdate(SQL);
    System.out.println("Successfully created container called: devices");
```

And now we create the dockerfile to build this java program to be run against the GridDB server.

```bash
FROM alpine:3.14

WORKDIR /app
RUN apk add --no-cache wget
RUN apk add openjdk11

RUN wget https://repo1.maven.org/maven2/com/github/griddb/gridstore-jdbc/5.6.0/gridstore-jdbc-5.6.0.jar
ENV CLASSPATH /app/gridstore-jdbc-5.6.0.jar

COPY ./src ./src
WORKDIR /app/src/main/java/
RUN javac net/griddb/jdbc/Jdbc.java

CMD ["java",  "net/griddb/jdbc/Jdbc.java", "griddb-server:20001", "myCluster", "public"]
```

For this build process, we install java and wget, download the latest griddb jdbc driver, add it to our class path environment, and then simply compile and run our java code. If all goes well, you should be able to run the docker image and set the network to be equal to where your GridDB server is connected and have it work that way.

In this case, we left the command line arguments within the Dockerfile itself, meaning you can simply change how the code is executed to keep it flexible.

## Conclusion

And now you should be able to run both nodejs and JDBC containers on your ARM devices. If you get other programming languages running ony our machines, please let us know.

