In this blog, we will be showcasing the GERN stack and how to properly implement CRUD (Create, Read, Update, Delete) with React, Express, and GridDB. To help demonstrate this, we will showcase a data table with fake sensor data. We will also be creating a docker environment for this simple application so that users may try this on their machine without much hassle.

 To mimic a possible real world scenario, the data and temp values are not able to be updated. Instead, there will be a location column which can be updated (think about a sensor possibly moving to a new building). The create portion of CRUD happens on page load -- all sensors are generated and saved into GridDB on each page load into the container called `sensorsblog`. 

 The update portion will allow users to update the location of a sensor. Deleting will delete the row outright and then re-query the container and update the table with the live data from the GridDB server.

To top it all off, we have set up some docker containers which will allow you to run the entirety of this project using a simple `docker-compose` command.

## Docker 

To run this project, please first install docker and docker-compose on your machine.

### Docker Compose

To run this project via Docker compose, simply run the following the command: 

```bash
$ docker-compose up
```

Once it's done building the images, the frontend will run and the GridDB DB itself will be starting up. At this point you can navigate to your browser and see the project, but you should wait until the docker output shows GridDB as fully running to actually see the project; otherwise you'll just have a mostly blank table with no functionality.

And if you're wondering how the `docker-compose.yml` file looks: 

```bash
version: '3'

services:

  griddb-server:
    build: ./server

    expose:
      - "10001"
      - "10010"
      - "10020"
      - "10030"
      - "10040"
      - "10050"
      - "10080"
      - "20001"
      - "41999"

    healthcheck:
        test: ["CMD", "tail", "-f", "tail -f /var/lib/gridstore/log/gridstore*.log"]
        interval: 30s
        timeout: 10s
        retries: 5


  frontend:
    build: .
    
    ports:
      - "2828:2828"

    restart: unless-stopped
    depends_on:
      - griddb-server
    links:
      - griddb-server
```

And then you can of course read through each of the Dockerfile's contents if you want to see these containers are built.

You can now skip to the CRUD section or read through running this project not through docker-compose, but through just regular ol' docker containers instead.



### GridDB Server

To run GridDB server, you can simply pull from Dockerhub

```bash
$ docker network create griddb-net
$ docker pull griddbnet/griddb:5.0
```

We first create a Docker network to allow our application to easily communicate with our GridDB container.

Now run your GridDB server: 

```bash
$  docker run --network griddb-net --name griddb-server -d -t griddbnet/griddb
```

### Nodejs Application

To run the Nodejs part of the application, there is a Dockerfile in the root of this directory. 

You can build the image and then run pretty easily: 

```bash
$ docker build -t griddb-nodejs .
$ docker run --network griddb-net --name griddb-nodejs -p 2828:2828 -d -t griddb-nodejs
```

And now if you navigate to `http://localhost:2828` you will the see full app running.

If you're curious as to how these containers work, you can read this [previous blog] (https://griddb.net/en/blog/improve-your-devops-with-griddb-server-and-client-docker-containers/)

## CRUD Operations

As stated earlier, CRUD stands for CREATE, READ, UPDATE, and DELETE. These are the main operations used for persistant storage apps -- things that will persist even after the user logs off/turns off the app. 

### CREATE

To showcase CREATE, we generate fake sensor data and save our records into a time series container and read that data immediately on page load. Each time the page is loaded, the container is dropped and re-made. We use express to easily create our endpoints -- in this case, our endpoint `/firstLoad` will be called on first page load. 

The follow code snippet is from the frontend. The React Use Effect hook with an empty array at the end means it will run just once on page load. Here you can see it calls the `/firstLoad` endpoint and then washes the data that is sent from our GridDB server to fit into our data table

```javascript
  useEffect(() => {
    var xhr = new XMLHttpRequest();

    xhr.onreadystatechange = function () {

      if (xhr.readyState !== 4) return;
      if (xhr.status >= 200 && xhr.status < 300) {
        let resp = JSON.parse(xhr.responseText);
        let res = resp.results


        var t = []
        for (let i = 0; i < res.length; i++) {
          let obj = {}
          obj["id"] = i
          obj["timestamp"] = res[i][0]
          obj["location"] = res[i][1]
          obj["data"] = res[i][2].toFixed(2)
          obj["temperature"] = res[i][3]
          t.push(obj)
        }
        console.log("rows: ", rows)
        setRows(t)
      }
    };
    xhr.open('GET', '/firstLoad');
    xhr.send();

  }, [])
```

And here is the code in the backend: 

```javascript
app.get('/firstLoad', async (req, res) => {
    try {
        await putCont();
        let queryStr = "select *"
        var results = await queryCont(queryStr)
        res.json({
            results
        });
    } catch (error) {
        console.log("try error: ", error)
    }
});
```

Here you can see when this endpoint is called it calls the function `putCont();`. This function is what handles our CREATE. 

```javascript
const putCont = async () => {
    console.log("Putting Container")
    const rows = generateSensors();
    try {
        await store.dropContainer(containerName);
        const cont = await store.putContainer(conInfo)
        await cont.multiPut(rows);
    } catch (error) {
        console.log("error: ", error)
    }
}

const generateSensors = () => {

    let numSensors = 10
    let arr = []
    console.log("Generating sensors")

    for (let i = 1; i <= numSensors; i++) {
        let tmp = [];
        let now = new Date();
        let newTime = now.setMilliseconds(now.getMinutes() + i)
        let data = parseFloat(getRandomFloat(1, 10).toFixed(2))
        let temperature = parseFloat(getRandomFloat(60, 130).toFixed(2))
        tmp.push(newTime)
        tmp.push("A1")
        tmp.push(data)
        tmp.push(temperature)
        arr.push(tmp)
        
    }
 //   console.log("arr: ", arr)
    return arr;

}
```

When putCont is called it first calls generateSensors to create the fake sensor data based on the current time. When the data is done being created, we drop the old container (if it exists) and then `putContainer` and then `multiPut` all rows of generated sensor data.

Immediately after running this, we run the READ operation in our `/firstLoad` endpoint

### READ

To read from GridDB, we simply use a SQL string and the query API call. In this case, we ran `select *` against our container name and then send our results back up to the frontend as json.

Once the frontend gets and transforms the data, it uses a React hook to update the state which will then re-render our data table with the updated data. 

In this case, we are using the [material ui for React](https://mui.com/material-ui/getting-started/overview/) to make our app nice. 

### UPDATE

To make the app somewhat realistic our LOCATION column is the only one that is updatable. With GridDB to update a row, you simply `put` the row again into your container. If that rowkey already exists, it will simply update the values with the new ones that pushed. 

In the case of the frontend, the data table component from material ui made updating a bit easier as it was a built in function. Using the framework meant we could tell our app right from the jump which columns were editable. In this case, we also added a green background on the editable cells. 

For the frontend, we handled editing the location like so: 

```javascript
  const updateRow = useCallback((row) => {
    console.log("Str: ", row)
    fetch('/update', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ row })
    }).then(function (response) {
      console.log(response)
      return response.json();
    });
  }, [])

  const processRowUpdate = useCallback(
    async (newRow) => {
      // Make the HTTP request to save in the backend
      console.log("new row: ", newRow)
      const response = await updateRow(newRow);
      console.log("Response from roiw update: ", response)
      return newRow;
    },
    [],
  );
```

In this case, we are using the useCallback React hook to handle the processing of row updates. When the user hits ENTER or clicks away from the editable cell, the `processRowUpdate` function will run which then calls the `updateRow` function. This will send over our new row back to our backend with the `/update` endpoint. This endpoint simply expects a singular row being sent back to the backend.

```javascript
app.post("/update", jsonParser, async (req, res) => {
    const newRowObj = req.body.row
    const newRowArr = []

    for (const [key, value] of Object.entries(newRowObj)) {
        newRowArr.push(value)
    }
    newRowArr.shift(); 
    newRowArr[2] = parseFloat(newRowArr[2])
    console.log("new row: from endpoint: ", newRowArr)

    try {
        let x = await updateRow(newRowArr)
        console.log("return of update row: ", x)
        res.status(200).json(true);
    } catch (err) {
        console.log("update endpoitn failure: ", err)
    }
});
```

Here the endpoint grabs the data from our frontend and then transform the data from an obj down to an array as this is what GridDB expects. We then send this data to the function called `updateRow`

```javascript
const updateRow = async (newRow) => {
    try {
        const cont = await store.putContainer(conInfo)
        const res = await cont.put(newRow)
        return res
    } catch (err) {
        console.log("update row error: ", err)
    }
}
```

This function is rather simple -- it simply takes the new data and puts the row back into GridDB. Even if the row exists, it will update that row.

To make sure the row was actually updated, you can of course always query the container `sensorsblog` with the griddb shell.

### DELETE

To delete, we call the GridDB remove api key. To do this, we simply feed into the API call the rowkey which we intend to remove. Because we are mimicking an IoT environment, the GridDB container is a time series container, meaning the rowkey is the timestamp. 

For the frontend, we allow the user to select rows with checkboxes and then once the user clicks the Delete button, it will send the rows back to the backend to be processed and extract the rowkeys (timestamps) and then one by one removed.

First, let's look at the backend. We are going to use the `/delete` endpoint: 

```javascript
app.post("/delete", jsonParser, async (req, res) => {
    const rows = req.body.rows

    try {
        let x = await deleteRow(rows)
        console.log("deleting rows")
        res.status(200).json(true)
    } catch (err) {
        console.log("delete row endpoint failure: ", err)
    }
})
```

Nothing too different here. But as you can see, we are calling the `deleteRow` function.

```javascript
const deleteRow = async (rows) => {
    console.log("Rows to be deleted: ", rows)
    var rowKeys = []
    rows.forEach ( row => {
        rowKeys.push(row.timestamp)
    })
    console.log("row keys: ", rowKeys) 
    try {
        const cont = await store.putContainer(conInfo)
        rowKeys.forEach ( async rowKey => {
            let res = await cont.remove(rowKey)
            console.log("Row deleted: ", res, rowKey)
        })
        return true
    } catch (err) {
        console.log("update row error: ", err)
    }

}
```

For the most part, this is a really simple function -- all it's doing is grabbing the rowkeys from the entire row data from the frontend and then looping through to remove each rowkey one by one.

The last bit that's important is that immediately following the actual running of the delete function from the frontend, we will query the GridDB container again to update our data table with the actual content in our GridDB container.

The following function is what runs everytime the delete button is clicked: 

```javascript
  const handleClick = async () => {
      console.log("Rows to delete: ", selectedRows)
      const response = await deleteRow(selectedRows);
      console.log("Response from roiw update: ", response)


    var xhr = new XMLHttpRequest();

    xhr.onreadystatechange = function () {

      if (xhr.readyState !== 4) return;
      if (xhr.status >= 200 && xhr.status < 300) {
        let resp = JSON.parse(xhr.responseText);
        let res = resp.results


        var t = []
        for (let i = 0; i < res.length; i++) {
          let obj = {}
          obj["id"] = i
          obj["timestamp"] = res[i][0]
          obj["location"] = res[i][1]
          obj["data"] = res[i][2].toFixed(2)
          obj["temperature"] = res[i][3]
          t.push(obj)
        }
        console.log("rows: ", rows)
        setRows(t)
      }
    };
    xhr.open('GET', '/updateRows');
    xhr.send();


    }
```

We are reusing the code from the `firstLoad` to make things easy on us. Once the rows are deleted, it will re-read the container and transform the data and update the data table with the real contents of our GridDB container.


## Conclusion

The full source code can be found here:
