const request = require('request');
const options = {
  'method': 'GET',
  'url': 'https://cloud5197.griddb.com/griddb/v2/gs_clustermfcloud5197/dbs/B2xcGQJy/checkConnection',
  'headers': {
    'Authorization': 'Basic TTAxMU1sd0MxYS1pc3JhZWw6aXNyYWVs',
    'User-Agent':'PostmanRuntime/7.29.0'
  }
};
request(options, function (error, response) {
  if (error) throw new Error(error);
  console.log("Response Status Code: ", response.statusCode);
});

