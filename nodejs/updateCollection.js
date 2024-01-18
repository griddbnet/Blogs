var request = require('request');
var options = {
  'method': 'PUT',
  'url': 'https://cloud5197.griddb.com/griddb/v2/gs_clustermfcloud5197/dbs/B2xcGQJy/containers/deviceMaster/rows',
  'headers': {
    'Content-Type': 'application/json',
    'Authorization': 'Basic TTAxMU1sd0MxYS1pc3JhZWw6aXNyYWVs'
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