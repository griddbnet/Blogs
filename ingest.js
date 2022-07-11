const griddb = require('griddb-node-api');
var { parse } = require('csv-parse');

var fs = require('fs');
var factory = griddb.StoreFactory.getInstance();
var store = factory.getStore({
    "host": process.argv[2],
    "port": parseInt(process.argv[3]),
    "clusterName": process.argv[4],
    "username": process.argv[5],
    "password": process.argv[6]
});

var containerName = "Cereal"
const conInfo = new griddb.ContainerInfo({
    'name': containerName,
    'columnInfoList': [
        ["name", griddb.Type.STRING],
        ["mfr", griddb.Type.STRING],
        ["type", griddb.Type.STRING],
        ["calories", griddb.Type.INTEGER],
        ["protein", griddb.Type.INTEGER],
        ["fat", griddb.Type.INTEGER],
        ["sodium", griddb.Type.INTEGER],
        ["fiber", griddb.Type.FLOAT],
        ["carbo", griddb.Type.FLOAT],
        ["sugars", griddb.Type.INTEGER],
        ["potass", griddb.Type.INTEGER],
        ["vitamins", griddb.Type.INTEGER],
        ["shelf", griddb.Type.INTEGER],
        ["weight", griddb.Type.FLOAT],
        ["cups", griddb.Type.FLOAT],
        ["rating", griddb.Type.FLOAT]
    ],
    'type': griddb.ContainerType.COLLECTION,
    'rowKey': false
});

var arr = []
fs.createReadStream(__dirname + '/cereal.csv')
    .pipe(parse({ columns: true }))
    .on('data', (row) => {
        arr.push(row)
    })
    .on('end', () => {
        store.putContainer(conInfo)
            .then(col => {
                arr.forEach(row => {
                    return col.put([
                        row['name'],
                        row['mfr'],
                        row['type'],
                        parseInt(row['calories']),
                        parseInt(row['protein']),
                        parseInt(row['fat']),
                        parseInt(row['sodium']),
                        parseFloat(row['fiber']),
                        parseFloat(row['carbo']),
                        parseInt(row['sugars']),
                        parseInt(row["potass"]),
                        parseInt(row["vitamins"]),
                        parseInt(row["shelf"]),
                        parseFloat(row["weight"]),
                        parseFloat(row["cups"]),
                        parseFloat(row["rating"])
                    ]);
                })
            })
            .then(() => {
                console.log("Success!");
                return true;
            })
            .catch(err => {
                console.log(err);
            });
    })