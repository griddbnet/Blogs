require('dotenv').config()
var request = require('request');
const base = process.env.GRIDDB_WEBAPI_URL
const creds = process.env.USER_PASS
var options = {
  'method': 'POST',
  'url': base+'/tql',
  'headers': {
    'Content-Type': 'application/json',
    'Authorization': 'Basic '+creds
  },
  body: JSON.stringify([
    {
      "name": "deviceMaster",
      "stmt": "select * limit 100",
      "columns": null
    },
    {
      "name": "device1",
      "stmt": "select * where temp>=24",
      "columns": [
        "temp",
        "co"
      ]
    }
  ])

};
request(options, function (error, response) {
  if (error) throw new Error(error);
  console.log(response.body);
});
