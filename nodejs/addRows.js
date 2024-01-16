var request = require('request');
var options = {
  'method': 'PUT',
  'url': 'https://cloud5197.griddb.com/griddb/v2/gs_clustermfcloud5197/dbs/B2xcGQJy/containers/cloud_quickstart/rows',
  'headers': {
    'Content-Type': 'application/json',
    'Authorization': 'Basic TTAxMU1sd0MxYS1pc3JhZWw6aXNyYWVs'
  },
  body: JSON.stringify([
    [
      "2024-01-09T10:00:01.234Z",
      0.003551,
      50,
      false,
      0.00754352,
      false,
      0.0232432,
      21.6
    ],
    [
      "2024-01-09T11:00:01.234Z",
      0.303551,
      60,
      false,
      0.00754352,
      true,
      0.1232432,
      25.3
    ],
    [
      "2024-01-09T12:00:01.234Z",
      0.603411,
      70,
      true,
      0.00754352,
      true,
      0.4232432,
      41.5
    ]
  ])

};
request(options, function (error, response) {
  if (error) throw new Error(error);
  console.log(response.body);
});

