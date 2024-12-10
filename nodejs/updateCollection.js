require('dotenv').config()
var request = require('request');
const base = process.env.GRIDDB_CLOUD_URL
const creds = process.env.USER_PASS
var options = {
  'method': 'PUT',
  'url': base+'/containers/deviceMaster/rows',
  'headers': {
    'Content-Type': 'application/json',
    'Authorization': 'Basic '+creds
  },
  body: JSON.stringify([
    [
      "device1",
      "01",
      "CA",
      "23412",
      "2023-12-15T10:45:00.032Z",
      "working"
    ]
  ])

};
request(options, function (error, response) {
  if (error) throw new Error(error);
  console.log(response.body);
});