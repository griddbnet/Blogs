#!/bin/bash

export JAVA_HOME=/usr/lib/jvm/java-11-openjdk-amd64
export CLASSPATH=$CLASSPATH:./lib/gridstore.jar:./lib/gridstore-arrow.jar:./lib/arrow-memory-netty.jar

export GRIDDB_NOTIFICATION_PROVIDER="http://dbaassharemp1gssta.blob.core.windows.net/dbaas-share-mp-griddb-blob/mfcloud8737.json"
export GRIDDB_CLUSTER_NAME="gs_clustermfcloud8737"
export GRIDDB_USERNAME="S016jp7ZJi-israel"
export GRIDDB_PASSWORD="israel"
export GRIDDB_DATABASE="o8O3h29r"
