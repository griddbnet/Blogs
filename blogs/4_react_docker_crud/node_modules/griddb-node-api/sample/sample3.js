var griddb = require('griddb-node-api');
var fs = require('fs');

var factory = griddb.StoreFactory.getInstance();
var store = factory.getStore({
    "host": process.argv[2],
    "port": parseInt(process.argv[3]),
    "clusterName": process.argv[4],
    "username": process.argv[5],
    "password": process.argv[6]
});

var timeseries;
store.getContainer("point01")
    .then(ts => {
        timeseries = ts;
        query = ts.query("select * from point01 where not active and voltage > 50");
        return query.fetch();
    })
    .then(rowset => {
        var row;
        while (rowset.hasNext()) {
            row = rowset.next();
            var timestamp = Date.parse(row[0]);
            aggCommand = "select AVG(voltage) from point01 where timestamp > TIMESTAMPADD(MINUTE, TO_TIMESTAMP_MS(" + timestamp + "), -10) AND timestamp < TIMESTAMPADD(MINUTE, TO_TIMESTAMP_MS(" + timestamp + "), 10)";
            aggQuery = timeseries.query(aggCommand);

            aggQuery.fetch()
                .then(aggRs => {
                    while (aggRs.hasNext()) {
                        aggResult = aggRs.next();
                        console.log("[Timestamp = " + timestamp + "] Average voltage = " + aggResult.get(griddb.Type.DOUBLE));
                    }
                });
        }
    })
    .catch(err => {
        console.log(err.message);
    });
