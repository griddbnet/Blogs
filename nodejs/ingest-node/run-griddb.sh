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
    cp node-api-${GRIDDB_NODE_API_VERSION}/sample/sample1.js .
    node gen-data.js $NOTIFICATION_ADDRESS $NOTIFICATION_PORT $GRIDDB_CLUSTER_NAME $GRIDDB_USERNAME $GRIDDB_PASSWORD
else
    echo "Run GridDB node_api client with GridDB server mode FixedList : $IP_NOTIFICATION_MEMBER:10001 $GRIDDB_CLUSTER_NAME $GRIDDB_USERNAME $GRIDDB_PASSWORD"
    source ~/.nvm/nvm.sh && nvm use 20.
    node gen-data.js $IP_NOTIFICATION_MEMBER:10001 $GRIDDB_CLUSTER_NAME $GRIDDB_USERNAME $GRIDDB_PASSWORD
fi
