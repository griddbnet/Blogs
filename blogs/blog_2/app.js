const { config } = require('dotenv');
const express = require('express');
const { join } = require('path');
//const griddb = require('griddb_node');

const app = express();

//var fs = require('fs');
//var factory = griddb.StoreFactory.getInstance();
//var store = factory.getStore({
//    "host": process.argv[2],
//    "port": parseInt(process.argv[3]),
//    "clusterName": process.argv[4],
//    "username": process.argv[5],
//    "password": process.argv[6]
//});

config();


app.use(express.static(join(__dirname, 'public')));

app.get('/api', (req, res) => {
    res.json({
        success: true,
        message: 'Welcome to the API',
    });
});

app.get('/', (req, res) => {
    res.send('Hello World');
});

const PORT = process.env.PORT || 5000;

app.listen(PORT, () => {
    console.log(`Server started on port ${PORT}`);
});

