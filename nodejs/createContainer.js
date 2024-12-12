require('dotenv').config()
var request = require('request');
const base = process.env.GRIDDB_WEBAPI_URL
const creds = process.env.USER_PASS
var options = {
  'method': 'POST',
  'url': base+'/containers',
  'headers': {
    'Content-Type': 'application/json',
    'Authorization': 'Basic '+creds
  },
  body: JSON.stringify({
    "container_name": "device1",
    "container_type": "TIME_SERIES",
    "rowkey": true,
    "columns": [
      {
        "name": "ts",
        "type": "TIMESTAMP"
      },
      {
        "name": "co",
        "type": "DOUBLE"
      },
      {
        "name": "humidity",
        "type": "DOUBLE"
      },
      {
        "name": "light",
        "type": "BOOL"
      },
      {
        "name": "lpg",
        "type": "DOUBLE"
      },
      {
        "name": "motion",
        "type": "BOOL"
      },
      {
        "name": "smoke",
        "type": "DOUBLE"
      },
      {
        "name": "temp",
        "type": "DOUBLE"
      }
    ]
  })

};
request(options, function (error, response) {
  if (error) throw new Error(error);
  console.log("Response Status Code: ", response.statusCode);
});

