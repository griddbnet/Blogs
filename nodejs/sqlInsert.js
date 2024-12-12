require('dotenv').config()
var request = require('request');
const base = process.env.GRIDDB_WEBAPI_URL
const creds = process.env.USER_PASS
var options = {
  'method': 'POST',
  'url': base+'/sql/update',
  'headers': {
    'Content-Type': 'application/json',
    'Authorization': 'Basic '+creds
  },
  body: JSON.stringify([
    {
      "stmt": "insert into deviceMaster(equipment, equipmentID, location, serialNumber, lastInspection, information) values('device2', '02', 'MA', '34412', TIMESTAMP('2023-12-21T10:45:00.032Z'), 'working')"
    }
  ])

};
request(options, function (error, response) {
  if (error) throw new Error(error);
  console.log(response.body);
});