const { app } = require('@azure/functions');
const axios = require('axios');

app.eventGrid('griddbTesting', {
    handler: async (event, context) => {
        context.log('Event grid function processed event:', event);

        const container = 'azureTest'
        const auth = {
            username: process.env.CLOUD_USERNAME,
            password: process.env.CLOUD_PASSWORD
        }
        const headers = {
            'Content-Type': 'application/json'
        }

        //HTTP Request to create our container called azureTest
        const dataCreation = {
            "container_name": container,
            "container_type": "COLLECTION",
            "rowkey": false,
            "columns": [
                { "name": "test", "type": "STRING" }
            ]
        }

        const configCreation = {
            method: 'POST',
            maxBodyLength: Infinity,
            url: process.env.CLOUD_URL + "/containers",
            headers,
            data: dataCreation,
            auth
        }

        // try {
        //     const response = await axios.request(configCreation)
        //     context.log(response.statusText);
        //     context.log(JSON.stringify(response.data));
        //     context.done()
        //     return response
        // } catch (error ) {
        //     context.error(error);

        // }

        //HTTP Request to send data to our container
        const data = JSON.stringify([
            ["GRID EVENT TRIGGERED"]
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
            context.done()
            return response
        } catch (error ) {
            context.error(error);

        }
    }
});