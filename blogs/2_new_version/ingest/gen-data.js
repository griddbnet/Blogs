const griddb = require('griddb-node-api');
const process = require('process');

var fs = require('fs');
var factory = griddb.StoreFactory.getInstance();
var store = factory.getStore({
    "notificationMember": process.argv[2],
    "clusterName": "myCluster",
    "username": "admin",
    "password": "admin"
});

const conInfo = new griddb.ContainerInfo({
    'name': "compressionBlog",
    'columnInfoList': [
        ["timestamp", griddb.Type.TIMESTAMP],
        ["location", griddb.Type.STRING],
        ["data", griddb.Type.FLOAT],
        ["temperature", griddb.Type.FLOAT],
    ],
    'type': griddb.ContainerType.COLLECTION, 'rowKey': false
});

function getRandomFloat(min, max) {
    return Math.random() * (max - min) + min;
}

const putCont = async (sensorCount, data, temperature) => {
    const rows = generateSensors(sensorCount, data, temperature);
    try {
        const cont = await store.putContainer(conInfo)
        await cont.multiPut(rows);
    } catch (error) {
        console.log("error: ", error)
    }
}

const generateSensors = (sensorCount, data, temperature) => {
    const arr = []
    let now = new Date();
    for (let i = 1; i <= sensorCount; i++) {
        let tmp = [];
        let newTime = now.setMilliseconds(now.getMinutes() + i)
        tmp.push(newTime)
        tmp.push("A1")
        tmp.push(data)
        tmp.push(temperature)
        arr.push(tmp)
    }
//    console.log("arr: ", arr)
    return arr;
}


const AMTROWS = 10000;
const AMTPASSES = 10000;

(async () => {
    try {
        console.log("attempting to gen data and push to GridDB")
        console.time("gen-ingest");
        for (let i = 0; i < AMTPASSES; i++) {
            const data = parseFloat(getRandomFloat(1, 10).toFixed(2))
            const temperature = parseFloat(getRandomFloat(60, 130).toFixed(2))
            await putCont(AMTROWS, data, temperature);
        }
        console.timeEnd("gen-ingest");
        console.log("Finished pushing data!")
    } catch (error) {
        console.log("Error putting to container", error);
    }
})();