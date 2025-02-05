const { app } = require('@azure/functions');
var https = require('follow-redirects').https;

app.eventGrid('eventGridTrigger1', {
    handler: (event, context) => {
        context.log('Event grid function processed event:', event);

        var options = {
        'method': 'POST',
        'hostname': 'cloud5197.griddb.com',
        'port': 443,
        'path': '/griddb/v2/gs_clustermfcloud5197/dbs/ZV8YUlQ8/containers',
        'headers': {
            'Authorization': 'Basic TTAxZ2FYMFZrRy1pc3JhZWw6aXNyYWVs',
            'Content-Type': 'application/json'
        },
        'maxRedirects': 20
        };

        context.log(options)

        var req = https.request(options, function (res) {
        var chunks = [];

        res.on("data", function (chunk) {
            chunks.push(chunk);
            context.log(chunk)
        });

        res.on("end", function (chunk) {
            var body = Buffer.concat(chunks);
            context.log(body.toString());
        });

        res.on("error", function (error) {
            context.error(error);
        });
        });

        var postData = JSON.stringify({"container_name":"testing","container_type":"COLLECTION","rowkey":false,"columns":[{"name":"test","type":"STRING"}]});

        req.write(postData);

        req.end();
            }
});
