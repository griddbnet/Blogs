var request = require('request');
var options = {
  'method': 'POST',
  'url': 'https://cloud5197.griddb.com/griddb/v2/gs_clustermfcloud5197/dbs/B2xcGQJy/containers/device1/rows',
  'headers': {
    'Content-Type': 'application/json',
    'Authorization': 'Basic TTAxMU1sd0MxYS1pc3JhZWw6aXNyYWVs'
  },
  body: JSON.stringify({
    "offset": 0,
    "limit": 100,
    "condition": "temp >= 30",
    "sort": "temp desc"
  })

};
request(options, function (error, response) {
  if (error) throw new Error(error);
  console.log(response.body);
});

