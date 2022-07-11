const griddb = require('griddb-node-api');

const factory = griddb.StoreFactory.getInstance();
const store = factory.getStore({
    "host": process.argv[2],
    "port": parseInt(process.argv[3]),
    "clusterName": process.argv[4],
    "username": process.argv[5],
    "password": process.argv[6]
});

const containerName = 'Sample_RowExpiration';
const expirationInfo = new griddb.ExpirationInfo(100, griddb.TimeUnit.DAY, 5);
const conInfo = new griddb.ContainerInfo({
    'name': containerName,
    'columnInfoList': [
        ["date", griddb.Type.TIMESTAMP],
        ["value", griddb.Type.DOUBLE]
    ],
    'type': griddb.ContainerType.TIME_SERIES, 'expiration' : expirationInfo
});
const sampleTimeSeriesRowExpiration = async function(){
    await store.dropContainer(containerName);
    await store.putContainer(conInfo);
    console.log("Create TimeSeries & Set Row Expiration name=%s", containerName);
    console.log('Success!')
}

sampleTimeSeriesRowExpiration().catch(err => {
    console.log(err.message);
});
