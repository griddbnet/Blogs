var griddb =  require('griddb_node');

const createCsvWriter = require('csv-writer').createObjectCsvWriter;
const csvWriter = createCsvWriter({
  path: 'out.csv',
  header: [
	  {id: "country", title: "country"},
	  {id: "daily oil consumption (barrels)", title: "daily oil consumption (barrels)"},
	  {id: "world share", title: "world share"},
	  {id: "yearly gallons per capita", title: "yearly gallons per capita"},
	  {id: "price per gallon (USD)", title: "price per gallon(USD)"},
	  {id: "price per liter (USD)", title: "price per liter (USD)"},
	  {id: "price per liter (PKR)", title: "price per liter (PKR)"}
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

const conInfo = new griddb.ContainerInfo({
    'name': "globalfluctuationsfuelprices",
    'columnInfoList': [
      ["country", griddb.Type.STRING],
      ["daily oil consumption (barrels)", griddb.Type.INTEGER],
      ["world share", griddb.Type.STRING],
      ["yearly gallons per capita", griddb.Type.DOUBLE],
      ["price per gallon (USD)", griddb.Type.DOUBLE],
      ["price per liter (USD)", griddb.Type.DOUBLE],
      ["price per liter (PKR)", griddb.Type.DOUBLE]
    ],
    'type': griddb.ContainerType.COLLECTION, 'rowKey': true
});





const csv = require('csv-parser');

const fs = require('fs');
var lst = []
var lst2 = []
var i =0;
fs.createReadStream('./dataset/Petrol Dataset June 20 2022.csv')
  .pipe(csv())
  .on('data', (row) => {
    lst.push(row);
  })
  .on('end', () => {

    var container;
    var idx = 0;

    for(let i=0;i<lst.length;i++){

        lst[i]['daily oil consumption (barrels)'] = parseInt(lst[i]["daily oil consumption (barrels)"])
        lst[i]['yearly gallons per capita'] = parseFloat(lst[i]["yearly gallons per capita"])
        lst[i]['price per gallon(USD)'] = parseFloat(lst[i]["price per gallon (USD)"])
        lst[i]['price per liter (USD)'] = parseFloat(lst[i]["price per liter (USD)"])
        lst[i]['price per liter (PKR)'] = parseFloat(lst[i]["price per liter (PKR)"])


        console.log(parseFloat(lst[i]["country"]))
    store.putContainer(conInfo, false)
        .then(cont => {
            container = cont;
            return container.createIndex({ 'columnName': 'name', 'indexType': griddb.IndexType.DEFAULT });
        })
        .then(() => {
            idx++;
            container.setAutoCommit(false);
            return container.put([String(idx), lst[i]['country'],lst[i]["daily oil consumption (barrels)"],lst[i]["world share"],lst[i]["yearly gallons per capita"],lst[i]["price per gallon (USD)"],lst[i]["price per liter(USD)"],lst[i]["price per liter (PKR)"]]);
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


    store.getContainer("globalfluctuationsfuelprices")
    .then(ts => {
        container = ts;
      query = container.query("select *")
      return query.fetch();
  })
  .then(rs => {
      while (rs.hasNext()) {

          console.log(rs.next())
          lst2.push(

                {
                    'country': rs.next()[1],
                    "daily oil consumption (barrels)": rs.next()[2],
                    "world share": rs.next()[3],
                    "yearly gallons per capita": rs.next()[4],
                    "price per gallon (USD)": rs.next()[5],
                    "price per liter (USD)": rs.next()[6],
                    "price per liter (PKR)": rs.next()[7]
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
