const { app } = require('@azure/functions');
const axios = require('axios');

const sslRootCAs = require('ssl-root-cas/latest')
sslRootCAs.inject().addFile("./griddb-com-chain.pem")

app.eventGrid('griddbTesting', {
    handler: async (event, context) => {
        context.log('Event grid function processed event:', event);

        const topic = event.topic;
        const substrings = topic.split('/');
        const hubName = substrings[substrings.length-1].toLowerCase();

        const b64Data = event.data.body;
        const jsonStr = Buffer.from(b64Data, 'base64');
        const json = JSON.parse(jsonStr);
        context.log(json);

        const {deviceId, ts, temp, speed} = json[0];
        const container = hubName + "_" + deviceId;
        context.log(container);

        const auth = {
            username: process.env.CLOUD_USERNAME,
            password: process.env.CLOUD_PASSWORD
        }
        const headers = {
            'Content-Type': 'application/json'
        }

        const data = JSON.stringify([
            [ts, temp, speed]
        ]);


        let config = {
            method: 'PUT',
            maxBodyLength: Infinity,
            url: process.env.CLOUD_URL + "/containers/" + container + "/rows",
            headers,
            data,
            auth
        };

        try {
            const response = await axios.request(config)
            context.log(response.statusText);
            context.log(JSON.stringify(response.data));
            return response
        } catch (error ) {
            context.error(error);

        }
    }
});