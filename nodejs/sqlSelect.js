require('dotenv').config()
var request = require('request');
const base = process.env.GRIDDB_WEBAPI_URL
const creds = process.env.USER_PASS
var options = {
  'method': 'POST',
  'url': base+'/sql',
  'headers': {
    'Content-Type': 'application/json',
    'Authorization': 'Basic '+creds
  },
  body: JSON.stringify([
    {
      "type": "sql-select",
      "stmt": "SELECT * FROM deviceMaster"
    },
    {
      "type": "sql-select",
      "stmt": "SELECT temp, co FROM device1 WHERE temp>=24"
    }
  ])

};
request(options, function (error, response) {
  if (error) throw new Error(error);
  console.log(response.body);
});
