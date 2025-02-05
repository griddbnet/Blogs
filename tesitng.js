require('dotenv').config()

const container = 'azureTest'
const auth = {
    username: process.env.CLOUD_USERNAME,
    password: process.env.CLOUD_PASSWORD
}

const headers = new Headers();
headers.set('Authorization', 'Basic ' + Buffer.from(auth.username + ":" + auth.password).toString('base64'));
headers.append("Content-Type", "application/json");


const body = JSON.stringify({
    "container_name": container,
    "container_type": "COLLECTION",
    "rowkey": false,
    "columns": [{
        "name": "test", "type": "STRING"
    }]
});

const configCreation = {
    method: 'POST',
    maxBodyLength: Infinity,
    url: process.env.CLOUD_URL + "/containers",
    headers,
    data: dataCreation,
    auth
}

const requestOptions = {
    method: 'POST',
    headers,
    body,
    redirect: 'follow'
}

fetch("https://cloud5197.griddb.com:443/griddb/v2/gs_clustermfcloud5197/dbs/ZV8YUlQ8/containers", requestOptions)
    .then(response => response.text())
    .then(result => console.log(result))
    .catch(error => console.log('error', error));