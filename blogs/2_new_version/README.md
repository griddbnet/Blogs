With the release of GridDB v5.6, we are taking a look at the new features that come bundled with this new update. To read the entirety of the notes, you can read them directly from GitHub: [GridDB CE v5.6 Release Notes](https://github.com/griddb/griddb/blob/master/docs/GridDB-5.6-CE-RELEASE_NOTES.md).

Of the new features, today we are focusing on the new data compression algorithm that is now selectable in the `gs_node.json` config file. Prior to v5.6, there were only two methods of compression that were selectable: `NO_COMPRESSION` and `COMPRESSION_ZLIB`. Though the default setting is still no compression for all versions, version 5.6 offers a new compression method called `COMPRESSION_ZSTD`. 

This compression method promises to be more efficient at compressing your data regularly, and also at compressing the data itself, meaning we can expect a smaller footprint when actually compressing the data itself. So in this article, we will inserting some data into GridDB and comparing the resulting storage space used and compare between all three compression methods.

## Methodology

As explained above, we will need to easily compare between three instances of GridDB with the same dataset. To accomplisah this, it seems docker would be the easiest method because we can easily spin up or down new instances and change the compression method for each instance. If we do this, then we simply use the same dataset or the same data generation script for each of instances. 

To get a robust enough dataset to really test the compression alogrithm differences, we decided on 100m rows of data. Specifically, we wanted the dataset to be similar enough in some respects that the compression can do its job so that we in turn can effectively measure its effectiveness. 

The three docker containers will be `griddb-server1`, `griddb-server2`, and `griddb-server3`. The compression levels are set in the docker-compose file, but we will do it the way that makes the most sense to me: server1 is `NO_COMPRESSION`, server2 is the old compression system (`COMPRESSION_ZLIB`), and server3 is the new compression system (`COMPRESSION_ZSTD`).

So when we run our gen-script, we can use command line arguments to specify which container we want to target. More on that in the next section.

## Implementation

To implement, we used a node.js script which generated 100m rows of random data. Because our GridDB containers are spun up using Docker, we made all three docker containers for GridDB separate services inside of a docker compose file. We then grabbed that docker network name and used it when running our nodejs script.

This means, our nodejs script was also built into a docker container and then we used that to push data into the GridDB containers with the following commands: 

```bash
$ docker build -t gen-data .
$ docker run  --network docker-griddb_default gen griddb-server1:10001
$ docker run  --network docker-griddb_default gen griddb-server2:10001
$ docker run  --network docker-griddb_default gen griddb-server3:10001
```

The full source code, including our Dockerfiles and docker-compose file can be found in the GitHub repo: [](). Here is the nodejs script in its entirety: 

```javascript
const griddb = require('griddb-node-api');
const process = require('process');

var fs = require('fs');
var factory = griddb.StoreFactory.getInstance();
var store = factory.getStore({
    "notificationMember": process.argv[2],
    "clusterName": "myCluster",
    "username": "admin",
    "password": "admin"
});

const conInfo = new griddb.ContainerInfo({
    'name': "compressionBlog",
    'columnInfoList': [
        ["timestamp", griddb.Type.TIMESTAMP],
        ["location", griddb.Type.STRING],
        ["data", griddb.Type.FLOAT],
        ["temperature", griddb.Type.FLOAT],
    ],
    'type': griddb.ContainerType.COLLECTION, 'rowKey': false
});

function getRandomFloat(min, max) {
    return Math.random() * (max - min) + min;
}

const putCont = async (sensorCount, data, temperature) => {
    const rows = generateSensors(sensorCount, data, temperature);
    try {
        const cont = await store.putContainer(conInfo)
        await cont.multiPut(rows);
    } catch (error) {
        console.log("error: ", error)
    }
}

const generateSensors = (sensorCount, data, temperature) => {
    const arr = []
    let now = new Date();
    for (let i = 1; i <= sensorCount; i++) {
        let tmp = [];
        let newTime = now.setMilliseconds(now.getMinutes() + i)
        tmp.push(newTime)
        tmp.push("A1")
        tmp.push(data)
        tmp.push(temperature)
        arr.push(tmp)
    }//    console.log("arr: ", arr)
    return arr;
}

const AMTROWS = 10000;
const AMTPASSES = 10000;

(async () => {
    try {
        console.log("attempting to gen data and push to GridDB")
        for (let i = 0; i < AMTPASSES; i++) {
            const data = parseFloat(getRandomFloat(1, 10).toFixed(2))
            const temperature = parseFloat(getRandomFloat(60, 130).toFixed(2))
            await putCont(AMTROWS, data, temperature);
        }
        console.log("Finished pushing data!")
    } catch (error) {
        console.log("Error putting to container", error);
    }
})();
```

The code itself is simple and self explanatory but please note that if you plan to follow along, inserting this volume of rows into GridDB takes a long time.