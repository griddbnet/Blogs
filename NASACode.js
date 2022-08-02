var griddb = require('griddb_node');

const createCsvWriter = require('csv-writer').createObjectCsvWriter;
const csvWriter = createCsvWriter({
  path: 'out.csv',
  header: [
    {id: "id", title:"id"}, 
    {id: "new_name", title:"new_name"}, 
    {id: "est_diameter_min", title:"est_diameter_min"}, 
    {id: "est_diameter_max", title:"est_diameter_max"}, 
    {id: "relative_velocity", title:"relative_velocity"}, 
    {id: "miss_distance", title:"miss_distance"}, 
    {id: "orbiting_body" , title:"orbiting_body"}, 
    {id: "sentry_object", title:"sentry_object"}, 
    {id: "absolute_magnitude", title:"absolute_magnitude"}
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
    'name': "neoanalysis",
    'columnInfoList': [
      ["name", griddb.Type.STRING],
      ["id", griddb.Type.INTEGER],
        ["new_name", griddb.Type.STRING],
        ["est_diameter_min", griddb.Type.DOUBLE],
        ["est_diameter_max", griddb.Type.DOUBLE],
        ["relative_velocity", griddb.Type.DOUBLE],
        ["miss_distance", griddb.Type.DOUBLE],
        ["absolute_magnitude", griddb.Type.DOUBLE]
    ],
    'type': griddb.ContainerType.COLLECTION, 'rowKey': true
});


// ////////////////////////////////////////////


const csv = require('csv-parser');

const fs = require('fs');
var lst = []
var lst2 = []
var i =0;
fs.createReadStream('./Dataset/neo.csv')
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

        //console.log("====?list1",lst)



    store.putContainer(conInfo, false)
        .then(cont => {
            container = cont;
            return container.createIndex({ 'columnName': 'name', 'indexType': griddb.IndexType.DEFAULT });
        })
        .then(() => {
            idx++;
            container.setAutoCommit(false);
            return container.put([String(idx), lst[i]['id'],lst[i]["new_name"],lst[i]["est_diameter_min"],lst[i]["est_diameter_max"],lst[i]["relative_velocity"],lst[i]["miss_distance"],lst[i]["absolute_magnitude"]]);
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

    
    store.getContainer("neoanalysis")
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
                'id': rsNext[1],
                "new_name": rsNext[2],
                "est_diameter_min": rsNext[3],
                "est_diameter_max": rsNext[4],
                "relative_velocity": rsNext[5],
                "miss_distance": rsNext[6],
                "absolute_magnitude": rsNext[7],
               
            
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
  
  
  
  
  
  
  
  /*
  let df = await dfd.readCSV("./out.csv")
  
  
  console.log(df.shape)
  
  
  
  console.log(df.columns)
  
  
  df.loc({columns:['new_name',
'est_diameter_min',
'est_diameter_max',
'relative_velocity']}).ctypes.print()




    # Get the containers
    obtained_data = gridstore.get_container("redwinequality")
    
    # Fetch all rows - language_tag_container
    query = obtained_data.query("select *")
    
    
    
    
    
    
    df.loc({columns:['new_name',
'est_diameter_min',
'est_diameter_max',
'relative_velocity']}).describe().round(2).print()

*/


 
