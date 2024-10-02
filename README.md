GridDB running via Docker containers isn't a new topic. We have covered it before: [https://griddb.net/en/blog/run-a-griddb-server-in-docker-desktop/](https://griddb.net/en/blog/run-a-griddb-server-in-docker-desktop/) & [https://griddb.net/en/blog/improve-your-devops-with-griddb-server-and-client-docker-containers/](https://griddb.net/en/blog/improve-your-devops-with-griddb-server-and-client-docker-containers/). 

In this blog, we want to again touch on using GridDB on Docker, but will focus instead on using GridDB on ARM architecture, namely a Mac with Apple silicon (M1, M2, etc). So, in this blog, we will provide a docker image which works with ARM devices, as well as walk through how to spin up application containers to work in conjunction with your docker container service.

## Running GridDB & GridDB Applications with Docker

First, you can read the source code that accompanies this article here: [https://github.com/griddbnet/griddb-docker-arm](https://github.com/griddbnet/griddb-docker-arm). It contains the docker image itself which you can build to run on your ARM machine. 

The image itself is also available for pulling from the GridDB.net [Dockerhub](https://hub.docker.com/r/griddbnet/griddb) page. The full image/tag name is: `griddbnet/griddb:arm-5.5.0`

### Running GridDB Server

To pull and run this image: 

```bash
$ docker network create griddb-net
$ docker pull griddbnet/griddb:arm-5.5.0
$ docker run --name griddb-server \
    --network griddb-net \
    -e GRIDDB_CLUSTER_NAME=myCluster \
    -e GRIDDB_PASSWORD=admin \
    -e NOTIFICATION_MEMBER=1 \
    -d -t griddbnet/griddb:arm-5.5.0
```

These commands will create a network for your GridDB server and any containers you intend to run with it. It will also download the built image and then run the image on your machine. Once you confirm it's running, you can try running application code, using your GridDB container as the data store.

### Running Application Containers

First, let's grab the source code and build our nodejs container to run some arbitrary code using GridDB as our connection.

```bash
$ git clone https://github.com/griddbnet/Blogs.git --branch docker-arm
```

Next, here are the commands to run some node.js GridDB code against your containerized server.

First, let's run the sample code that accompanies the official node.js GridDB repo

```bash
$ cd Blogs/nodejs/node-api
$ docker build -t griddb_node_app .
$ docker run --name griddb-node --network griddb-net -e GRIDDB_CLUSTER_NAME=myCluster -e GRIDDB_USERNAME=admin -e GRIDDB_PASSWORD=admin -e IP_NOTIFICATION_MEMBER=griddb-server griddb_node_app
```

First, we need to grab the source code which contains some modified files when compared to the official source code (changes to allow the C_Client to run on macos/ARM, which is required for non java programming language connectors). Then we build the image and run it, setting some options such as cluster name, user/pass combo, and finally the IP_NOTIFICATION_MEMBER which explictly tells the container the ip address of the GridDB server container.

Of course here, when running this, you are simply running the sample code provided, not your own. But it also lays out the framework for running your own GridDB nodejs code. The flow is as follows: you write your code, build the docker image, and then run it with explict case of choosing the docker network and pointing to the correct hostname/ip address.

To go along with the nodejs application interface, JDBC and Java have also been tested and confirmed to work with an ARM based Mac using an M-series chip.

### Examples of Creating Application Container

To build and run your own application in docker, the process is simple: you write the application in your language of choice, write the Dockerfile for that application, and then finally build & run the container, ensuring the use the same network as used when running the GridDB container.

#### Node.js

For example, let's say you wrote a quick node.js script to generate some 'fake' data.

To keep the application connection agnostic, you can keep the connection details as command line arguments, meaning when you run your docker container, you can simply enter in the docker container you wish to connect to similar to how it was done above. If you enter in the environment details when running the docker container. These details will then be picked up by our entry point script.

Here is the Dockerfile for installing the GridDB Node.js connector, along with the c_client connector on an ARM machine. Most of the file is installing everything necessary, including installing the included c_client rpm file. In this instance, we are simply copying over the one file we want to run (`gen-data.js`) along with the entrypoint script.

```bash
FROM rockylinux:9.3

ENV GRIDDB_NODE_API_VERSION=0.8.5
ENV NODE_PATH=/root/node-api-${GRIDDB_NODE_API_VERSION}

# Install griddb server
RUN set -eux \
    && dnf update -y \
    # Install nodejs version 16.x and c client for griddb nodejs_client
    && dnf install -y curl make python3 tar --allowerasing \
    && dnf groupinstall -y 'Development Tools'

RUN curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.40.1/install.sh | bash

RUN source ~/.nvm/nvm.sh && nvm install 20 && nvm use 20


COPY ./lib/griddb-c-client-5.5.0-linux.aarch64.rpm /
RUN rpm -Uvh /griddb-c-client-5.5.0-linux.aarch64.rpm

SHELL ["/bin/bash", "--login", "-c"]
# Copy entrypoint script and sample for fixlist
RUN mkdir /app
COPY run-griddb.sh gen-data.js /app/

WORKDIR /root

# Install nodejs client
RUN curl -L https://github.com/griddb/node-api/archive/refs/tags/${GRIDDB_NODE_API_VERSION}.tar.gz -o ${GRIDDB_NODE_API_VERSION}.tar.gz -sS \
    && tar -xzvf ${GRIDDB_NODE_API_VERSION}.tar.gz \
    && cd node-api-${GRIDDB_NODE_API_VERSION} 

WORKDIR /root/node-api-${GRIDDB_NODE_API_VERSION}
RUN  npm install 
RUN rm ../${GRIDDB_NODE_API_VERSION}.tar.gz

WORKDIR /app
# Set permission executable for script
RUN chmod a+x run-griddb.sh

# Run sample
CMD ["/bin/bash", "run-griddb.sh"]
```

And here is the simple `run-griddb.sh` script. All it does is basically run the node command with the proper arg details to connect to our GridDB docker container.

```bash
#!/bin/bash

if [ -z "$GRIDDB_CLUSTER_NAME" ]; then
    GRIDDB_CLUSTER_NAME='dockerGridDB'
fi

if [ -z "$NOTIFICATION_ADDRESS" ]; then
    NOTIFICATION_ADDRESS=239.0.0.1
fi

if [ -z "$NOTIFICATION_PORT" ]; then
    NOTIFICATION_PORT=31999
fi

if [ -z "$GRIDDB_USERNAME" ]; then
    GRIDDB_USERNAME='admin'
fi

if [ -z "$GRIDDB_PASSWORD" ]; then
    GRIDDB_PASSWORD='admin'
fi

if [ -z "$IP_NOTIFICATION_MEMBER" ]; then
    echo "Run GridDB node_api client with GridDB server mode MULTICAST : $NOTIFICATION_ADDRESS $NOTIFICATION_PORT $GRIDDB_CLUSTER_NAME $GRIDDB_USERNAME $GRIDDB_PASSWORD"
    source ~/.nvm/nvm.sh && nvm use 20
    node sample1.js $NOTIFICATION_ADDRESS $NOTIFICATION_PORT $GRIDDB_CLUSTER_NAME $GRIDDB_USERNAME $GRIDDB_PASSWORD
else
    echo "Run GridDB node_api client with GridDB server mode FixedList : $IP_NOTIFICATION_MEMBER:10001 $GRIDDB_CLUSTER_NAME $GRIDDB_USERNAME $GRIDDB_PASSWORD"
    source ~/.nvm/nvm.sh && nvm use 20.
    node gen-data.js $IP_NOTIFICATION_MEMBER:10001 $GRIDDB_CLUSTER_NAME $GRIDDB_USERNAME $GRIDDB_PASSWORD
fi
```

```bash
$ docker build -t nodejs-gen-griddb .
```

We are building our current Dockerfile with the tag of `nodejs-gen-griddb`. Then we run it, specifying the connection details: 

```bash
$ docker run  --network griddb-net -e GRIDDB_CLUSTER_NAME=myCluster -e GRIDDB_USERNAME=admin -e GRIDDB_PASSWORD=admin -e IP_NOTIFICATION_MEMBER=griddb-server nodejs-gen-griddb 
```

#### JDBC 

Here is another example, connecting to our GridDB server using Java and JDBC so that we can run SQL commands.

First, we create our java program. In this case, we simply want to make a connection and then create a new table. 

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

And now you should be able to run both nodejs and JDBC containers on your ARM devices. If you get other programming languages running ony our machines, please let us know in the GridDB forum: [https://forum.griddb.net](https://forum.griddb.net)
