const griddb = require('griddb-node-api');
var { parse } = require('csv-parse');

var fs = require('fs');
var factory = griddb.StoreFactory.getInstance();
var store = factory.getStore({
    "notificationMember": "127.0.0.1:10001",
    "clusterName": "myCluster",
    "username": "admin",
    "password": "admin"
});

const containerNameList = ["device1", "device2", "device3"];
allData = {
    "device1": [],
    "device2": [],
    "device3": []
}

const createContainer = async function (containerName) {
    const conInfo = new griddb.ContainerInfo({
        'name': containerName,
        'columnInfoList': [
            ["ts", griddb.Type.TIMESTAMP],
            ["co", griddb.Type.DOUBLE],
            ["humidity", griddb.Type.DOUBLE],
            ["light", griddb.Type.BOOL],
            ["lpg", griddb.Type.DOUBLE],
            ["motion", griddb.Type.BOOL],
            ["smoke", griddb.Type.DOUBLE],
            ["temp", griddb.Type.DOUBLE],
        ],
        'type': griddb.ContainerType.TIME_SERIES, 'rowKey': true
    });
    console.log("Create container :" + containerName);
    await store.putContainer(conInfo);
    console.log("Create Collection name=%s", containerName);
}

let i = 1
const parseCSV = async () => {
    console.log("Starting parse of csv")
    const readStream = fs.createReadStream(__dirname + '/iot_telemetry_data.csv').pipe(parse({ columns: true }))

    readStream.on('data', async (row) => {

        let ts = parseFloat(row['ts'])
        let timestamp = new Date(ts * 1000)
        let device = row['device']
        device = device.replace(/:\s*/g, ""); //removing colons between characters in device name 
        switch (device) {
            case "b827ebbf9d51": 
                device = "device1"
                break;
            case "000f0070910a": 
                device = "device2"
                break;
            case "1cbfce15ec4d": 
                device = "device3"
                break;
            default: 
                device = "device"
        }

        let co = parseFloat(row['co'])
        let humidity = parseFloat(row['humidity'])
        let light = JSON.parse(row['light'])
        let lpg = parseFloat(row['lpg'])
        let motion = JSON.parse(row['motion'])
        let smoke = parseFloat(row["smoke"])
        let temp = parseFloat(row["temp"])

        let r = [
            timestamp,
            co,
            humidity,
            light,
            lpg,
            motion,
            smoke,
            temp
        ]
        allData[device].push(r)
        if (i % 10000 == 0) {
            console.log("PUTTING DATA i: ", i);
            sampleMultiPut(allData).catch(err => {
                console.log(err);
            });
            allData['device1'].length = 0
            allData['device2'].length = 0
            allData['device3'].length = 0
        }

        i++
    })
        .on('end', () => {
            console.log('CSV file successfully processed. Putting in the remaining rows: ', allData['device1'].length, allData['device2'].length, allData['device3'].length)
            sampleMultiPut(allData).catch(err => {
                console.log(err);
            });
        });
}

(async () => {
    for (const containerName of containerNameList) {
        await store.dropContainer(containerName);
        await createContainer(containerName);
    }
    await parseCSV();
})();

const sampleMultiPut = async function (data) {
    await store.multiPut(data);
}
