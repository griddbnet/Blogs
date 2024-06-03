GridDB running on Docker isn't a new topic. We have covered it before: [https://griddb.net/en/blog/run-a-griddb-server-in-docker-desktop/](https://griddb.net/en/blog/run-a-griddb-server-in-docker-desktop/) & [https://griddb.net/en/blog/improve-your-devops-with-griddb-server-and-client-docker-containers/](https://griddb.net/en/blog/improve-your-devops-with-griddb-server-and-client-docker-containers/). 

In this blog, we want to again touch on using GridDB on docker, but will focus instead on using GridDB on ARM archiecture, namely a mac using Apple silicon (M1, M2, etc). So, in this blog, we will provide a docker image which works with ARM devices, as well as walk through how to spin up application containers to work in conjunction with your docker container service.

## Running GridDB on Docker

First, you can read the source code that accompanies this article here: [https://github.com/griddbnet/griddb-docker-arm](https://github.com/griddbnet/griddb-docker-arm). It contains the docker image itself which you can build to run on your ARM machine. The docker image itself is also built and ready for immediate consumption on our Dockerhub page: [Dockerhub](https://hub.docker.com/r/griddbnet/griddb).

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