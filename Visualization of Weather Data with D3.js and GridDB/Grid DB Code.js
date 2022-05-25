var griddb = require('griddb_node');

const createCsvWriter = require('csv-writer').createObjectCsvWriter;
const csvWriter = createCsvWriter({
  path: 'out.csv',
  header: [
    {id: "DATE", title:"DATE"}, 
    {id: "WIND", title:"WIND"}, 
    {id: "IND", title:"IND"}, 
    {id: "RAIN", title:"RAIN"}, 
    {id: "IND.1", title:"IND.1"}, 
    {id: "T.MAX", title:"T.MAX"}, 
    {id: "IND.2" , title:"IND.2"}, 
    {id: "T.MIN", title:"T.MIN"}, 
    {id: "T.MIN.G", title:"T.MIN.G"}, 
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
    'name': "windspeedanalysis",
    'columnInfoList': [
      ["name", griddb.Type.STRING],
      ["DATE", griddb.Type.STRING],
        ["WIND", griddb.Type.STRING],
        ["IND", griddb.Type.STRING],
        ["RAIN", griddb.Type.STRING],
        ["IND.1", griddb.Type.STRING],
        ["T.MAX", griddb.Type.STRING],
        ["IND.2", griddb.Type.STRING],
        ["T.MIN", griddb.Type.STRING],
        ["T.MIN.G", griddb.Type.STRING]
    ],
    'type': griddb.ContainerType.COLLECTION, 'rowKey': true
});


// ////////////////////////////////////////////


const csv = require('csv-parser');

const fs = require('fs');
var lst = []
var lst2 = []
var i =0;
fs.createReadStream('./dataset/windspeed.csv')
  .pipe(csv())
  .on('data', (row) => {
    lst.push(row);
    console.log(lst);

  })
  .on('end', () => {
    console.log(lst);
    // console.log("workig=================>",lst)




    // console.log('output');
    var container;
    var idx = 0;
    
    for(let i=0;i<lst.length;i++){

        // lst[i]['DATE'] = String(lst[i]["DATE"])
        // lst[i]['WIND'] = parseFloat(lst[i]["WIND"])
        // lst[i]['IND'] = parseInt(lst[i]["IND"])
        // lst[i]['RAIN'] = parseFloat(lst[i]["RAIN"])
        // lst[i]['IND.1'] = parseInt(lst[i]["IND.1"])
        // lst[i]['T.MAX'] = parseFloat(lst[i]["T.MAX"])
        // lst[i]['IND.2'] = parseInt(lst[i]["IND.2"])
        // lst[i]['T.MIN'] = parseFloat(lst[i]["T.MIN"])
        // lst[i]['T.MIN.G'] = parseFloat(lst[i]["T.MIN.G"])

        console.log("====?list1",lst)



    store.putContainer(conInfo, false)
        .then(cont => {
            container = cont;
            return container.createIndex({ 'columnName': 'name', 'indexType': griddb.IndexType.DEFAULT });
        })
        .then(() => {
            idx++;
            container.setAutoCommit(false);
            return container.put([String(idx), lst[i]['DATE'],lst[i]["WIND"],lst[i]["IND"],lst[i]["RAIN"],lst[i]["IND.1"],lst[i]["T.MAX"],lst[i]["IND.2"],lst[i]["T.MIN"],lst[i]["T.MIN.G"]]);
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

    
    store.getContainer("windspeedanalysis")
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
                'DATE': rsNext[1],
                "WIND": rsNext[2],
                "IND": rsNext[3],
                "RAIN": rsNext[4],
                "IND.1": rsNext[5],
                "T.MAX": rsNext[6],
                "IND.2": rsNext[7],
                "T.MIN": rsNext[8],
                "T.MIN.G": rsNext[9],
               
            
            }

              
            
            
          );
          
      }

      console.log("===========>",lst2)

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
 
