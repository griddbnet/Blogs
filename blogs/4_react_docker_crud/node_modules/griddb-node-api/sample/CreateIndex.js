var griddb = require('griddb-node-api');
var fs = require('fs');

var factory = griddb.StoreFactory.getInstance();
var store = factory.getStore({
    "host": process.argv[2],
    "port": parseInt(process.argv[3]),
    "clusterName": process.argv[4],
    "username": process.argv[5],
    "password": process.argv[6]
});
var containerName = 'Sample_Index';
var conInfo = new griddb.ContainerInfo({
    'name': containerName,
    'columnInfoList': [
        ["id", griddb.Type.INTEGER],
        ["productName", griddb.Type.STRING],
        ["count", griddb.Type.INTEGER]
    ],
    'type': griddb.ContainerType.COLLECTION, 'rowKey': true
});
var container;

store.dropContainer(containerName)
    .then(() => {
        return store.putContainer(conInfo);
    })
    .then(cont => {
        console.log("Create Collection name=%s", containerName);
        container = cont;
        return container.createIndex({columnName: 'count',indexType: griddb.IndexType.HASH, name: 'hash_index'});
    })
        .then(() => {
            console.log("Create Index");
            console.log("Success!");
            return true;
    })
    .catch(err => {
        console.log(err);
    });
