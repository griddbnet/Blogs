const { config } = require('dotenv');
const express = require('express');
const { join } = require('path');
const griddb = require('griddb_node');
var bodyParser = require('body-parser')

const app = express();
var jsonParser = bodyParser.json()
var urlencodedParser = bodyParser.urlencoded({ extended: false })

app.use(bodyParser.json({ type: 'application/*+json' }))

var fs = require('fs');
var factory = griddb.StoreFactory.getInstance();
store = factory.getStore({
    "host": process.argv[2],
    "port": parseInt(process.argv[3]),
    "clusterName": process.argv[4],
    "username": process.argv[5],
    "password": process.argv[6]
});


config();

var promise = new Promise(function(resolve, reject) {
    resolve(true);
});

containerName = 'Crereal';

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

const querySpecific = async (cereal, comp, list) => {

    var data = []
    let q = `SELECT * WHERE name='${cereal}'`
    try {
        const col = await store.getContainer(containerName)
        const query = await col.query(q)
        const rs = await query.fetch(query)
        while(rs.hasNext()) {
            data.push(rs.next())
        }
        return data
    } catch (error) {
        console.log("error: ", error)
    }
}


const queryVal = async (list, comp, val) => {

    var data = []
    let q = `SELECT * WHERE ${list} ${comp} ${val} `
    console.log("query VaL: ", q)
    try {
        const col = await store.getContainer(containerName)
        const query = await col.query(q)
        const rs = await query.fetch(query)
        while(rs.hasNext()) {
            data.push(rs.next())
        }
        return data
    } catch (error) {
        console.log("error: ", error)
    }
}


const checkType = type => {
    console.log("check type: ", type)
    var par;
    switch(type) {
        case "calories":
            par = 3
            break
        case "protein":
            par = 4
             break
        case "fat":
            par = 5
             break
        case "sodium":
            par = 6
             break
        case "fiber":
            par = 7
             break
        case "carbo":
            par = 8
             break
        case "sugars":
            par = 9
             break
        case "vitamins":
            par = 11
             break
    }   

    console.log("check type val: ", par)
    return par
}

const checkComp = comp => {
    console.log("Check comp: ", comp)
    var c;
    switch(String(comp)) {
        case "Greater Than":
            c = ">"
             break
        case "Less Than":
            c = "<"
             break
        case "Equal":
            c = "="
             break
    }
    console.log("Check comp val: ", c)
    return c
}

app.use(express.static(join(__dirname, 'public')));


app.get('/all', async (req, res) => {
    try {
        let queryStr = "select *"
        var results = await queryCont(queryStr)
        res.json({
            results
        });
    } catch (error) {
        console.log("try error: ", error)
    }
});

var userResult = {"ok": "ok"}
var userVal;
app.post('/query', jsonParser, async (req, res) => {
    const {list, comp, name} = req.body 
    console.log("req body: ", req.body)
    try {
        var results = await querySpecific(name, comp, list)
        let type = checkType(list) //grabs array position of proper value
        let compVal = checkComp(comp)
        let val = results[0][type]
        console.log("results, type, compval, val", type, compVal, val)
        userVal = val
        let specificRes = await queryVal(list, compVal, val)
        userResult = specificRes
        //console.log("specific results: ", specificRes)
        res.status(200).json(userResult);
    } catch (error) {
        console.log("try error: ", error)
    }
});

app.get("/test", (req, res) => {
    res.json({
        userVal,
        userResult
    })
});

app.get('/', (req, res) => {
    res.send('Hello World');
});

const PORT = process.env.PORT || 5000;

app.listen(PORT, () => {
    console.log(`Server started on port ${PORT}`);
});

