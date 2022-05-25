var griddb = require('griddb_node');

const createCsvWriter = require('csv-writer').createObjectCsvWriter;
const csvWriter = createCsvWriter({
  path: 'out.csv',
  header: [
    {id: "fixed acidity", title:"fixed acidity"}, 
    {id: "volatile acidity", title:"volatile acidity"}, 
    {id: "citric acid", title:"citric acid"}, 
    {id: "residual sugar", title:"residual sugar"}, 
    {id: "chlorides", title:"chlorides"}, 
    {id: "free sulfur dioxide", title:"free sulfur dioxide"}, 
    {id: "total sulfur dioxide" , title:"total sulfur dioxide"}, 
    {id: "density", title:"density"}, 
    {id: "pH", title:"pH"}, 
    {id: "sulphates", title:"sulphates"}, 
    {id: "alcohol", title:"alcohol"}, 
    {id: "quality", title:"quality"} 
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
    'name': "redwinequality",
    'columnInfoList': [
      ["name", griddb.Type.STRING],
      ["fixedacidity", griddb.Type.DOUBLE],
      ["volatileacidity", griddb.Type.DOUBLE],
      ["citricacid", griddb.Type.DOUBLE],
      ["residualsugar", griddb.Type.DOUBLE],
      ["chlorides", griddb.Type.DOUBLE],
      ["freesulfurdioxide", griddb.Type.INTEGER],
      ["totalsulfurdioxide", griddb.Type.INTEGER],
      ["density", griddb.Type.DOUBLE],
      ["pH", griddb.Type.DOUBLE],
      ["sulphates", griddb.Type.DOUBLE],
      ["alcohol", griddb.Type.DOUBLE],
      ["quality", griddb.Type.INTEGER],
    ],
    'type': griddb.ContainerType.COLLECTION, 'rowKey': true
});


// ////////////////////////////////////////////


const csv = require('csv-parser');

const fs = require('fs');
var lst = []
var lst2 = []
var i =0;
fs.createReadStream('./dataset/winequality-red.csv')
  .pipe(csv())
  .on('data', (row) => {
    lst.push(row);
  })
  .on('end', () => {
    // console.log(lst);
    // console.log("workig=================>",lst)




    // console.log('output');
    var container;
    var idx = 0;
    
    for(let i=0;i<lst.length;i++){
        lst[i]["fixed acidity"] = parseFloat(lst[i]["fixed acidity"])

        lst[i]['volatile acidity'] = parseFloat(lst[i]["volatile acidity"])
        lst[i]['citric acid'] = parseFloat(lst[i]["citric acid"])
        lst[i]['residual sugar'] = parseFloat(lst[i]["residual sugar"])
        lst[i]['chlorides'] = parseFloat(lst[i]["chlorides"])
        lst[i]['free sulfur dioxide'] = parseInt(lst[i]["free sulfur dioxide"])
        lst[i]['total sulfur dioxide'] = parseInt(lst[i]["total sulfur dioxide"])
        lst[i]['density'] = parseFloat(lst[i]["density"])
        lst[i]['pH'] = parseFloat(lst[i]["pH"])
        lst[i]['sulphates'] = parseFloat(lst[i]["sulphates"])
        lst[i]['alcohol'] = parseFloat(lst[i]["alcohol"])
        lst[i]['quality'] = parseFloat(lst[i]["quality"])





        console.log(parseFloat(lst[i]["fixed acidity"]))
    store.putContainer(conInfo, false)
        .then(cont => {
            container = cont;
            return container.createIndex({ 'columnName': 'name', 'indexType': griddb.IndexType.DEFAULT });
        })
        .then(() => {
            idx++;
            container.setAutoCommit(false);
            return container.put([String(idx), lst[i]['fixed acidity'],lst[i]["volatile acidity"],lst[i]["citric acid"],lst[i]["residual sugar"],lst[i]["chlorides"],lst[i]["free sulfur dioxide"],lst[i]["total sulfur dioxide"],lst[i]["density"],lst[i]["pH"],lst[i]["sulphates"],lst[i]["alcohol"],lst[i]["quality"]]);
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

    
    store.getContainer("redwinequality")
    .then(ts => {
        container = ts;
      query = container.query("select *")
      return query.fetch();
  })
  .then(rs => {
      while (rs.hasNext()) {
          // console.log('heellsldldllsdf')

          console.log(rs.next())
          
          lst2.push(
            
            
                {
                    'fixed acidity': rs.next()[1],
                    "volatile acidity": rs.next()[2],
                    "citric acid": rs.next()[3],
                    "residual sugar": rs.next()[4],
                    "chlorides": rs.next()[5],
                    "free sulfur dioxide": rs.next()[6],
                    "total sulfur dioxide": rs.next()[7],
                    "density": rs.next()[8],
                    "pH": rs.next()[9],
                    "sulphates": rs.next()[10],
                    "alcohol": rs.next()[11],
                    "quality": rs.next()[12]
                
                }

              
            
            
          );
          
      }

      console.log(lst2)

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
    
  console.log(lst2);



  });
 

// console.log(lst2)
