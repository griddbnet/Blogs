var request = require('request');
var options = {
  'method': 'POST',
  'url': 'https://cloud5197.griddb.com/griddb/v2/gs_clustermfcloud5197/dbs/B2xcGQJy/containers',
  'headers': {
    'Content-Type': 'application/json',
    'Authorization': 'Basic TTAxMU1sd0MxYS1pc3JhZWw6aXNyYWVs'
  },
  body: JSON.stringify({
    "container_name": "deviceMaster",
    "container_type": "COLLECTION",
    "rowkey": true,
    "columns": [
      {
        "name": "equipment",
        "type": "STRING"
      },
      {
        "name": "equipmentID",
        "type": "STRING"
      },
      {
        "name": "location",
        "type": "STRING"
      },
      {
        "name": "serialNumber",
        "type": "STRING"
      },
      {
        "name": "lastInspection",
        "type": "TIMESTAMP"
      },
      {
        "name": "information",
        "type": "STRING"
      }
    ]
  })

};
request(options, function (error, response) {
  if (error) throw new Error(error);
  console.log(response.statusCode);
});
