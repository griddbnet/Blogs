With the release of GridDB v5.6, we are taking a look at the new features that come bundled with this new update. To read the entirety of the notes, you can read them directly from GitHub: [GridDB CE v5.6 Release Notes](https://github.com/griddb/griddb/blob/master/docs/GridDB-5.6-CE-RELEASE_NOTES.md). You can also read the detailed GridDB documenation, including the new v5.6 updates here: [https://www.toshiba-sol.co.jp/en/pro/griddb/docs-en/v5_6/GridDB_FeaturesReference.html](https://www.toshiba-sol.co.jp/en/pro/griddb/docs-en/v5_6/GridDB_FeaturesReference.html)

Of the new features, today we are focusing on the new data compression algorithm that is now selectable in the `gs_node.json` config file and automatic time aggregation from the GridDB CLI tool. Prior to v5.6, there were only two methods of compression that were selectable: `NO_COMPRESSION` and `COMPRESSION_ZLIB`. Though the default setting is still no compression for all versions, version 5.6 offers a new compression method called `COMPRESSION_ZSTD`. 

This compression method promises to be more efficient at compressing your data regularly, and also at compressing the data itself, meaning we can expect a smaller footprint. So, in this article, we will inserting a consistent amount of data into GridDB, comparing the resulting storage space taken up, and then finally comparing between all three compression methods.

As for automatic aggregation, we will show a brief demonstration of how it looks at the end of this artcle. But first, compression.

## Methodology

As explained above, we will need to easily compare between three instances of GridDB with the same dataset. To accomplish this, it seems docker would be the easiest method because we can easily spin up or down new instances and change the compression method for each instance. If we do this, then we simply use the same dataset or the same data generation script for each of the instances. 

To get a robust enough dataset to really test the compression alogrithm differences, we decided on 100 million rows of data. Specifically, we wanted the dataset to be similar enough in some respects that the compression can do its job so that we in turn can effectively measure its effectiveness. 

The three docker containers will be `griddb-server1`, `griddb-server2`, and `griddb-server3`. The compression levels are set in the docker-compose file, but we will do it the way that makes the most sense to me: server1 is `NO_COMPRESSION`, server2 is the old compression system (`COMPRESSION_ZLIB`), and server3 is the new compression system (`COMPRESSION_ZSTD`).

So when we run our gen-script, we can use command line arguments to specify which container we want to target. More on that in the next section.

## How to Follow Along

If you plan to build and test out these methods yourself while you read along, you can grab the source code from our GitHub page: [](). 

Once you have the repo, you can start with spinng up your GridDB servers. We will get into how to run the generation data script to push 100m rows of data into your servers in the next section.

To get the three servers running, the instructions are laid out in the docker compose file in the root of the projectory repoistory, so simply run: 

```bash
$ docker compose build
$ docker compose up -d
```

If all goes well, you should have three GridDB containers running: `griddb-server1`, `griddb-server2`, `griddb-server3`

## Implementation

To implement, we used a node.js script which generated 100m rows of random data. Because our GridDB containers are spun up using Docker, we made all three docker containers for GridDB separate services inside of a docker compose file. We then grabbed that docker network name and used it when running our nodejs script.

This means that our nodejs script was also built into a docker container and then we used that to push data into the GridDB containers with the following commands: 

```bash
$ docker build -t gen-data .
$ docker run  --network docker-griddb_default gen griddb-server1:10001
$ docker run  --network docker-griddb_default gen griddb-server2:10001
$ docker run  --network docker-griddb_default gen griddb-server3:10001
```

Here is the nodejs script in its entirety: 

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
    }
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

The code itself is simple and self explanatory but please note that if you plan to follow along, inserting this volume of rows into GridDB takes a long time and you should be prepared to let the script work for ~10-20 minutes, depending on your server's hardware. 

## Compression Method Results

Now that we have our rows of data inside of our three GridDB containers, we can let GridDB handle the actual compressing of the data. This process happens automatically and in the background; you can read more about that here: [https://www.toshiba-sol.co.jp/en/pro/griddb/docs-en/v5_5/GridDB_FeaturesReference.html#database-compressionrelease-function](https://www.toshiba-sol.co.jp/en/pro/griddb/docs-en/v5_5/GridDB_FeaturesReference.html#database-compressionrelease-function).

To check how much space your 100 million rows of data are taking up, you can run the following command against each Docker container of GridDB: 

```bash
$ docker exec griddb-server1 du -sh /var/lib/gridstore

16G	/var/lib/gridstore/
```

Which checks the storage space used up by GridDB in total, including any swap files and logs. If you just want the data: 

```bash
$ docker exec griddb-server1 du -sh /var/lib/gridstore/data

12G	/var/lib/gridstore/data/
```

This, of course, must be repeated for all three containers. 

You can also verify the compression method in your GridDB container like so: 

```bash
$ docker exec griddb-server3 cat /var/lib/gridstore/conf/gs_node.json | grep "storeCompressionMode"

"storeCompressionMode": "COMPRESSION_ZSTD",
```

Beyond testing the storage space used, we tested how long it took to load the data and how long a query takes. You can see the results here in the following table: 

|       | NO_COMPRESSION | COMPRESSION_ZLIB |        COMPRESSION_ZSTD         |
|---------------------|------------------|------------------|-----------------|
| Search (ms)         | 32644            | 20666            | 11475           |
| Agreggation (ms)    | 30261            | 13302            | 8402            |
| Storage (gridstore) | 11968312 (17GB)  | 7162824 (6.9GB)  | 6519520 (6.3GB) |
| Storage (/data)     | 17568708 (12GB)  | 1141152 (1.1GB)  | 1140384 (1.1GB) |
| Insert (m:ss.mmm)   | 14:42.452        | 15:02.748        | 15:05.404       |

To test the query speed, we did both `select *` and agreggation queries like: `select AVG(data) from` and then took the average of 3 results and placed them into the table.

The results are clear: compression helps a lot more than it hurts. It helps save on storage space but also helps query speeds. Version 5.6's compression method seems to both save storage space and also help query speed by a meaningful amount. All of this is done of course on consumer level hardware.

## Automatic Aggregation with CLI

This functionality utilizes cron on your linux machine to regularly run the script you create. But essentially, what this addition allows is for you to run an aggregation on one of your containers, and then push all of those values onto another table, allowing for you to periodically run new queries, perhaps in the background when your resources aren't in use. This way you can have updated/fresh values on hand without needing to conduct your aggregations and wait for possibly long calculation times.

The way it works is you can now Insert values from one table into another like so: 

```sql
gs[public]> INSERT OR REPLACE INTO device_output (ts, co) SELECT ts,avg(co) FROM device WHERE ts BETWEEN TIMESTAMP('2020-07-12T00:29:38.905Z') AND TIMESTAMP('2020-07-19T23:58:25.634Z') GROUP BY RANGE(ts) EVERY (20,SECOND);
The 34,468 records had been inserted.
```
And so, knowing this, we can do some clever things, like writing a GridDB CLI script file (`.gsh`), and allowing for that script to get the latest values from a table, run aggregation, and then push them out into your etl_output file. Once you write that script file, you can set up a [cron job](https://crontab.guru/) to regularly schedule the script to run in the background. This process will allow your agg output file to be regularly updated with new, up-to-date values completely automatically! Here is an example script file directly from the docs page: 

```bash
# gs_sh script file (sample.gsh)

# If no table exists, create a partitioning table with intervals of 30 days to output data.
CREATE TABLE IF NOT EXISTS etl_output (ts TIMESTAMP PRIMARY KEY, value DOUBLE)
 PARTITION BY RANGE (ts) EVERY (30, DAY);

# Retrieve the last run time registered. If it does not exist, retrieve the time one hour before the present.
SELECT case when MAX(ts) ISNULL THEN TIMESTAMP_ADD(HOUR,NOW(),-1) else MAX(ts)
 end AS lasttime FROM etl_output;

# Store the retrieved time in a variable.
getval LastTime

# Set the aggregation range between the time retrieved and the present time and obtain the average value for every 20 seconds. Register or update the results into the output container.
INSERT OR REPLACE INTO etl_output (ts, value)
 SELECT ts,avg(value) FROM etl_input
 WHERE ts BETWEEN TIMESTAMP('$LastTime') AND NOW()
 GROUP BY RANGE(ts) EVERY (20, SECOND);
```

In this example, we're placing aggregated results from etl_input into etl_output. Pretty neat!