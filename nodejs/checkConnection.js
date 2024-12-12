require('dotenv').config()
var request = require('request');
const base = process.env.GRIDDB_WEBAPI_URL
const creds = process.env.USER_PASS
const options = {
  'method': 'GET',
  'url': base+'/checkConnection',
  'headers': {
    'Authorization': 'Basic '+creds,
    'User-Agent':'PostmanRuntime/7.29.0'
  }
};
request(options, function (error, response) {
  if (error) throw new Error(error);
  console.log("Response Status Code: ", response.statusCode);
});