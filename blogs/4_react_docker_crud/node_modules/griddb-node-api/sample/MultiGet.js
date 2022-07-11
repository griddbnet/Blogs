const griddb = require('griddb-node-api');

const factory = griddb.StoreFactory.getInstance();
const store = factory.getStore({
    "host": process.argv[2],
    "port": parseInt(process.argv[3]),
    "clusterName": process.argv[4],
    "username": process.argv[5],
    "password": process.argv[6]
});

const containerNameList = ["Sample_MultiGet1", "Sample_MultiGet2"];
const rowCount = 5;
const nameList = ["notebook PC", "desktop PC", "keyboard", "mouse", "printer"];
const numberList = [[108, 72, 25, 45, 62], [50, 11, 208, 23, 153]];
let predicateList = [];

//Create container, rows, rowkey predicate
const createContainerRowsPredicate = async function(containerName, count){
    let conInfo = new griddb.ContainerInfo({
        'name': containerName,
        'columnInfoList': [
            ["id", griddb.Type.INTEGER],
            ["productName", griddb.Type.STRING],
            ["count", griddb.Type.INTEGER]
        ],
        'type': griddb.ContainerType.COLLECTION, 'rowKey': true
    });
    let container = await store.putContainer(conInfo);
    console.log("Sample data generation: Create Collection name=%s",
            containerName);

    // Create rows
    let rows = [];
    for (let i = 0; i < rowCount; i++) {
        rows.push([
            i,
            nameList[i],
            numberList[count][i]
        ]);
    }
    await container.multiPut(rows);
    console.log("Sample data generation: Put Rows count=%d", rowCount);

    // Create rowkey predicate
    let predicate = store.createRowKeyPredicate(griddb.Type.INTEGER);
    predicate.setRange(0, 2);
    predicateList.push(predicate);
}

//Get data and print to output.
const getRowSet = function(query, containerName){
    rs = query.getRowSet();
    while (rs.hasNext()) {
        row = rs.next();
        console.log("FetchAll result: container=%s, row=(%d, \"%s\", %d)",
            containerName, row[0], row[1], row[2]);
    }
}

// Multi get data
const sampleMultiGet = async function(){
    for (const [i, containerName] of containerNameList.entries()) {
        await store.dropContainer(containerName);
        await createContainerRowsPredicate(containerName, i);
    }
    let param = {};
    for (let i = 0; i < containerNameList.length; i++) {
        param[containerNameList[i]] = predicateList[i];
    }
    const ret = await store.multiGet(param);
    for (let key in ret) {
        for (i = 0; i< ret[key].length; i++) {
            console.log("MultiGet: container=%s, id=%d, productName=%s, count=%d", key, ret[key][i][0], ret[key][i][1],
                ret[key][i][2]);
        }
    }
    console.log("Success!");
}

sampleMultiGet().catch(err => {
    console.log(err.message);
});
