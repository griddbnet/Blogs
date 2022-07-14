This blog serves as a soft follow up to a [previous blog][1] in which we used [Facebook's React][2], paired with the GridDB Web API, to ingest a CSV file and then visualize said data. In this follow up, we will again use React, but this time we will be using the GridDB Node.js connector instead of the Web API to ingest and serve data to our frontend. You can think of this configuration as the GERN stack: GridDB, Express, React, and node.js; this is an obvious play on the hugely popular [ MERN stack ][3]

The main and obvious difference between the MERN stack and the GERN Stack is of course the database being deployed. The original acronym stands for MongoDB, Express.js, React.js, and node.js. If you are unfamiliar with MongoDB, it is a document-based database which allows for quick prototyping as the NoSQL schema allows for very fluid ingestion of data without any sort of foresight or pre-planning. Using the GERN stack over the MERN stack depends solely on your use case -- if you need much higher performance, if you need to store time series data, or if your data is from IoT sensors -- you would opt to use GridDB over MongoDB.


## Project Overview

To showcase these products, we will create a simple query builder app that will display data that a user selects via dropdown menus. We will go through the process of installing the GridDB node.js client via npm, ingesting our open source data from [Kaggle][4], and then setting up a React frontend to work with a node.js server (backend) connected to GridDB. We will also set the project up to build out the static React assets and be able to deploy this sort of project onto a website (and to avoid using two running node.js servers).

From there, we can move on to the frontend which will be simple: three dropdowns to allow a user to find some data points relating to the dataset which was ingested. We will then showcase sending query strings to our backend to run against our DB and then pushing the results back up to the frontend.

[Full Source Code Found Here](https://github.com/griddbnet/Blogs/tree/query_builder)

Here is a quick example of the completed project: 

![](https://thumbs.gfycat.com/SaneWeightyBlackfish-max-1mb.gif)


## Prerequisites

The following prerequisites are required to run this project: 

- GridDB
- node.js
- GridDB c-client
- GridDB node.js client 


<style>
  ul {
        padding-left: 36px;
    }

    .toc_container {
        border: 1px solid #aaa !important;
        display: table !important;
        font-size: 95%;
        margin-bottom: 1em;
        padding: 20px;
        width: auto;
    }
    
    .toc_title {
        font-weight: 700;
        text-align: center;
    }
    
    .toc_container li,
    .toc_container ul,
    .toc_container ul li,
    .toc_container ol li {
        list-style: outside none none !important;
    }
    

.single-post .post>.entry-content { font-size: 14px !important }

</style>


<div id="toc-block"
    style="background: #f9f9f9 none repeat scroll 0 0;border: 1px solid #aaa; padding: 20px; width: auto; display: table;margin-bottom: 1em;">
  <h5 id="toc">
    Table of Contents
  </h5>
  
  <div class="toc_container">
    <ul id="toc-list" style="list-style-type: none !important;">
      <li>
        <a href="#details">Technical Details</a>
      </li>
      <li style="list-style: outside none none !important;">
        <div class="inner-list">
          <ul style="list-style-type: none !important; padding-left: 9px;">
            <li>
              <a href="#architecture ">Architecture Overview </a>
            </li>
            <li>
              <a href="#frontend-backend "> Setting up the Frontend to Work with the Backend  </a>
            </li>
          </ul>
        </div>
      </li>
      <li>
        <a href="#building">Building and Running</a>
      </li>
      <li style="list-style: outside none none !important;">
        <div class="inner-list">
          <ul style="list-style-type: none !important; padding-left: 9px;">
            <li>
              <a href="#first"> First Steps</a>
            </li>
            <li>
              <a href="#installing-npm"> Installing the GridDB node.js Connector (via npm) </a>
            </li>
            <li>
              <a href="#ingest">How to Create Ingesting Script with Node.js</a>
            </li>
            <li>
              <a href="#running-ingest">Running the Ingest</a>
            </li>
            <li>
              <a href="#implementation">Running The Frontend and Backend</a>
            </li>
          </ul>
        </div>
      </li>
      <li>
        <a href="#set-up"> Setting Up Project Code </a>
      </li>
      <li style="list-style: outside none none !important;">
        <div class="inner-list">
          <ul style="list-style-type: none !important; padding-left: 9px;">
            <li>
              <a href="#getting-started">Getting Started</a>
            </li>
            <li>
              <a href="#querying"> How to Query with GridDB's node.js Connector </a>
            </li>
            <li>
              <a href="#react"> Using React with GridDB </a>
            </li>
            <li>
              <a href="#query-builder"> Creating a Query Builder with React </a>
            </li>
          </ul>
        </div>
      </li>
      <li>
        <a href="#conclusion">Conclusion</a>
      </li>
      <li style="list-style: outside none none !important;">
        <div class="inner-list">
          <ul style="list-style-type: none !important; padding-left: 9px;">
            <li>
              <a href="#source-code">Source Code</a>
            </li>
          </ul>
        </div>
      </li>
    </ul>
  </div>
</div>

## <span id="details"> Technical Details </span>

### <span id="architecture"> Architecture Overview  </span>

As explained above, the technologies being used here fit nicely into the acronym GERN: GridDB, Express, React, node.js. My personal environment was as follows: a CentOS 7 server running GridDB on bare metal. For the web app's backend, I ran the [GridDB node.js connector][5] along with the node.js server and the express.js framework. The frontend consists of React.js being ran via the react bundler tool in its own frontend server; data is shared between the backend and frontend via API endpoints. The end goal will have the React frontend built out to static assets which are then displayed by the backend.

[<img src="https://griddb.net/en/wp-content/uploads/2022/06/diaghram-1.png" class="aligncenter size-full wp-image-28479" />][6]

The frontend server portion was set up using the [npx integrated toolchain](https://reactjs.org/docs/create-a-new-react-app.html). The backend was built simply by creating and adding some express.js code into the `app.js` file.

### <span id="frontend-backend"> Setting up the Frontend to Work with the Backend </span>

One interesting point was adding in a line for proxy in the frontend portion. Because the frontend and backend use their own servers, they each have their own set of packages required to run, so you will need to run `npm install` on both the backend and the frontend. This also means that each server will have its own `package.json`, so in the frontend file, we added in a line indicating a proxy URL for the frontend: `"proxy": "http://localhost:5000"`. This address and port combination correspond to the backend we built using nodejs/griddb/express. 

The purpose of adding this line here means that our fetch API endpoints in the frontend can simply call the endpoint rather than calling the entire address (for example, calling `/query` directly rather than calling `http://localhost:5000/query`). This is perfect for running in a dev environment as it totally squashes CORS issues and keeps things simple.

Another point of interest was setting up the workflow to work with just one server. When running this content within your dev environment, you will likely be running two simultaneous servers (one backend, one frontend), but when deploying to production or just checking out the demo, there is another step you can do. 

In the `package.json` of the root directory, we need to add an `npm run build` script: 

`"build": "cd frontend && npm install && npm run build"`

This will build our React frontend to be static files within the `frontend/build` directory. 

And then we add a snippet of code in our backend server (`app.js`): 

<div class="clipboard">
  <pre><code class="language-js">const path = require('path');
app.use(express.static(path.resolve(__dirname, 'frontend/build')));

app.get('*', (req, res) => {
    res.sendFile(path.resolve(__dirname, 'frontend/build', 'index.html'));
  });
  </code></pre>
</div>

This tells our server to serve up our Frontend of our newly built React contents found within the `frontend/build` directory.

## <span id="building"> Building and Running </span>

### <span id="first"> First Steps </span>

Before we get into the frontend and backend code, let's briefly explain the exact steps of running this on your local machine.

First and foremost, let's do a git clone for the full project's source code

<div class="clipboard">
  <pre><code class="language-sh">$ git clone --branch query_builder https://github.com/griddbnet/Blogs.git</code></pre>
</div>

And then let's install GridDB and run as a service: 

<div class="clipboard">
  <pre><code class="language-sh">$ wget --no-check-certificate https://github.com/griddb/griddb/releases/download/v5.0.0/griddb_5.0.0_amd64.deb
$ sudo dpkg -i griddb_5.0.0_amd64.deb
$ sudo systemctl start gridstore</code></pre>
</div>

[Here is a link to the GitHub Releases page for all distros and for the most up to date release ](https://github.com/griddb/griddb/releases)

Next steps will be installing the Node.js client and then setting up and running the ingest for our demo dataset.

### <span id="installing-npm"> Installing the GridDB node.js Connector (via npm) </span>

To install the node.js connector, you will first need to install the GridDB c-client. To do so, you can grab appropriate package files from the [GitHub page](https://github.com/griddb/c_client/releases). 

On CentOS you can install like so: 

<div class="clipboard">
  <pre><code class="language-sh">$ wget https://github.com/griddb/c_client/releases/download/v5.0.0/griddb-c-client_5.0.0_amd64.deb
  $ sudo dpkg -i griddb-c-client_5.0.0_amd64.deb</code></pre>
</div>

Now with the GridDB c-client installed, you can simply grab the [nodejs package](https://www.npmjs.com/package/griddb-node-api) using npm

<div class="clipboard">
  <pre><code class="language-sh">$ npm i griddb-node-api</code></pre>
</div>

And everything should run now. You can now run the ingest to ingest the `cereal.csv` file and then run project itself.

### <span id="ingest"> How to Create Ingesting Script with Node.js </span>

To start, let's create a simple node.js script to ingest our data from the `csv` file provided by Kaggle. If you are familiar at all with the [Python GridDB Client][8], the node.js iteration will be very familiar.

First, you must import the griddb library, along with the csv parser. To install the parser, simply use `npm`.

`npm install --save csv-parse`

Next up is setting the credentials with the griddb factory get store.

<div class="clipboard">
  <pre><code class="language-javascript">const griddb = require('griddb-node-api');
var { parse } = require('csv-parse');

var fs = require('fs');
var factory = griddb.StoreFactory.getInstance();
var store = factory.getStore({
    "host": process.argv[2],
    "port": parseInt(process.argv[3]),
    "clusterName": process.argv[4],
    "username": process.argv[5],
    "password": process.argv[6]
});</code></pre>
</div>

The schema of the data to be imported is set up as a javascript variable

<div class="clipboard">
  <pre><code class="language-javascript">var containerName = "Cereal"
const conInfo = new griddb.ContainerInfo({
    'name': containerName,
    'columnInfoList': [
        ["name", griddb.Type.STRING],
        ["mfr", griddb.Type.STRING],
        ["type", griddb.Type.STRING],
        ["calories", griddb.Type.INTEGER],
        ["protein", griddb.Type.INTEGER],
        ["fat", griddb.Type.INTEGER],
        ["sodium", griddb.Type.INTEGER],
        ["fiber", griddb.Type.FLOAT],
        ["carbo", griddb.Type.FLOAT],
        ["sugars", griddb.Type.INTEGER],
        ["potass", griddb.Type.INTEGER],
        ["vitamins", griddb.Type.INTEGER],
        ["shelf", griddb.Type.INTEGER],
        ["weight", griddb.Type.FLOAT],
        ["cups", griddb.Type.FLOAT],
        ["rating", griddb.Type.FLOAT]
    ],
    'type': griddb.ContainerType.COLLECTION,
    'rowKey': false
});</code></pre>
</div>

Once the proposed schema is set up, we simply read the `csv` file from using the native node.js file system reader and then loop through the contents of each line and grab the proper values to be inserted into our database

<div class="clipboard">
  <pre><code class="language-javascript">var arr = []
fs.createReadStream(__dirname + '/cereal.csv')
    .pipe(parse({ columns: true }))
    .on('data', (row) => {
        arr.push(row)
    })
    .on('end', () => {
        store.putContainer(conInfo)
            .then(col => {
                arr.forEach(row => {
                    return col.put([
                        row['name'],
                        row['mfr'],
                        row['type'],
                        parseInt(row['calories']),
                        parseInt(row['protein']),
                        parseInt(row['fat']),
                        parseInt(row['sodium']),
                        parseFloat(row['fiber']),
                        parseFloat(row['carbo']),
                        parseInt(row['sugars']),
                        parseInt(row["potass"]),
                        parseInt(row["vitamins"]),
                        parseInt(row["shelf"]),
                        parseFloat(row["weight"]),
                        parseFloat(row["cups"]),
                        parseFloat(row["rating"])
                    ]);
                })
            })
            .then(() => {
                console.log("Success!");
                return true;
            })
            .catch(err => {
                console.log(err);
            });
    })</code></pre>
</div>

### <span id="running-ingest"> Running the Ingest </span>

To ingest the `cereal.csv` file, you can imply run the following code.

<div class="clipboard">
  <pre><code class="language-sh">$ node ingest.js 127.0.0.1:10001 myCluster admin admin</code></pre>
</div>

Once you run this, the entirety of the data should be available in your GridDB server under the container name `Cereal`.

To verify, you can drop into your GridDB shell and check

<div class="clipboard">
  <pre><code class="language-sh">$ sudo su gsadm
  $ gs_sh
  gs[public]> showcontainer Cereal</code></pre>
</div>

```
Database    : public
Name        : Cereal
Type        : COLLECTION
Partition ID: 35
DataAffinity: -

Columns:
No  Name                  Type            CSTR  RowKey
------------------------------------------------------------------------------
 0  name                  STRING                
 1  mfr                   STRING                
 2  type                  STRING                
 3  calories              INTEGER               
 4  protein               INTEGER               
 5  fat                   INTEGER               
 6  sodium                INTEGER               
 7  fiber                 FLOAT                 
 8  carbo                 FLOAT                 
 9  sugars                INTEGER               
10  potass                INTEGER               
11  vitamins              INTEGER               
12  shelf                 INTEGER               
13  weight                FLOAT                 
14  cups                  FLOAT                 
15  rating                FLOAT
```

### <span id="implementation"> Running The Frontend and Backend <span></span></span>

To run this project, you have two options: running as dev mode, or running just one server. To run in a development environment, you will need to run the frontend server first:

<div class="clipboard">
  <pre><code class="language-sh">$ cd frontend && npm install && npm run start</code></pre>
</div>

And then in another terminal you will need to run the backend. After running `$ npm install`, you can run: 

<div class="clipboard">
  <pre><code class="language-sh">$ npm run start 127.0.0.1:10001 myCluster admin admin</code></pre>
</div>

***NOTE: When running GridDB v5.0 as a service, GridDB will be run in FIXED_LIST mode (as opposed to Multicast). You can read more about it [here](https://docs.griddb.net/architecture/structure-of-griddb/#cluster-configuration-methods).***

If you are following along and ran GridDB as a service, the above command will work. If you are running GridDB in Multicast mode, the IP address and port may be different. Please use the correct credentials for your configuration.

Of course, you will need to enter in your own credentials along with the run command; these are all GridDB default values.

But if you want a simpler way to run this project with only one terminal, you can build out the React static assets and simply run the backend server.

<div class="clipboard">
  <pre><code class="language-sh">$ npm run build # builds out the frontend into frontend/build
  $ npm install # installs backend packages
  $ npm run start 127.0.0.1:10001 myCluster admin admin #command line arguments for GridDB server creds</code></pre>
</div>

## <span id="set-up"> Setting Up Project Code</span>

### <span id="getting-started"> Getting Started </span>

To get started, we need to connect to the GridDB instance in the same way as handled above in the ingest section. Of course, to host the frontend portion of our app, we will need to set up a frontend server which hosts our React frontend app. We will also need to set up some endpoints within the node.js code (`app.js`). To help us to easily set up the endpoints, we will install the often-used `express` web framework.

### <span id="querying"> How to Query with GridDB's node.js Connector </span>

When querying a container, the results will be in the form of promises, which either resolve or reject once they are finished running. So, to properly run this code, you need to either utilize [promise chaining][9], or you can opt to use the newer form of handling promises: [JavaScript Async functions][10].

This is what querying a container looks like

<div class="clipboard">
  <pre><code class="language-javascript">containerName = 'Cereal';

const queryCont = async (queryStr) => {

    var data = []
    try {
        const col = await store.getContainer(containerName)
        const query = await col.query(queryStr)
        const rs = await query.fetch(query)
        while(rs.hasNext()) {
            data.push(rs.next())
        }
        return data
    } catch (error) {
        console.log("error: ", error)
    }
}</code></pre>
</div>

We call the above code in our endpoint of `/all` which will run the query of `select *`, meaning we fetch all available data. So, whenever we fetch from `/all`, we run the `queryCont` function and return the results back to the agent creating the REQUEST, usually the browser and our React frontend.

<div class="clipboard">
  <pre><code class="language-javascript">app.get('/all', async (req, res) => {
    try {
        let queryStr = "select *"
        var results = await queryCont(queryStr)
        res.json({
            results
        });
    } catch (error) {
        console.log("try error: ", error)
    }
});</code></pre>
</div>

A couple of things to notice here: 1, we use the express framework to set our endpoint; if we run an HTTP GET Request on the endpoint of `/all`, it will run this code and respond with a json file. And 2, when we call our `queryCont` function, we must call it with the `await` keyword as it returns a promise, not just a static variable.

With React, we will run this query on page load to grab all the data with an HTTP GET request. This is done simply for demo purposes as an easy way to populate our dropdown menu. In production code, you would not run this.

### <span id="react"> Using React with GridDB </span>

Now that we have the basics of our backend, we can set up our frontend. When building a new React app, it's usually easiest to use the integrating bundling tools maintained by Facebook/Meta. To shortcut your way to a ready-made project, you can simply run: 

<div class="clipboard">
  <pre><code class="language-sh">$ npx create-react-app frontend</code></pre>
</div>

As mentioned [earlier](#architecture), it is wise to add in a line about which proxy to use in the frontend's `package.json` file. This will allow you to use endpoints that point directly to the address and not need to indicate IP address or port, etc. 

To make edits to the code, we simply edit the `src/App.js` file. For this project, I simply stuck in all of our frontend code inside that file.

The `App.js` file is essentially just one JavaScript function which then gets exported (at the bottom of the file). This function returns `JSX` which builds out app; in this case it will produce all of the elements we see in our frontend app.

And with that, we can get on to writing our actual React code.

### <span id="query-builder"> Creating a Query Builder with React </span>

The full code for this project is available on [Github](https://github.com/griddbnet/Blogs/tree/query_builder), so you can see in detail how this portion was created -- we will not be delving into too much into detail from here. But the basic idea of it is that there are three separate dropdown menus for the user to select various options from. The user can select from a variety of different nutrients, and then pick greater than, less than, or equals, and then finally they can pick a specific cereal to run the query against.

So for example: say you would like to find out which cereals have more fiber than Frosted Mini-Wheats, you would simply select, Fiber, then Greater Than, and finally the cereal name. The query will then be sent to the nodejs server, run the query, and then sent back up to the React code via an endpoint.

[<img src="https://griddb.net/en/wp-content/uploads/2022/05/Screen-Shot-2022-05-17-at-11.14.46-AM.png" class="aligncenter size-full wp-image-28265" />][11]


Sending the data back to the node.js server is done via an HTTP POST request. With this request, we can send back a payload of data, in this case JSON, to which we will use to build out our GridDB SQL query.

Once the user sets their parameters, they will click the submit button, which will fire off our HTTP Request:

<div class="clipboard">
  <pre><code class="language-javascript"> const handleSubmit = async (event) => {
            fetch('/query', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({'list': list, 'comp': comp, 'name': nameDropDown})
            }).then(function(response) {
                console.log(response)
                return response.json();
            });</code></pre>
</div>

The body contains the user-chosen parameters of the dropdown list. On the backend/node.js side, we will grab these values and form our query.

<div class="clipboard">
  <pre><code class="language-javascript">var userResult = {"null": "null"}
var userVal;

app.post('/query', jsonParser, async (req, res) => {
    const {list, comp, name} = req.body 

    try {
        var results = await querySpecific(name)
        let type = checkType(list) //grabs array position of proper value
        let compVal = checkComp(comp)
        let val = results[0][type] // the specific value being queried against
  
        userVal = val
        let specificRes = await queryVal(list, compVal, val)
        userResult = specificRes
        res.status(200).json(userResult);
    } catch (error) {
        console.log("try error: ", error)
    }
});</code></pre>
</div>

So, once a user sets their parameters, the REQUEST comes back to this endpoint. First, we grab the proper values: `list`, `comp`, and `name`. Then, we use our `querySpecific` function which will form one of our queries based on the chosen cereal name.

<div class="clipboard">
  <pre><code class="language-javascript">const querySpecific = async (cerealName) => {

    var data = []
    let q = `SELECT * WHERE name='${cerealName}'`
    try {
        const col = await store.getContainer(containerName)
        const query = await col.query(q)
        const rs = await query.fetch(query)
        while(rs.hasNext()) {
            data.push(rs.next())
        }
        return data
    } catch (error) {
        console.log("error: ", error)
    }
}</code></pre>
</div>

So, if our user wanted to know all of the cereals with a higher fiber content than Frosted Mini-Wheats, we must first grab the entire row where the name is Frosted Mini Wheats. From there, we can grab the array position for which value is the fiber, and then run a second query to find all cereals with a fiber count higher than the value we found.

The `checkType` and `checkComp` functions simply convert the user's selected parameters to the proper array position for both the nutrient type (calories, fiber, etc), as well as the proper sign for equals, or less than, etc.

Now that we have the user's full parameters, as well as the value of the item being compared, we can run our full query with `queryVal`. This function is very similar to the previous two functions but instead takes more parameters to build out a more complex query:

<div class="clipboard">
  <pre><code class="language-javascript">const queryVal = async (list, comp, val) => {

    var data = []
    let q = `SELECT * WHERE ${list} ${comp} ${val} `

    try {
        const col = await store.getContainer(containerName)
        const query = await col.query(q)
        const rs = await query.fetch(query)
        while(rs.hasNext()) {
            data.push(rs.next())
        }
        return data
    } catch (error) {
        console.log("error: ", error)
    }
}</code></pre>
</div>

The body and query were printed out to console and look like this:

<div class="clipboard">
  <pre><code class="language-sh">query val string:  SELECT * WHERE fiber > 3 
req body:  { list: 'fiber', comp: 'Greater Than', name: 'Frosted Mini-Wheats' }</code></pre>
</div>

And now that the data has been retrieved, we can run one more HTTP GET Request to retrieve the rows of our query and then add them into our rudimentary HTML table:

<div class="clipboard">
  <pre><code class="language-javascript">app.get("/data", (req, res) => {
    res.json({
        userVal,
        userResult
    })
});</code></pre>
</div>

And on the frontend:

<div class="clipboard">
  <pre><code class="language-javascript">let response = await fetch(`/data`)
            let result = await response.text()
            let resp = await JSON.parse(result)
            // the Specific react state will set off other functions to form our table rows and columns to be inserted into our table with all the relevant information
            setSpecific(resp)</code></pre>
</div>

And now with the data in our state, we can properly build out our table rows to be inserted into our HTML table to be displayed for the user.

Here is that brief demo again: 

<div style="text-align:center">
<iframe src='https://gfycat.com/ifr/SaneWeightyBlackfish' frameborder='0' scrolling='no' allowfullscreen width='640' height='404'></iframe>
</div>

## <span id="conclusion"> Conclusion </span>

In this blog, we covered quite a lot. We covered how to install the latest GridDB node.js connector (and c_client), how to ingest a CSV file using the node.js connector, how to build a simple express server to serve up your GridDB data, how to use React with GridDB and its bundling tools, and finally, how to run queries on your GridDB server which can be sent up to your React app.

### <span id="source-code"> Source Code </span>

 [1]: https://griddb.net/en/blog/visualize-data-with-griddb-and-the-webapi-using-react-js/
 [2]: https://reactjs.org/
 [3]: https://www.geeksforgeeks.org/mern-stack/
 [4]: https://www.kaggle.com/datasets/crawford/80-cereals
 [5]: https://github.com/griddb/node-api
 [6]: https://griddb.net/en/wp-content/uploads/2022/06/diaghram-1.png
 [7]: https://griddb.net/en/blog/improve-your-devops-with-griddb-server-and-client-docker-containers/
 [8]: https://griddb.net/en/blog/griddb-python-client-adds-new-time-series-functions/
 [9]: https://developer.mozilla.org/en-US/docs/Web/JavaScript/Guide/Using_promises#chaining
 [10]: https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Statements/async_function
 [11]: https://griddb.net/en/wp-content/uploads/2022/05/Screen-Shot-2022-05-17-at-11.14.46-AM.png
 [12]: https://github.com/griddbnet/Blogs/tree/query_builder
