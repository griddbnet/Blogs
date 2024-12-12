require('dotenv').config()
var request = require('request');
const base = process.env.GRIDDB_WEBAPI_URL
const creds = process.env.USER_PASS
var options = {
  'method': 'DELETE',
  'url': base+'/containers/deviceMaster/rows',
  'headers': {
    'Content-Type': 'application/json',
    'Authorization': 'Basic '+creds
  },
  body: JSON.stringify([
    "device1"
  ])

};
request(options, function (error, response) {
  if (error) throw new Error(error);
  console.log(response.statusCode);
});
