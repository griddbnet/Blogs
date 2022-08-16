var griddb = require('griddb_node');

const createCsvWriter = require('csv-writer').createObjectCsvWriter;
const csvWriter = createCsvWriter({
  path: 'out.csv',
  header: [
    {id: "Country", title:"Country"}, 
    {id: "Sector", title:"Sector"}, 
    {id: "Gas", title:"Gas"}, 
    {id: "Unit", title:"Unit"}, 
    {id: "2018", title:"2018"}, 
    {id: "2017", title:"2017"}, 
    {id: "2016", title:"2016"},  
    {id: "2015", title:"2015"},  
    {id: "2014", title:"2014"}, 
    {id: "2013", title:"2013"},
    {id: "2012", title:"2012"},  
    {id: "2011", title:"2011"}, 
    {id: "2010", title:"2010"}, 
    {id: "2009", title:"2009"}, 
    {id: "2008", title:"2008"},
    {id: "2007", title:"2007"},
    {id: "2006", title:"2006"},
    {id: "2005", title:"2005"},
    {id: "2004", title:"2004"},
    {id: "2003", title:"2003"},
    {id: "2002", title:"2002"},
    {id: "2001", title:"2001"},
    {id: "2000", title:"2000"},
    {id: "1999", title:"1999"},
    {id: "1998", title:"1998"},
    {id: "1997", title:"1997"},
    {id: "1996", title:"1996"},
    {id: "1995", title:"1995"},
    {id: "1994", title:"1994"},
    {id: "1993", title:"1993"},
    {id: "1992", title:"1992"},
    {id: "1991", title:"1991"},
    {id: "1990", title:"1990"},
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
    'name': "methaneanalysis",
    'columnInfoList': [
      ["name", griddb.Type.STRING],
      ["Country", griddb.Type.STRING],
        ["Sector", griddb.Type.STRING],
        ["Gas", griddb.Type.STRING],
        ["Unit", griddb.Type.STRING],
        ["2018", griddb.Type.DOUBLE],
        ["2017", griddb.Type.DOUBLE],
        ["2016", griddb.Type.DOUBLE],
        ["2015", griddb.Type.DOUBLE],
        ["2014", griddb.Type.DOUBLE],
        ["2013", griddb.Type.DOUBLE],
        ["2012", griddb.Type.DOUBLE],
        ["2011", griddb.Type.DOUBLE],
        ["2010", griddb.Type.DOUBLE],
        ["2009", griddb.Type.DOUBLE],
        ["2008", griddb.Type.DOUBLE],
        ["2007", griddb.Type.DOUBLE],
        ["2006", griddb.Type.DOUBLE],
        ["2005", griddb.Type.DOUBLE],
        ["2004", griddb.Type.DOUBLE],
        ["2003", griddb.Type.DOUBLE],
        ["2002", griddb.Type.DOUBLE],
        ["2001", griddb.Type.DOUBLE],
        ["2000", griddb.Type.DOUBLE],
        ["1999", griddb.Type.DOUBLE],
        ["1998", griddb.Type.DOUBLE],
        ["1997", griddb.Type.DOUBLE],
        ["1996", griddb.Type.DOUBLE],
        ["1995", griddb.Type.DOUBLE],
        ["1994", griddb.Type.DOUBLE],
        ["1993", griddb.Type.DOUBLE],
        ["1992", griddb.Type.DOUBLE],
        ["1991", griddb.Type.DOUBLE],
        ["1990", griddb.Type.DOUBLE]
    ],
    'type': griddb.ContainerType.COLLECTION, 'rowKey': true
});


// ////////////////////////////////////////////


const csv = require('csv-parser');

const fs = require('fs');
var lst = []
var lst2 = []
var i =0;
fs.createReadStream('./Dataset/methane_hist_emissions.csv')
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
            return container.put([String(idx), lst[i]['Country'],lst[i]["Sector"],lst[i]["Gas"],lst[i]["Unit"],lst[i]["2018"],lst[i]["2017"],lst[i]["2016"],lst[i]["2015"],lst[i]["2014"],lst[i]["2013"],lst[i]["2012"],lst[i]["2011"],lst[i]["2010"],lst[i]["2009"],lst[i]["2008"],lst[i]["2007"],lst[i]["2006"],lst[i]["2005"],lst[i]["2004"],lst[i]["2003"],lst[i]["2002"],lst[i]["2001"],lst[i]["2000"],lst[i]["1999"],lst[i]["1998"],lst[i]["1997"],lst[i]["1996"],lst[i]["1995"],lst[i]["1994"],lst[i]["1993"],lst[i]["1992"],lst[i]["1991"],lst[i]["1990"]]);
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

    store.getContainer("methaneanalysis")
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
                'Country': rsNext[1],
                "Sector": rsNext[2],
                "Gas": rsNext[3],
                "Unit": rsNext[4],
                "2018": rsNext[5],
                "2017": rsNext[6],
                "2016": rsNext[7],
                "2015": rsNext[8],
                "2014": rsNext[9],
                "2013": rsNext[10],
                "2012": rsNext[11],
                "2011": rsNext[12],
                "2010": rsNext[13],
                "2009": rsNext[14],
                "2008": rsNext[15],
                "2007": rsNext[16],
                "2006": rsNext[17],
                "2005": rsNext[18],
                "2004": rsNext[19],
                "2003": rsNext[20],
                "2002": rsNext[21],
                "2001": rsNext[22],
                "2000": rsNext[23],
                "1999": rsNext[24],
                "1998": rsNext[25],
                "1997": rsNext[26],
                "1996": rsNext[27],
                "1995": rsNext[28],
                "1994": rsNext[29],
                "1993": rsNext[30],
                "1992": rsNext[31],
                "1991": rsNext[32],
                "1990": rsNext[33],
                
            
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
  
  
 


 
