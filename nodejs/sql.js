var request = require('request');
var options = {
  'method': 'POST',
  'url': 'https://cloud5197.griddb.com/griddb/v2/gs_clustermfcloud5197/dbs/B2xcGQJy/sql',
  'headers': {
    'Content-Type': 'application/json',
    'Authorization': 'Basic TTAxMU1sd0MxYS1pc3JhZWw6aXNyYWVs'
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
