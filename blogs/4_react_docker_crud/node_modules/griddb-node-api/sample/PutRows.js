const griddb = require('griddb-node-api');

const factory = griddb.StoreFactory.getInstance();
const store = factory.getStore({
    "host": process.argv[2],
    "port": parseInt(process.argv[3]),
    "clusterName": process.argv[4],
    "username": process.argv[5],
    "password": process.argv[6]
});
const containerName = 'Sample_PutRows';
const conInfo = new griddb.ContainerInfo({
    'name': containerName,
    'columnInfoList': [
        ["id", griddb.Type.INTEGER],
        ["productName", griddb.Type.STRING],
        ["count", griddb.Type.INTEGER]
    ],
    'type': griddb.ContainerType.COLLECTION, 'rowKey': true
});

const samplePutRows = async function(){
    await store.dropContainer(containerName);
    let container = await store.putContainer(conInfo);
    console.log("Create Collection name=%s", containerName);
    let rows = [];
    const rowCount = 5;
    for (var i = 0;i < rowCount; i++) {
        rows.push([i, "dvd", i * 10]);
    }
    await container.multiPut(rows);
    console.log("Put Rows");
    console.log("Success!");
}

samplePutRows().catch(err => {
    console.log(err.message);
});
