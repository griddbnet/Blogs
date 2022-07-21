const { config } = require('dotenv');
const express = require('express');
const path = require('path');
const griddb = require('griddb-node-api');
var bodyParser = require('body-parser')

const app = express();
var jsonParser = bodyParser.json()

app.use(bodyParser.json({ type: 'application/*+json' }))
app.use(express.static(path.resolve(__dirname, 'frontend/build')));

var factory = griddb.StoreFactory.getInstance();
store = factory.getStore({
    "notificationMember": process.argv[2],
    "clusterName": process.argv[3],
    "username": process.argv[4],
    "password": process.argv[5]
});


//store = factory.getStore({
//    "host": process.argv[2],
//    "port": parseInt(process.argv[3]),
//    "clusterName": process.argv[4],
//    "username": process.argv[5],
//    "password": process.argv[6]
//});

 const queryCont = async (queryStr) => {

     var data = []
     try {
         const col = await store.getContainer(containerName)
         const query = await col.query(queryStr)
         const rs = await query.fetch(query)
         while(rs.hasNext()) {
             data.push(rs.next())
         }
         return data
     } catch (error) {
         console.log("error: ", error)
     }
}

var containerName = 'sensorsblog';

const conInfo = new griddb.ContainerInfo({
    'name': containerName,
    'columnInfoList': [
        ["timestamp", griddb.Type.TIMESTAMP],
        ["location", griddb.Type.STRING],
        ["data", griddb.Type.FLOAT],
        ["temperature", griddb.Type.FLOAT],
    ],
    'type': griddb.ContainerType.TIME_SERIES, 'rowKey': true
});

const updateRow = async (newRow) => {
    try {
        const cont = await store.putContainer(conInfo)
        const res = await cont.put(newRow)
        return res
    } catch (err) {
        console.log("update row error: ", err)
    }
}

const deleteRow = async (rows) => {
    console.log("Rows to be deleted: ", rows)
    var rowKeys = []
    rows.forEach ( row => {
        rowKeys.push(row.timestamp)
    })
    console.log("row keys: ", rowKeys) 
    try {
        const cont = await store.putContainer(conInfo)
        rowKeys.forEach ( async rowKey => {
            let res = await cont.remove(rowKey)
            console.log("Row deleted: ", res, rowKey)
        })
        return true
    } catch (err) {
        console.log("update row error: ", err)
    }

}

config();

function getRandomFloat(min, max) {
    return Math.random() * (max - min) + min;
  }


const putCont = async (sensorCount) => {
    console.log("Putting Container")
    const rows = generateSensors(sensorCount);
    try {
        await store.dropContainer(containerName);
        const cont = await store.putContainer(conInfo)
        await cont.multiPut(rows);
    } catch (error) {
        console.log("error: ", error)
    }
}

const generateSensors = (sensorCount) => {

    let numSensors = sensorCount
    let arr = []
    console.log("Generating sensors")

    for (let i = 1; i <= numSensors; i++) {
        let tmp = [];
        let now = new Date();
        let newTime = now.setMilliseconds(now.getMinutes() + i)
        let data = parseFloat(getRandomFloat(1, 10).toFixed(2))
        let temperature = parseFloat(getRandomFloat(60, 130).toFixed(2))
        tmp.push(newTime)
        tmp.push("A1")
        tmp.push(data)
        tmp.push(temperature)
        arr.push(tmp)
        
    }
 //   console.log("arr: ", arr)
    return arr;
}

app.get("/create", async (req, res) => {
    try {
        await putCont(1);
    } catch (err) {
        console.log("/create error: ", err)
    }
    
})

app.get("/updateRows", async (req, res) => {
    try {
        let queryStr = "select *"
        var results = await queryCont(queryStr)
        res.json({
            results
        });
    } catch (error) {
        console.log("update rows error: ", error)
    }
});

app.get('/firstLoad', async (req, res) => {
    try {
        await putCont(10);
        let queryStr = "select *"
        var results = await queryCont(queryStr)
        res.json({
            results
        });
    } catch (error) {
        console.log("try error: ", error)
    }
});

app.post("/update", jsonParser, async (req, res) => {
    const newRowObj = req.body.row
    const newRowArr = []

    for (const [key, value] of Object.entries(newRowObj)) {
        newRowArr.push(value)
    }
    newRowArr.shift(); 
    newRowArr[2] = parseFloat(newRowArr[2])
    console.log("new row: from endpoint: ", newRowArr)

    try {
        let x = await updateRow(newRowArr)
        console.log("return of update row: ", x)
        res.status(200).json(true);
    } catch (err) {
        console.log("update endpoitn failure: ", err)
    }
});

app.post("/delete", jsonParser, async (req, res) => {
    const rows = req.body.rows

    try {
        let x = await deleteRow(rows)
        console.log("deleting rows")
        res.status(200).json(true)
    } catch (err) {
        console.log("delete row endpoint failure: ", err)
    }
})

// All other GET requests not handled before will return our React app
app.get('*', (req, res) => {
    res.sendFile(path.resolve(__dirname, 'frontend/build', 'index.html'));
  });

const PORT = process.env.PORT || 2828;

app.listen(PORT, () => {
    console.log(`Server started on port ${PORT}`);
});
