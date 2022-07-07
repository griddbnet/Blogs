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
                        ["name", griddb.Type.INTEGER],
                        ["mfr", griddb.Type.INTEGER],
                        ["type", griddb.Type.INTEGER],
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

const results = [];


fs.createReadStream(__dirname+'/cereal.csv')
  .pipe(parse({columns: true}))
  .on('data', (row) => {
    var col2;
    store.putContainer(conInfo, false)
       .then(col => {
           col2 = col;
           return col;
        })
       .then(col => {
            setTimeout(() => {  console.log("Row Parsed and Put!"); }, 1000);

           col.put([
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
           return col;
       })
            .catch(err => {
                if (err.constructor.name == "GSException") {
                    for (var i = 0; i < err.getErrorStackSize(); i++) {
                        console.log("[", i, "]");
                        console.log(err.getErrorCode(i));
                        console.log(err.getMessage(i));
                    }
                } else {
                    console.log(err);
                }
            });
  })
  .on('end', () => {
    console.log('CSV file successfully processed');
  });
