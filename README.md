# Using A No Code Solution to Display your IoT Data with GridDB

We have written articles on how to use python to create no "frontend-code" dashboards: [Create Interactive Dashboard with Streamlit, Python and GridDB](https://griddb.net/en/blog/create-interactive-dashboard-with-streamlit-python-and-griddb/) & [Create an Interactive Dashboard using Python and the GridDB Cloud](https://griddb.net/en/blog/create-an-interactive-dashboard-using-python-and-the-griddb-cloud-no-frontend-code-needed/) and have also tackled articles on how to use node.js and react.js to create simple dashboards: [An Alternative to the MERN Stack: Create a Query Builder with the GERN Stack](https://griddb.net/en/blog/gern-stack/) &  [CRUD Operations with the GERN Stack](https://griddb.net/en/blog/crud-gern-stack/). 

In this article we will be following a similar approach, but will instead be leveraging [bubble](https://bubble.io/), a "no code" solution which allows users to create web pages without fussing without any frontend code at all. This means that once we have set up our backend API with some basic endpoints, we can build a usable dashboard for our IoT data without even touching HTML/CSS/JavaScript.

Despite not needing to use frotnend code, we will still need to tackle using some node.js code for the backend which communicates directly with GridDB. You can of course create your web server in any language you prefer, but for this article we used node.js. 

As for the dataset, we elected to use simulated IoT data that we created in the free udemy course: [Create a working IoT Project - Apache Kafka, Python, GridDB](https://www.udemy.com/course/create-a-working-iot-project-with-iot-database-griddb/learn/lecture/37912776#overview). The quick rundown of it is: we run the project and it will fill up our GridDB docker container with a bunch of simulated IoT data with numerous meter data.

So, the flow of operations are as follows: 

1. Run the IoT Project to ingest data to our GridDB Server
2. Create node.js server with the GridDB node.js connector
3. Write endpoints for CRUD (Create, Read, Update, Delete) reading our IoT data
4. Expose the port of your server
5. use bubble api connector to reach data points
6. Create bubble integration frontend (line chart, dropdown menus, tables) 

We will go through each of the numbered list one by one until the project is done, though we will not being too much in-depth with certain aspects.

## Ingesting Data with IoT Project

You can sign up for the free udemy course to learn about this portion more in-depth; it will show you how to connect on the field sensors and send data through Kafka to your GridDB database (among other things). 

If you don't quite have the time for a full course, the source code of this project can be found here: [iot_project](https://github.com/griddbnet/iot_project) with the instructions on how to run this in the README. Once you run it, as explained above, you will have many containers full of various IoT-like data rows which will be the basis of the data we display on Bubble.

Next up will be reading and updating this data with a node.js server.

## Create a node.js Server with a GridDB Connector

Next we will need a way for our Bubble dashboard to communicate with our running GridDB server. On the Bubble side, we can use the API Connector to make HTTP Requests -- this part is easy. From our server side, we will need to create our endpoints to serve up our GridDB Data. We will create a node.js project with express and the GridDB Connector as our packages: 

<div class="clipboard">
<pre><code class="language-sh">    "dependencies": {
      "express": "^4.18.2",
      "griddb-node-api": "^0.8.5"
    }</code></pre>
</div>

If are just following along and cloning this repo, you can simply run `npm install` to install the GridDB nodejs connector and express.js. 

## Write Endpoints for CRUD Actions Using our IoT Data

This section will be kind of a rehash of our previous articles so we will not go into much detail here. The basic idea will be as follows: use express.js to create endpoints which will query GridDB and allow the user to make different queries based on different inputs on the Bubble no code frontend. Following CRUD, we will need to make endpoints to fulfill the acronym. Let's take a look at a few functions here for querying GridDB and sending the data to our frontend: 

First, let's look at connecting to our GridDB docker container: 

<div class="clipboard">
<pre><code class="language-js">const griddb = require('griddb-node-api');
var factory = griddb.StoreFactory.getInstance();
var store = factory.getStore({
    "notificationMember": "griddb-server:10001",
    "clusterName": "myCluster",
    "username": "admin",
    "password": "admin"
});</code></pre>
</div>

The node.js code and the GridDB Container share a docker network so we simply connect to the GridDB docker container's hostname/service name ("griddb-server"). Next, let's take a look at some helper functions and then the API endpoints themselves.

The star of this show is a generic funtion to query GridDB. It will take in three parameters: our container name (our IoT data dictates that each sensor gets its own GridDB container), our query string, and lastly, the number of points to display in our frontend. With these parameters we can allow the user to select the sensor ID and the number of points to display on Bubble: 

<div class="clipboard">
<pre><code class="language-js">const queryCont = async (containerName, queryStr, numOfPoints) => {

    var data = [];
    try {
        // console.log("containerName: ", containerName, queryStr, numOfPoints);
        const col = await store.getContainer(containerName)
        const query = await col.query(queryStr)
        const rs = await query.fetch(query)
        let i = 0;
        while (rs.hasNext()) {
            if (i < numOfPoints) {
                let temp = {}
                temp["timestamp"] = rs.next()[0]
                temp["kwh"] = rs.next()[1]
                temp["temp"] = rs.next()[2]
                data.push(temp)
                i++
            } else {
                break;
            }
        }
        return data
    } catch (error) {
        console.log("error: ", error)
    }
}</code></pre>
</div>

And here is what the endpoints look like: 

<div class="clipboard">
<pre><code class="language-js">app.get('/get', async (req, res) => {
    try {
        var id = req.query.id;
        var numOfPoints = req.query.points;
        let queryStr = "select *"
        let containerName = "meter_" + id;
        var results = await queryCont(containerName, queryStr, numOfPoints);
        res.json({
            results
        });
    } catch (error) {
        console.log("try error: ", error)
    }
});

app.put("/update", jsonParser, async (req, res) => {
    const newRowObj = req.body
    const id = req.query.id;
    try {
        let x = await updateRow(newRowObj, id)
        console.log("return of update row: ", x)
        res.status(200).json(true);
    } catch (err) {
        console.log("update endpoitn failure: ", err)
    }
});</code></pre>
</div>

The `/get` endpoint will allow the user to select the number of points to display and of the ID which they wish to display. And predictably, `/update` will grab the rowkey of the row and update the values outside of the rowkey based on the user's input. In this case, we will only allow the user to change the kwh.

## Expose the Port 

Now that our server is ready to be queried and send back data as an HTTP Response, we can expose our port to be queried by the Bubble API Connector plugin. For this portion, I exposed the port via docker compose and then also expose the port on my Azure firewall (I am running this node.js server on an Azure virtual machine)

<div class="clipboard">
<pre><code class="language-sh">  api:
    build:
      context: api
      dockerfile: Dockerfile
    container_name: api
    ports:
      - "8080:8080"
    restart: unless-stopped
    depends_on: 
      - griddb-server
    links: 
      - griddb-server</code></pre>
</div>

And now we can query our endpoints, for example: `http://serverIP:8080/get`.

## Bubble.io

And now on to the star of the show: bubble. After initial sign up and creating of a new app, let's get the endpoints set up. To do so, we will need to grab some plugins: API Connector, Chart Element, and Google Material Icons. The API Connector will be how we reach our node.js endpoints to display our data. So to start, let's create our API Calls

### Bubble API Connector HTTP Requests

Let's go through each of the endpoint calls we will need to make before we fiddle around with the frontend design.

#### Get Rows

First up: `Get Rows`. We will create this one as "Data" (as opposed to action). We will make a GET HTTP Call to `/get` with two parameters: Id & num, which correspond to container name and number of points. 

![]screenshot

You can see here we are using `[]` as params in our API call. We will be inserting dynamic data based on what the user inputs from the frontend into our endpoint. For now, we will set some default values: id: 0, num: 10. 

And then once we have everything down, we can initialize the call and set the type of data: 

![]screenshot

#### Delete Row with Row Key and Container ID

Next we will want to be able to delete a row. We will have a user click a row on the data table and send back to the server the container name and the rowkey (timestamp) to be deleted. this will call the `/delete` endpoint with a POST request. This API Call will be of type "Action" which means we can call it from the frontend by clicking buttons, etc.

![]screenshot

And then of course please initialize it.

#### Update Row

Now let's create the update row Action. The idea behind this call will be to send back an entire row of data with a valid rowkey and container name to the backend. It will update that row with the corresponding rowkey with the new data attached in the body of that API call. 

![]screenshot

The JSON Body we send in the HTTP Request will attempt to receive from the parameters from user input from the frontend.

The idea is: the user clicks an edit button on a specific row in the data table and it will allow the user to change the kwh column of that row. We will then send the data to the backend to update the GridDB container data.

#### Create Row

Lastly we will include a simple create row button into the frontend which will simply create a new row with the current time and randomly generated values for kwh and temp. This is the "fakest" implementation of a real IoT usecase because normally you'd be creating new containers for new sensors, but this is just a simple showcase of how you could create new data. So let's make a new API Call action to create this: 

![]screenshot

Because the id is generated server side, the only parameter we need is the meter ID number to correspond to the container name in which we will write our new row of generated data into. 

To verify that the data is in the container, you can of course run the GridDB CLI tool on the GridDB docker container to query your container; your new row should be at the bottom as the time of that row will be the most recent.

## Bubble Frontend Designing

This portion will also not be too in-depth as it's mostly related to using Bubble itself, but here is what I did. I created two input dropdowns which correspond to the meter ID and number of points. 


![]screenshot

AOnce a user selects both of these, the chart and the table will appear with the GridDB Data: 

![]screenshot

The button to generate new rows will also appear once the user has selected the container ID we want to work with

### How to Set Up Bubble Table

As explained earlier, I will not be going too in-depth with how to set up this, just the portions where we make API Calls from the frontend and how to display said data. With that out of the way, let's take a look at building out data table.

First we need to set the `type of content` as a Get Rows result. This corresponds to our first endpoint of type "data". We set the data source as `Get Row's results`. From there we set the table column names and then the first row we set as repeating and then enter in text boxes with the value corresponding to the column: for example, the timestamp column will have dynamic data of: `current row's Get Rows result's timestamp:formatted in ISO`. You can do the same for the kwh. Because this row is repeating, every row under this one will copy the format of what we set up, but will go through the entire structure of our data coming from our Get Rows API Call.

![screenshot]

Beyond that, we will also be including two extra columns: one for the delete button and one for the edit button. The delete button will have an action directly tied to it: delete this row by sending an API Call to /delete with the row key -- we can accomplish this with a workflow. We set one up to occur whenever our delete button is clicked. The trickier of these two actions will be the Edit Button

#### Setting up The Update Button and Action

Next let's make the edit button. On the design side, let's add another column to the datatable and add in an edit button using the material icon. Now double click it and start/edit the workflow. 

From here we can set it up so that when a user clicks the edit button, it will bring up a popup (which you can set up on the design tab). Ideally the flow works like this: there is a row the user wants to edit, the user clicks the edit button, a pop up appears with an input and a button. The input is already pre-filled with the current kwh value for that row. The user can change that value and click that button, which will then send that data back to the server to update the row with the corresponding row. 

The tricky part of trying to implement this is that when we send our data via our API Call, the Request body needs to have all columns filled, including data we are not displaying (the schema is: timestamp, kwh, temp), but we are only displaying the kwh. So how do we send all relevant data to the API Call when the row is missing the `temp` value? We use states.

When the user clicks the edit button, we kick off our workflow to show the popup. We also set the data of our to be displayed inside the popup and then finally the kwh value into the input. The other values we need, timestamp and temp, will be saved states. And so when we make our API call at the end of this workflow, we can pull the user input kwh value from the input, the timestamp (rowkey) from the state, and the temp from the state as well. Now we can send off our HTTP request to our server to update the data.

![]screenshot

### The Line Chart

Much like the datatable, the line chart will be the same. It will use the API Call data "Get Rows" and will use the user inputs from the dropdown for the parameters of our API Call. We will then set the value expression as the current point kwh and the label expression as the row's timestmap

![]screenshot

## Conclusion

And that's all! With no frontend code whatsoever -- and very minimal backend code -- we were able to display complex data fromn GridDB onto an attractive and interactive dashboard.