var griddb = require('griddb_node');

const createCsvWriter = require('csv-writer').createObjectCsvWriter;
const csvWriter = createCsvWriter({
  path: 'out.csv',
  header: [
    {id: "Sales_ID", title:"Sales_ID"}, 
    {id: "name", title:"name"},
    {id: "year", title:"year"},
    {id: "selling_price", title:"selling_price"},
    {id: "km_driven", title:"km_driven"},
    {id: "Region", title:"Region"},
    {id: "State or Province", title:"State or Province"},
    {id: "City", title:"City"},
    {id: "fuel", title:"fuel"},
    {id: "seller_type", title:"seller_type"},
    {id: "transmission", title:"transmission"},
    {id: "owner", title:"owner"},
    {id: "mileage", title:"mileage"},
    {id: "engine", title:"engine"},
    {id: "max_power", title:"max_power"},
    {id: "torque", title:"torque"},
    {id: "seats", title:"seats"},
    {id: "sold", title:"sold"}, 
    
  ]
});

const factory = griddb.StoreFactory.getInstance();
const store = factory.getStore({
    "host": '239.0.0.1',
    "port": 31999,
    "clusterName": "defaultCluster",
    "username": "admin",
    "password": "admin"
});

// For connecting to the GridDB Server we have to make containers and specify the schema.
const conInfo = new griddb.ContainerInfo({
    'name': "usedcaranalysis",
    'columnInfoList': [
      ["name", griddb.Type.STRING],
      ["Sales_ID", griddb.Type.INTEGER],
        ["name", griddb.Type.STRING],
        ["year", griddb.Type.INTEGER],
        ["selling_price", griddb.Type.INTEGER],
        ["km_driven", griddb.Type.INTEGER],
        ["Region", griddb.Type.STRING],
        ["State or Province", griddb.Type.STRING],
        ["City", griddb.Type.STRING],
        ["fuel", griddb.Type.STRING],
        ["seller_type", griddb.Type.STRING],
        ["transmission", griddb.Type.STRING],
        ["owner", griddb.Type.STRING],
        ["mileage", griddb.Type.DOUBLE],
        ["engine", griddb.Type.INTEGER],
        ["max_power", griddb.Type.DOUBLE],
        ["torque", griddb.Type.STRING],
        ["seats", griddb.Type.INTEGER],
        ["sold", griddb.Type.STRING]
    ],
    'type': griddb.ContainerType.COLLECTION, 'rowKey': true
});


// ////////////////////////////////////////////


const csv = require('csv-parser');

const fs = require('fs');
var lst = []
var lst2 = []
var i =0;
fs.createReadStream('./Dataset/UserCarData.csv')
  .pipe(csv())
  .on('data', (row) => {
    lst.push(row);
    console.log(lst);

  })
  .on('end', () => {
    var container;
    var idx = 0;
    
    for(let i=0;i<lst.length;i++){

    store.putContainer(conInfo, false)
        .then(cont => {
            container = cont;
            return container.createIndex({ 'columnName': 'name', 'indexType': griddb.IndexType.DEFAULT });
        })
        .then(() => {
            idx++;
            container.setAutoCommit(false);
            return container.put([String(idx), lst[i]['Sales_ID'],lst[i]["name"],lst[i]["year"],lst[i]["selling_price"],lst[i]["km_driven"],lst[i]["Region"],lst[i]["State or Province"],lst[i]["City"],lst[i]["fuel"],lst[i]["seller_type"],lst[i]["transmission"],lst[i]["owner"],lst[i]["mileage"],lst[i]["engine"],lst[i]["max_power"],lst[i]["torque"],lst[i]["seats"],lst[i]["sold"]]);
        })
        .then(() => {
            return container.commit();
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
    
    }
    store.getContainer("usedcaranalysis")
    .then(ts => {
        container = ts;
      query = container.query("select *")
      return query.fetch();
  })
  .then(rs => {
      while (rs.hasNext()) {

          let rsNext = rs.next()

          lst2.push(
                
            {
                'Sales_ID': rsNext[1],
                "name": rsNext[2],
                "year": rsNext[3],
                "selling_price": rsNext[4],
                "km_driven": rsNext[5],
                "Region": rsNext[6],
                "State or Province": rsNext[7],
                "City": rsNext[8],
                "fuel": rsNext[9],
                "seller_type": rsNext[10],
                "transmission": rsNext[11],
                "owner": rsNext[12],
                "mileage": rsNext[13],
                "engine": rsNext[14],
                "max_power": rsNext[15],
                "torque": rsNext[16],
                "seats": rsNext[17],
                "sold": rsNext[18]
                
            }      
          );   
      }
        csvWriter
        .writeRecords(lst2)
        .then(()=> console.log('The CSV file was written successfully'));

      return 
  }).catch(err => {
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
  
  });
  
