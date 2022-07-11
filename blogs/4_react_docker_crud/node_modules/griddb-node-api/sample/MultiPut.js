const griddb = require('griddb-node-api');

const factory = griddb.StoreFactory.getInstance();
const store = factory.getStore({
    "host": process.argv[2],
    "port": parseInt(process.argv[3]),
    "clusterName": process.argv[4],
    "username": process.argv[5],
    "password": process.argv[6]
});

const containerNameList = ["Sample_MultiPut1", "Sample_MultiPut2"];
const rowCount = 5;
const nameList = ["notebook PC", "desktop PC", "keyboard", "mouse", "printer"];
const numberList = [[108, 72, 25, 45, 62], [50, 11, 208, 23, 153]];

//Create container
const createContainer = async function(containerName) {
    const conInfo = new griddb.ContainerInfo({
        'name': containerName,
        'columnInfoList': [
            ["id", griddb.Type.INTEGER],
            ["productName", griddb.Type.STRING],
            ["count", griddb.Type.INTEGER]
        ],
        'type': griddb.ContainerType.COLLECTION, 'rowKey': true
    });
    console.log("Create container :" + containerName);
    await store.putContainer(conInfo);
    console.log("Create Collection name=%s", containerName);
}

// Multi put data
const sampleMultiPut = async function() {

    for (const containerName of containerNameList) {
        await store.dropContainer(containerName);
        await createContainer(containerName);
    }

    let rows = [];
    for (let count=0; count < containerNameList.length; count++) {
        let arr = [];
        for (let i=0;i < rowCount;i++) {
            let row = [];
            row[0] = i;
            row[1] = nameList[i];
            row[2] = numberList[count][i];
            arr.push(row);
        }
        rows.push(arr);
    }

    let param = {};
    for (let i = 0; i < containerNameList.length; i++) {
        param[containerNameList[i]] = rows[i];
    }
    await store.multiPut(param);
    console.log("Multi Put");
    console.log("Success!");
}

sampleMultiPut().catch(err => {
    console.log(err.message);
});
