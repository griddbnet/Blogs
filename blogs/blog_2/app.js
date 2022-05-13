const { config } = require('dotenv');
const express = require('express');
const { join } = require('path');
const griddb = require('griddb_node');
var bodyParser = require('body-parser')

const app = express();
var jsonParser = bodyParser.json()
var urlencodedParser = bodyParser.urlencoded({ extended: false })

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

containerName = 'Cereal';

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

app.post('/query', urlencodedParser, async (req, res) => {
   const {list, comp, cereal} = req.body 
    try {
        var results = await querySpecific(cereal, comp, list)
        console.log("results: ", results)
    } catch (error) {
        console.log("try error: ", error)
    }
});

app.get('/', (req, res) => {
    res.send('Hello World');
});

const PORT = process.env.PORT || 5000;

app.listen(PORT, () => {
    console.log(`Server started on port ${PORT}`);
});

