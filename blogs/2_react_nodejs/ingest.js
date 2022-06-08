const griddb = require('griddb_node');
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

var conInfo = new griddb.ContainerInfo({
          'name': 'Crereal',
          'columnInfoList': [
                        ["name", griddb.GS_TYPE_STRING],
                        ["mfr", griddb.GS_TYPE_STRING],
                        ["type", griddb.GS_TYPE_STRING],
                        ["calories", griddb.GS_TYPE_INTEGER],
                        ["protein", griddb.GS_TYPE_INTEGER],
                        ["fat", griddb.GS_TYPE_INTEGER],
                        ["sodium", griddb.GS_TYPE_INTEGER],
                        ["fiber", griddb.GS_TYPE_FLOAT],
                        ["carbo", griddb.GS_TYPE_FLOAT],
                        ["sugars", griddb.GS_TYPE_INTEGER],
                        ["potass", griddb.GS_TYPE_INTEGER],
                        ["vitamins", griddb.GS_TYPE_INTEGER],
                        ["shelf", griddb.GS_TYPE_INTEGER],
                        ["weight", griddb.GS_TYPE_FLOAT],
                        ["cups", griddb.GS_TYPE_FLOAT],
                        ["rating", griddb.GS_TYPE_FLOAT]
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
