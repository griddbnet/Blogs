const { app } = require('@azure/functions');
const axios = require('axios');

const sslRootCAs = require('ssl-root-cas/latest')
sslRootCAs.inject().addFile("./griddb-com-chain.pem")

app.eventGrid('eventGridTrigger1', {
    handler: async (event, context) => {
        context.log('Event grid function processed event:', event);

        const deviceId = event.data.deviceId;
        const hubName = event.data.hubName;
        const container = hubName + "_" + deviceId;

        const ts = event.eventTime;

        const auth = {
            username: process.env.CLOUD_USERNAME,
            password: process.env.CLOUD_PASSWORD
        }
        const headers = {
            'Content-Type': 'application/json'
        }

        const createTable = `CREATE TABLE IF NOT EXISTS "${container}" (date TIMESTAMP NOT NULL PRIMARY KEY, temperature FLOAT, speed FLOAT) USING TIMESERIES`

        //HTTP Request to send data to our container
        const data = JSON.stringify([
            { "stmt": createTable }
        ]);

        context.log("DATA: ", data)

        let config = {
            method: 'POST',
            maxBodyLength: Infinity,
            url: process.env.CLOUD_URL + "/sql/ddl",
            headers,
            data,
            auth
        };

        try {
            const response = await axios.request(config)
            context.log(response.statusText);
            context.log(JSON.stringify(response.data));
        } catch (error) {
            context.error(error);
        }

        const dataPut = JSON.stringify([
            [deviceId, ts]
        ]);

        context.log("putting row to device master");
        context.log(dataPut);

        let configPut = {
            method: 'PUT',
            maxBodyLength: Infinity,
            url: process.env.CLOUD_URL + "/containers/" + hubName + "_deviceMaster/rows",
            headers,
            data: dataPut,
            auth
        };



        try {
            const response = await axios.request(configPut)
            context.log(response.statusText);
            context.log(JSON.stringify(response.data));
            return response
        } catch (error) {
            context.error(error);

        }
    }
});