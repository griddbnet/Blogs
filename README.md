This blog serves as a soft follow up to a [previous blog][1] in which we used [Facebook's React][2], paired with the GridDB Web API, to ingest a CSV file and then visualize said data. In this follow up, we will again use React, but this time we will be using the GridDB Node.js connector instead of the Web API to ingest and serve data to our frontend. You can think of this configuration as the GERN stack: GridDB, Express, React, and node.js; this is an obvious play on the hugely popular [ MERN stack ][3]

The main and obvious difference between the MERN stack and the GERN Stack is of course the database being deployed. The original acronym stands for MongoDB, Express.js, React.js, and node.js. If you are unfamiliar with MongoDB, it is a document-based database which allows for quick prototyping as the NoSQL schema allows for very fluid ingestion of data without any sort of foresight or pre-planning. Using the GERN stack over the MERN stack depends solely on your use case: if you need much higher performance, if you need to store time series data, or if your data is from IoT sensors, you would opt to use GridDB over MongoDB.

<iframe src='https://gfycat.com/ifr/SaneWeightyBlackfish' frameborder='0' scrolling='no' allowfullscreen width='640' height='404'></iframe>

## Project Overview

To showcase both of these products, we will create a simple query builder app that will display data that a user selects via dropdown menus. We will go through the process of installing the GridDB node.js Client via docker, ingesting our open source data from [Kaggle][4], and then setting up React to work with a node.js server connected to GridDB.

From there, we can move on to the frontend which will be simple: three dropdowns to allow a user to find some data points relating to the dataset which was ingested.

One small caveat regarding using React with this app: we cannot use the typical React bundle tooling. This is because our node.js server needs a direct connection to the GriddB server. Instead we will be installing and using React via script import tags in an HTML file -- truly old school!

[Full Source Code Found Here](https://github.com/griddbnet/Blogs/tree/main/Create%20a%20GridDB%20Query%20Builder%20Using%20React.js)


## Prequisites

The following prequisites are required to run this project: 

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
              <a href="#architecture ">Architecture </a>
            </li>
            <li>
              <a href="#installing-npm"> Installing the GridDB node.js Connector (via NPM) </a>
            </li>
            <li>
              <a href="#building "> Building and Running </a>
            </li>
          </ul>
        </div>
      </li>
      
      <li>
        <a href="#implementation">Implementation</a>
      </li>
      <li style="list-style: outside none none !important;">
        <div class="inner-list">
          <ul style="list-style-type: none !important; padding-left: 9px;">
            <li>
              <a href="#ingest">Ingesting CSV Data with Node.js</a>
            </li>
            <li>
              <a href="#aggregation-tql">Aggregation Queries</a>
            </li>
          </ul>
        </div>
      </li>
      
      <li>
        <a href="#set-up">Setting Up Backend and Frontend </a>
      </li>
      <li style="list-style: outside none none !important;">
        <div class="inner-list">
          <ul style="list-style-type: none !important; padding-left: 9px;">
            <li>
              <a href="#getting-started">Getting Started</a>
            </li>
            <li>
              <a href="#querying"> Querying </a>
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

### <span id="architecture"> Architecture </span>

As explained above, the technologies being used here fit nicely into the acronym GERN: GridDB, Express, React, node.js. My personal environment was as follows: a CentOS 7 server running GridDB on bare metal. For the web app's backend, I ran the [GridDB node.js connector][5] along with the node.js server and the express.js framework in a docker container. The frontend consists of React.js being ran via a self-contained HTML file that gets read in through the node.js frontend.

[<img src="https://griddb.net/en/wp-content/uploads/2022/06/diaghram-1.png" alt="" width="1280" height="720" class="aligncenter size-full wp-image-28479" />][6]

### <span id="installing-npm"> Installing the GridDB node.js Connector (via npm) </span>

To install the node.js connector, you will first need to install the GridDB c-client. To do so, you can grab appropriate package files from the [GitHub page](https://github.com/griddb/c_client/releases). 

On CentOS you can still like so: 

<div class="clipboard">
  <pre><code class="language-sh">$ sudo rpm -ivh griddb-c-client-5.0.0-linux.x86_64.rpm</code></pre>
</div>

Now with the GridDB c-client installed, you can simply grab the [nodejs package](https://www.npmjs.com/package/griddb-node-api) using npm

<div class="clipboard">
  <pre><code class="language-sh">$ npm i griddb-node-api</code></pre>
</div>

And everything should run now. You can now run the ingest to ingest the `cereal.csv` file and then run project itself.

<div class="clipboard">
  <pre><code class="language-sh">$ node ingest.js 239.0.0.1 31999 defaultCluster admin admin</code></pre>
</div>

<div class="clipboard">
  <pre><code class="language-sh">$ npm run start 239.0.0.1 31999 defaultCluster admin admin</code></pre>
</div>



### <span id="building"> Building and Running </span>

Before we get into the details of how the project was made, let's briefly explain how exactly to run this on your local machine. There are two ways to do this, you can install GridDB normally on your machine and run it as a service as I have, or you can run GridDB in a container as shown in this [ blog ][7].

To run the project on bare metal without the use of Docker, you can simply just use npm script.

Of course, you will need to enter in your own credentials along with the run command; these are all GridDB default values.

<div class="clipboard">
  <pre><code class="language-sh">$ npm run start 239.0.0.1 31999 defaultCluster admin admin</code></pre>
</div>

## <span id="implementation"> Implementation <span></span></span>

### <span id="ingest"> Ingesting CSV Data with Node.js </span>

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
                        ["name", griddb.Type.INTEGER],
                        ["mfr", griddb.Type.INTEGER],
                        ["type", griddb.Type.INTEGER],
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
  <pre><code class="language-javascript">fs.createReadStream(__dirname+'/cereal.csv')
  .pipe(parse({columns: true}))
  .on('data', (row) => {
    var col2;
    store.putContainer(conInfo, false)
       .then(col => {
           col2 = col;
           return col;
        })
       .then(col => {
            setTimeout(() => {  console.log("Row Parsed and Put!"); }, 1000);

           col.put([
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
           return col;
       })
            .catch(err => {
                if (err.constructor.name == "GSException") {
                    for (var i = 0; i &lt; err.getErrorStackSize(); i++) {
                        console.log("[", i, "]");
                        console.log(err.getErrorCode(i));
                        console.log(err.getMessage(i));
                    }
                } else {
                    console.log(err);
                }
            });
  })
  .on('end', () => {
    console.log('CSV file successfully processed');
  });</code></pre>
</div>

Once you run this, the entirety of the data should be available in your instance.

## <span id="set-up"> Setting Up Backend and Frontend </span>

### <span id="getting-started"> Getting Started </span>

To get started, we need to connect to the GridDB instance the same was as handled above in the ingest section. Of course, to host the frontend portion of our app, we will need to include html files loaded up with React imports. We will also need to set up some endpoints within the node.js code (`app.js`). To help us to easily set up the endpoints, as well as to help hosting the static HTML files, we will install the often-used `express` web framework.

Loading up static files can be done like so:

`app.use(express.static(join(__dirname, 'public')));`

### <span id="querying"> Querying </span>

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

We call the above code in our endpoint of `/all` which will run the query of `select *`, meaning we fetch all available data.

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

Now that we have the basics of our backend, we can set up a static HTML file in the `public` directory which will display when we run our node.js server. From there, we can test if our endpoint produces our query results.

To start, we set up an `index.html` file and import React, React DOM, and then Babel. React and the DOM are self-explanatory; babel, however, is needed to use React's `JSX` which makes writing React code much simpler and easier to read.

<div class="clipboard">
  <pre><code class="language-html">&lt;head&gt;
&lt;meta charset="UTF-8" /&gt;
&lt;title&gt;Query Builder&lt;/title&gt;
&lt;script src="https://unpkg.com/react@18/umd/react.development.js" crossorigin&gt;&lt;/script&gt;
&lt;script src="https://unpkg.com/react-dom@18/umd/react-dom.development.js" crossorigin&gt;&lt;/script&gt;
&lt;script src="https://unpkg.com/babel-standalone@6/babel.min.js"&gt;&lt;/script&gt;
&lt;/head&gt;</code></pre>
</div>

With this at the top of the file, now React can be used as normal in the rest of the file. We just need to add the following babel script tags for all of our code which contains `JSX`

`<script type="text/babel">`

There also needs to be a div made right above our babel script tags to let our React code where to create our React code in relation to the body of the HTML code. JavaScript

<div class="clipboard">
  <pre><code class="language-javascript">&lt;div id="root"&gt;
&lt;/div&gt;

&lt;script type="text/babel"&gt;

    const MyApp = () =&gt;  {

    const container = document.getElementById('root');
    const root = ReactDOM.createRoot(container);
    root.render(&lt;myapp&gt;&lt;/myapp&gt;);
&lt;/script&gt; 
// end of babel script tags</code></pre>
</div>

And with that, we can get on to writing our React code.

### <span id="query-builder"> Creating a Query Builder with React </span>

The full code for this project is available on Github, so you can see in detail how this portion was created -- we will not being too much into detail from here. But the basic idea of it is that there are three separate dropdown menus for the user to select various options from. The user can select from a variety of different nutrients, and then pick greater than, less than, or equals, and then finally they can pick a specific cereal to run the query against.

So for example. say you would like to find out which cereals have more fiber than Frosted Mini-Wheats, you would simply select, Fiber, then Greater Than, and finally the cereal name. The query will then be sent to the nodejs server, run the query, and then sent back up to the React code via an endpoint.

[<img src="https://griddb.net/en/wp-content/uploads/2022/05/Screen-Shot-2022-05-17-at-11.14.46-AM.png" alt="" width="2840" height="844" class="aligncenter size-full wp-image-28265" />][11]

Sending the data back to the node.js server is done via an HTTP POST request. With this request, we can send back a payload of data, in this case JSON, to which we will use to build out our GridDB SQL query.

Once the user sets their parameters, they will click the submit button, which will fire off our HTTP Request:

<div class="clipboard">
  <pre><code class="language-javascript"> const handleSubmit = async (event) => {
            fetch('${ADDRESS}/query', {
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
  <pre><code class="language-javascript">const querySpecific = async (name) => {

    var data = []
    let q = `SELECT * WHERE name='${name}'`
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
  <pre><code class="language-javascript">let response = await fetch(`${ADDRESS}/data`)
            let result = await response.text()
            let resp = await JSON.parse(result)
            // the Specific react state will set off other functions to form our table rows and columns to be inserted into our table with all the relevant information
            setSpecific(resp)</code></pre>
</div>

And now with the data in our state, we can properly build out our table rows to be inserted into our HTML table to be displayed for the user.

## <span id="conclusion"> Conclusion </span>

In this blog, we covered quite a lot. We covered how to build the latest GridDB Node.js connector from source, how to install it with Docker, how to ingest a CSV file with node.js, how to build a simple express server to serve up your GridDB data, how to use React with GridDB, and finally, how to run queries on your GridDB server which can be sent back to your React app.

### <span id="source-code"> Source Code </span>

[ Full source code found here ][12]

 [1]: https://griddb.net/en/blog/visualize-data-with-griddb-and-the-webapi-using-react-js/
 [2]: https://reactjs.org/
 [3]: https://www.geeksforgeeks.org/mern-stack/
 [4]: https://www.kaggle.com/datasets/crawford/80-cereals
 [5]: https://github.com/griddb/nodejs_client
 [6]: https://griddb.net/en/wp-content/uploads/2022/06/diaghram-1.png
 [7]: https://griddb.net/en/blog/improve-your-devops-with-griddb-server-and-client-docker-containers/
 [8]: https://griddb.net/en/blog/griddb-python-client-adds-new-time-series-functions/
 [9]: https://developer.mozilla.org/en-US/docs/Web/JavaScript/Guide/Using_promises#chaining
 [10]: https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Statements/async_function
 [11]: https://griddb.net/en/wp-content/uploads/2022/05/Screen-Shot-2022-05-17-at-11.14.46-AM.png
 [12]: https://github.com/griddbnet/Blogs/tree/main/Create%20a%20GridDB%20Query%20Builder%20Using%20React.js