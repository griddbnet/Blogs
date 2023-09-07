const griddb = require('griddb-node-api');
const express = require('express');
const { config } = require('dotenv');
var bodyParser = require('body-parser')


const app = express();
app.use(bodyParser.json({ type: 'application/*+json' }))
var jsonParser = bodyParser.json()

var factory = griddb.StoreFactory.getInstance();
var store = factory.getStore({
    "notificationMember": "griddb-server:10001",
    "clusterName": "myCluster",
    "username": "admin",
    "password": "admin"
});

const conInfo = (containerId) => {
    let containerName = "meter_" + containerId
    console.log("container information: ", containerName)
    let ci = new griddb.ContainerInfo({
        'name': containerName,
        'columnInfoList': [
            ["timestamp", griddb.Type.TIMESTAMP],
            ["kwh", griddb.Type.DOUBLE],
            ["temp", griddb.Type.DOUBLE],
        ],
        'type': griddb.ContainerType.COLLECTION, 'rowKey': true
    })
    return ci
}


const queryCont = async (containerName, queryStr, numOfPoints) => {

    var data = [];
    try {
        // console.log("containerName: ", containerName, queryStr, numOfPoints);
        const col = await store.getContainer(containerName)
        const query = await col.query(queryStr)
        const rs = await query.fetch(query)
        let i = 0;
        while (rs.hasNext()) {
            if (i < numOfPoints) {
                let temp = {}
                temp["timestamp"] = rs.next()[0]
                temp["kwh"] = rs.next()[1]
                temp["temp"] = rs.next()[2]
                data.push(temp)
                i++
            } else {
                break;
            }
        }
        return data
    } catch (error) {
        console.log("error: ", error)
    }
}

const updateRow = async (newRow, id) => {
    try {
        let ci = conInfo(id)
        const cont = await store.putContainer(ci)
        console.log("rew row: ", newRow)
        const res = await cont.put(newRow)
        return res
    } catch (err) {
        console.log("update row error: ", err)
    }
}

const deleteRow = async (timestamp, id) => {
    console.log("Row to be deleted: ", timestamp)
    try {
        let ci = conInfo(id)
        const cont = await store.putContainer(ci)
        let res = await cont.remove(timestamp)
        console.log("Row deleted: ", res, timestamp)

        return true
    } catch (err) {
        console.log("update row error: ", err)
    }

}

function randomRange(min, max) {
    let cal = (Math.random() * (max - min) + min);
    return parseFloat(cal);
   }

const createRow = async (id) => {
    console.log("Row to be created ")
    const newRow = []
    var now = new Date();
    let randkwh = randomRange(1.5, 9.5)
    let randTemp = randomRange(45.5, 99.5)
    newRow.push(now.toISOString());
    newRow.push(randkwh)
    newRow.push(randTemp)
    try {
        let ci = conInfo(id)
        const cont = await store.putContainer(ci)
        const res = await cont.put(newRow)
        return res
    } catch (err) {
        console.log("update row error: ", err)
    }

}


app.get('/get', async (req, res) => {
    try {
        var id = req.query.id;
        var numOfPoints = req.query.points;
        let queryStr = "select *"
        let containerName = "meter_" + id;
        var results = await queryCont(containerName, queryStr, numOfPoints);
        res.json({
            results
        });
    } catch (error) {
        console.log("try error: ", error)
    }
});

app.put("/update", jsonParser, async (req, res) => {
    const newRowObj = req.body
    const id = req.query.id;
    try {
        let x = await updateRow(newRowObj, id)
        console.log("return of update row: ", x)
        res.status(200).json(true);
    } catch (err) {
        console.log("update endpoitn failure: ", err)
    }
});

app.post("/create", jsonParser, async (req, res) => {
    const id = req.query.id;
    try {
        let x = await createRow(id)
        res.status(200).json(true)
    } catch (err) {
        console.log("delete row endpoint failure: ", err)
    }
})


app.post("/delete", jsonParser, async (req, res) => {
    const timestamp = req.query.timestamp
    const id = req.query.id;
    try {
        let x = await deleteRow(timestamp, id)
        res.status(200).json(true)
    } catch (err) {
        console.log("delete row endpoint failure: ", err)
    }
})

app.listen(8080, () => {
    console.info(
        'server/build/app.js: express.js server app is now running locally on port: ' +
        8080
    );
});