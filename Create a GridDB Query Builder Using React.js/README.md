## Introduction 

This blog serves as a soft follow up to a [previous blog](https://griddb.net/en/blog/visualize-data-with-griddb-and-the-webapi-using-react-js/) in which we used Facebook's React, paired with the GridDB Web API to ingest a CSV file and then vizualize said data. In this follow up, we will again use React, but this time we will be using the GridDB Node.js connector instead of the Web API to ingest and serve data to our frontend.

So, in totality, to showcase both of these products, we will create a simple query builder app to display data that a user wants to see. We will go through the process of installing the GridDB node.js Client via docker, ingesting our open source data from [Kaggle](https://www.kaggle.com/datasets/crawford/80-cereals), and then setting up React to work with a node.js instance connected to GridDB. 

From there, we can move on to the frontend which will be simple: three dropdowns to allow a user to find some data points relating to the dataset which was ingested. 

One small caveat regarding using React with this app: we cannot use the typical React bundle tooling as our node.js server needs a direct connection to the GriddB server. Instead we will be installing React via some file imports which is very simple, but different.

## Installing the GridDB node.js Connector

To install the node.js connector, you can use Docker and connect to your local running GridDB Server. Alternatively, you can follow along with the Dockerfile's file instructions and run the same comands in sequence. Here is the file and all of its contents:

```bash
FROM centos:7

RUN yum -y groupinstall "Development Tools"
RUN yum -y install epel-release wget
RUN yum -y install pcre2-devel.x86_64
RUN yum -y install openssl-devel libffi-devel bzip2-devel -y
RUN yum -y install libffi-devel bzip2-devel nodejs-devel -y
RUN yum -y install xz-devel  perl-core zlib-devel -y

RUN curl -sL https://rpm.nodesource.com/setup_12.x | bash -
RUN yum install nodejs -y

# Make c_client
WORKDIR /
RUN wget --no-check-certificate https://github.com/griddb/c_client/archive/refs/tags/v4.6.0.tar.gz
RUN tar -xzvf v4.6.0.tar.gz
WORKDIR /c_client-4.6.0/client/c
RUN  ./bootstrap.sh
RUN ./configure
RUN make
WORKDIR /c_client-4.6.0/bin
ENV LIBRARY_PATH ${LIBRARY_PATH}:/c_client-4.6.0/bin
ENV LD_LIBRARY_PATH ${LD_LIBRARY_PATH}:/c_client-4.6.0/bin

# Make Swig
WORKDIR /
RUN git clone https://github.com/swig/swig.git
WORKDIR /swig
RUN git checkout 113d78a083aa897ffdca4ff6bd9b42e630d16d27
RUN chmod +x autogen.sh
RUN ./autogen.sh
RUN ./configure
RUN make 
RUN make install
WORKDIR /

# Download and Make the Node.js Client
RUN wget https://github.com/griddb/nodejs_client/archive/refs/tags/0.8.6.tar.gz
RUN tar xvf 0.8.6.tar.gz
WORKDIR /nodejs_client-0.8.6
RUN npm install nan
RUN make
ENV NODE_PATH /nodejs_client-0.8.6

WORKDIR /app

COPY app.js /app
COPY ingest.js /app
COPY cereal.csv /app
RUN mkdir /app/public
COPY public /app/public
COPY package.json /app
COPY package-lock.json /app
RUN npm install


ENTRYPOINT ["npm", "run", "start", "239.0.0.1",  "31999", "defaultCluster", "admin", "admin"]

#ENTRYPOINT ["node", "ingest.js", "239.0.0.1",  "31999", "defaultCluster", "admin", "admin"]
```

As you can see here, we have both the ingest as well as the running of the node.js code in here by moving around which line is currently commented out.

You can also pull the project from the public Dockerhub repo like so: 

`docker pull griddbnet/nodejs-client:latest`

Though if you go this route, you will need to ingest the Cereal data through some other means.

Anyway, when using this container, you can either run a second container which will host a GridDB Server, or you can use your locally running GridDB instance. This can be accomplished by using the network flag while running your docker image:

`docker run -it --network host --name node_client <image id>`

## Ingesting CSV Data with Node.js

To start, let's create a simple node.js script to ingest our data from the `csv` file provided by Kaggle. If you are familiar at all with the [Python GridDB Client](https://griddb.net/en/blog/griddb-python-client-adds-new-time-series-functions/), the node.js iteration will be very familiar. 

First, you must import the griddb library, along with the csv parser. To install the parser, simply use `npm`.

`npm install --save csv-parse`

Next up is setting the credentials with the griddb factory get store.

```javascript
const griddb = require('griddb_node');
var { parse } = require('csv-parse');

var fs = require('fs');
var factory = griddb.StoreFactory.getInstance();
var store = factory.getStore({
    "host": process.argv[2],
    "port": parseInt(process.argv[3]),
    "clusterName": process.argv[4],
    "username": process.argv[5],
    "password": process.argv[6]
});
```
The schema of the data to be imported is set up as a javascript variable

```javascript
var conInfo = new griddb.ContainerInfo({
          'name': 'Cereal',
          'columnInfoList': [
                        ["name", griddb.GS_TYPE_STRING],
                        ["mfr", griddb.GS_TYPE_STRING],
                        ["type", griddb.GS_TYPE_STRING],
                        ["calories", griddb.GS_TYPE_INTEGER],
                        ["protein", griddb.GS_TYPE_INTEGER],
                        ["fat", griddb.GS_TYPE_INTEGER],
                        ["sodium", griddb.GS_TYPE_INTEGER],
                        ["fiber", griddb.GS_TYPE_FLOAT],
                        ["carbo", griddb.GS_TYPE_FLOAT],
                        ["sugars", griddb.GS_TYPE_INTEGER],
                        ["potass", griddb.GS_TYPE_INTEGER],
                        ["vitamins", griddb.GS_TYPE_INTEGER],
                        ["shelf", griddb.GS_TYPE_INTEGER],
                        ["weight", griddb.GS_TYPE_FLOAT],
                        ["cups", griddb.GS_TYPE_FLOAT],
                        ["rating", griddb.GS_TYPE_FLOAT]
            ],
     'type': griddb.ContainerType.COLLECTION,
    'rowKey': false
});
```
Once the proposed schema is set up, we simply read the `csv` file from using the native node.js file system reader and then loop through the contents of each line and grab the proper values to be inserted into our database

```javascript
fs.createReadStream(__dirname+'/cereal.csv')
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
                    for (var i = 0; i < err.getErrorStackSize(); i++) {
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
  });
```

Once you run this, the entirety of the data should be available in your instance.

## Building Our App

### Getting Started

To get started, you connect to your GridDB instance the same was as handled above in the ingest section. Of course, to host the frontend portion of our app, we will need to include html files, as well as some node.js code to host our endpoints. To host help us host our frontend, as well as host static HTML files, we will use the often-used `express` web framework. Loading up static files can be done like so: 

`app.use(express.static(join(__dirname, 'public')));`

### Querying

When querying a container, the results will be in the form of promises, which either resolve or reject once they are finished running. So, to properly run this code, you need to either utilize [promise chaining](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Guide/Using_promises#chaining), or the newer form of [JavaScript Async functions](https://developer.mozilla.org/en-US/docs/Web/JavaScript/Reference/Statements/async_function).  

This is what querying a container looks like

```javascript
containerName = 'Cereal';

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
}
```

We call the above code in our endpoint of `/all` which will run the query of `select *`, meaning we fetch all available data.

```javascript
app.get('/all', async (req, res) => {
    try {
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

A couple of things to notice here: 1, we use the express framework to set our endpoint; if we run an HTTP GET Request on the endpoint of `/all`, it will run this code and respond with a json file. And 2, when we call our `queryCont` function, we must call it with the `await` keyword as it returns a promise, not just a static variable. 

With React, we will run this query on page load to grab all the data with an HTTP GET request. This is done simply for demo purposes as an easy way to populate our dropdown menu. In production code, you would not run this.

### Using React with GridDB

Now that we have the basics of our backend, we can set up a static HTML file in the public directory to display when we run our node.js server. From there, we can test if our endpoint produces our query results. 

To start, we set up an `index.html` file and import React, React DOM, and then Babel. React and the DOM are self-explanatory, babel, however, is needed to use React's `JSX` which makes writing React code much simpler and easier to read.

```html
  <head>
    <meta charset="UTF-8" />
    <title>Query Builder</title>
    <script src="https://unpkg.com/react@18/umd/react.development.js" crossorigin></script>
    <script src="https://unpkg.com/react-dom@18/umd/react-dom.development.js" crossorigin></script>
    <script src="https://unpkg.com/babel-standalone@6/babel.min.js"></script>
  </head>
  ```


With this at the top of the file, now React can be used as normal in the rest of the file. We just need to add the following babel script tags for all of our code which contains `JSX`

`  <script type="text/babel">`

There also needs to be a div made right above our babel script tags to let our React code where to create our React code in relation to the body of the HTML code.

```javacript
<div id="root"></div>
<script type="text/babel">

    const MyApp = () =>  {

    const container = document.getElementById('root');
    const root = ReactDOM.createRoot(container);
    root.render(<MyApp />);
</script> // end of babel script tags
```

And with that, we can get on to writing our React code. 

### Creating a Query Builder with React

The full code for this project is available on Github, so you can see in detail how this portion was created -- we will not being too much into detail from here. But the basic idea of it is that there are three separate dropdown menus for the user to select various options from. The user can select from a variety of different nutrients, and then pick greater than, less than, or equals, and then finally they can pick a specific cereal to run the query against. 

So for example. say you would like to find out which cereals have more fiber than Frosted Mini-Wheats, you would simply select, Fiber, then Greater Than, and finally the cereal name. The query will then be sent to the nodejs server, run the query, and then sent back up to the React code via an endpoint.

![]image

Sending the data back to the node.js server is done via an HTTP POST request. With this request, we can send back a payload of data, in this case JSON, to which we will use to build out our GridDB SQL query.

Once the user sets their parameters, they will click the submit button, which will fire off our HTTP Request:

```javascript
        const handleSubmit = async (event) => {
            fetch('${ADDRESS}/query', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({'list': list, 'comp': comp, 'name': nameDropDown})
            }).then(function(response) {
                console.log(response)
                return response.json();
            });
```
The body contains the user-chosen parameters of the dropdown list. On the backend/node.js side, we will grab these values and form our query.

```javascript
var userResult = {"null": "null"}
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
});
```

So, once a user sets their parameters, the REQUEST comes back to this endpoint. First, we grab the proper values: `list`, `comp`, and `name`. Then, we use our `querySpecific` function which will form one of our queries based on the chosen cereal name. 

```javascript
const querySpecific = async (name) => {

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
}
```

So, if our user wanted to know all of the cereals with a higher fiber content than Frosted Mini-Wheats, we must first grab the entire row where the name is Frosted Mini Wheats. From there, we can grab the array position for which value is the fiber, and then run a second query to find all cereals with a fiber count higher than the value we found.

The `checkType` and `checkComp` functions simply convert the user's selected parameters to the proper array position for both the nutrient type (calories, fiber, etc), as well as the proper sign for equals, or less than, etc.

Now that we have the user's full parameters, as well as the value of the item being compared, we can run our full query with `queryVal`. This function is very similar to the previous two functions but instead takes more parameters to build out a more complex query:

```javascript
const queryVal = async (list, comp, val) => {

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
}
```
The body and query were printed out to console and look like this: 

```bash
query val string:  SELECT * WHERE fiber > 3 
req body:  { list: 'fiber', comp: 'Greater Than', name: 'Frosted Mini-Wheats' }
```

And now that the data has been retrieved, we can run one more HTTP GET Request to retrieve the rows of our query and then add them into our rudimentary HTML table: 

```javascript
app.get("/dats", (req, res) => {
    res.json({
        userVal,
        userResult
    })
});
```

And on the frontend: 

```javascript
            let response = await fetch(`${ADDRESS}/data`)
            let result = await response.text()
            let resp = await JSON.parse(result)
            // the Specific react state will set off other functions to form our table rows and columns to be inserted into our table with all the relevant information
            setSpecific(resp)
```

And now with the data in our state, we can properly build out our table rows to be inserted into our HTML table to be displayed for the user.

## Conclusion

In this blog, we covered quite a lot. We covered how to build the latest GridDB Node.js connector from source, how to install it with Docker, how to ingest a CSV file with node.js, how to build a simple express server to serve up your GridDB data, how to use React with GridDB, and finally, how to run queries on your GridDB server which can be sent back to your React app.


