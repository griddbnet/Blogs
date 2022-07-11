const griddb = require('griddb-node-api');

const factory = griddb.StoreFactory.getInstance();
const store = factory.getStore({
    "host": process.argv[2],
    "port": parseInt(process.argv[3]),
    "clusterName": process.argv[4],
    "username": process.argv[5],
    "password": process.argv[6]
});

const containerNameList = ["Sample_FetchAll1", "Sample_FetchAll2"];
const tqlList = ["select * where count > 60", "select * where count > 100"];
const rowCount = 5;
const nameList = ["notebook PC", "desktop PC", "keyboard", "mouse", "printer"];
const numberList = [[108, 72, 25, 45, 62], [50, 11, 208, 23, 153]];
let queryList = [];

//Create container, rows, query
const createContainerRowsQuery = async function(containerName, count){
    const conInfo = new griddb.ContainerInfo({
        'name': containerName,
        'columnInfoList': [
            ["id", griddb.Type.INTEGER],
            ["productName", griddb.Type.STRING],
            ["count", griddb.Type.INTEGER]
        ],
        'type': griddb.ContainerType.COLLECTION, 'rowKey': true
    });
    let container = await store.putContainer(conInfo);

    // Create rows
    console.log("Sample data generation: Create Collection name=%s",
            containerName);
    let rows = [];
    for (let i=0;i < rowCount;i++) {
        let row = [];
        row[0] = i;
        row[1] = nameList[i];
        row[2] = numberList[count][i];
        rows.push(row);
    }
    await container.multiPut(rows);
    console.log("Sample data generation: Put Rows count=%d", rowCount);

    // Create query
    const query = await container.query(tqlList[count]);
    console.log("FetchAll query : container=%s, tql=%s", containerName, tqlList[count]);
    queryList.push(query);
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

// Fetch all data
const sampleFetchAll = async function(){
    for (const [i, containerName] of containerNameList.entries()) {
        await store.dropContainer(containerName);
        await createContainerRowsQuery(containerName, i);
    }
    await store.fetchAll(queryList);
    queryList.forEach(function(query, i) {
        return getRowSet(query, containerNameList[i]);
    });
}

sampleFetchAll().catch(err => {
    console.log(err.message);
});
