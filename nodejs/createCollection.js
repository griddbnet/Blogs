require('dotenv').config()
var request = require('request');
const base = process.env.GRIDDB_CLOUD_URL
const creds = process.env.USER_PASS
var options = {
  'method': 'POST',
  'url': base+'/containers',
  'headers': {
    'Content-Type': 'application/json',
    'Authorization': 'Basic '+creds
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
