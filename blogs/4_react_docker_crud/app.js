const { config } = require('dotenv');
const express = require('express');
const path = require('path');
const griddb = require('griddb-node-api');
var bodyParser = require('body-parser')

const app = express();
var jsonParser = bodyParser.json()

app.use(bodyParser.json({ type: 'application/*+json' }))
app.use(express.static(path.resolve(__dirname, 'frontend/build')));

var fs = require('fs');
var factory = griddb.StoreFactory.getInstance();
store = factory.getStore({
    "host": process.argv[2],
    "port": parseInt(process.argv[3]),
    "clusterName": process.argv[4],
    "username": process.argv[5],
    "password": process.argv[6]
});

 const queryCont = async (queryStr) => {

     var data = []
     try {
         const col = await store.getContainer(containerName)
         const query = await col.query(queryStr)
         const rs = await query.fetch(query)
         while(rs.hasNext()) {
             data.push(rs.next())
         }
         console.log("Data: " , data)
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
        ["data", griddb.Type.FLOAT],
        ["temperature", griddb.Type.FLOAT],
    ],
    'type': griddb.ContainerType.TIME_SERIES, 'rowKey': true
});


config();

function getRandomFloat(min, max) {
    return Math.random() * (max - min) + min;
  }




const putCont = async () => {
    console.log("Putting COntainer")
    const rows = generateSensors();
    try {
        await store.dropContainer(containerName);
        const cont = await store.putContainer(conInfo)
        await cont.multiPut(rows);
    } catch (error) {
        console.log("error: ", error)
    }
}

const generateSensors = () => {

    let numSensors = 10
    let arr = []
    console.log("Generating sensors")

    for (let i = 1; i <= numSensors; i++) {
        let tmp = [];
        let now = new Date();
        let newTime = now.setMilliseconds(now.getMinutes() + i)
        let data = parseFloat(getRandomFloat(1, 10).toFixed(2))
        let temperature = parseFloat(getRandomFloat(60, 130).toFixed(2))
        tmp.push(newTime)
        tmp.push(data)
        tmp.push(temperature)
        arr.push(tmp)
        
    }
 //   console.log("arr: ", arr)
    return arr;

}


app.get('/firstLoad', async (req, res) => {
    try {
        await putCont();
        let queryStr = "select *"
        var results = await queryCont(queryStr)
        res.json({
            results
        });
    } catch (error) {
        console.log("try error: ", error)
    }
});

// All other GET requests not handled before will return our React app
app.get('*', (req, res) => {
    res.sendFile(path.resolve(__dirname, 'frontend/build', 'index.html'));
  });

const PORT = process.env.PORT || 5000;

app.listen(PORT, () => {
    console.log(`Server started on port ${PORT}`);
});
