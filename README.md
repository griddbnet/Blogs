The Rust programming language is a static, compiled language which "emphasizes performance, type safety, and concurrency." It has grown very quickly in the limited time it has been available and consistently ranks as the most loved language in the [annual stackoverflow developer survey](https://insights.stackoverflow.com/survey/2021#section-most-loved-dreaded-and-wanted-programming-scripting-and-markup-languages). 

Rust's growing popularity is exactly why the GridDB development team has written the [GridDB rust client](https://github.com/griddb/rust_client) for interfacing with the database. As with the other [GridDB connectors](https://github.com/griddb), this will allow you to write programs which can directly read and write to your running GridDB server.

For this article, we will discuss installing Rust, the Rust client, and then go over some simple CRUD commands to showcase the basic functionality of this client; this includes actions such as querying your [containers](https://docs.griddb.net/latest/architecture/data-model/#container).

Before we dive into the article, you can follow along with the full source code here: 

<div class="clipboard">
<pre><code class="language-sh">$ git clone --branch blog_1_rust https://github.com/griddbnet/Blogs.git</code></pre>
</div>

Also a quick note: we are using a dataset of which we have been using in the most recent blogs that was taken from Kaggle. You can read more about it in our [PostgreSQL to GridDB Migration blog](https://griddb.net/en/blog/using-the-griddb-import-export-tools-to-migrate-from-postgresql-to-griddb/)

## Getting Started

To get started, you will need to have GridDB up and running either via [direct installation](https://docs.griddb.net/latest/gettingstarted/using-apt/#install-with-deb), or through [Docker](https://griddb.net/en/blog/improve-your-devops-with-griddb-server-and-client-docker-containers/).

You will also need the [GridDB c_client](https://github.com/griddb/c_client) (NOTE: if you installed GridDB via apt or yum, the c_client is already included in your installation). And lastly you will need to install the Rust programming language as well: 

<div class="clipboard">
<pre><code class="language-sh">$ curl https://sh.rustup.rs -sSf | sh</code></pre>
</div>

Another thing you will need is to install the compiler clang. 

<div class="clipboard">
<pre><code class="language-sh"> # Ubuntu
$ sudo apt-get install clang-10 libclang-10-dev </code></pre>
</div>

<div class="clipboard">
<pre><code class="language-sh"> # CentOS
$ sudo yum install llvm-toolset-7.0
$ scl enable llvm-toolset-7.0 bash</code></pre>
</div>

Once you have these prereqs ready on your machine, you can navigate into the `rust_client` directory and run the Rust build command:

<div class="clipboard">
<pre><code class="language-sh">$ cargo build</code></pre>
</div>

This command is a part of the Rust toolchain which will read the `Cargo.toml` file and build out the project for you. From here you can run the sample code included in the official client's repo to see if everything is working as intended. If instead you have cloned the repo for this project, please hang on and we will discuss a bit further before we include how to run the included code.

## Writing Rust Source Code

To begin writing Rust source code to interface with your GridDB server, we will begin with importing the proper GridDB crate and then connecting to our server.

So, to start, let's import the library and then import the functions we aim to use in our code.

And note: we are now working out of the source code included at the top and bottom of this article. 

<div class="clipboard">
<pre><code class="language-rust">use std::time::Duration;
use griddb::get_value;
use griddb::griddb::ContainerInfo::*;
use griddb::griddb::StoreFactory::*;
use griddb::griddb::Type::*;
use griddb::griddb::Value::*;
use griddb::gsvec;
use chrono:: Utc;</code></pre>
</div>

<div class="clipboard">
<pre><code class="language-sh">[package]
name = "griddb_rust_client_blog"
version = "0.1.0"
edition = "2021"</code></pre>
</div>

The imports at the top of each file work similarly to other languages (such as Python). Here, we are calling the [konector_db](https://lib.rs/crates/griddb) library which is where the GridDB Rust client exists. 

As for the package name, you can name it however you like; here we are naming it something generic for this blog.


### Using the GridDB Rust Client

To add the GridDB Rust client into your own repo/project, you will need to add the following to your `Cargo.toml` file.

<div class="clipboard">
<pre><code class="language-sh">[dependencies]
griddb = "0.5.0"
chrono = "0.4"
convert_case = "^0.3.0"</code></pre>
</div>

This simply means that the Rust toolchain will make sure the GridDB rust connector source code gets built and included with our project during compile time when we run `cargo build` or `cargo run`. 

To run this project, you can clone the repository and then run each of the examples similar to the sample code from the official repo 

<div class="clipboard">
<pre><code class="language-sh">$ cargo run --example connect

Successfully Connected to GridDB</code></pre>
</div>

Our server values are hardcoded into the example files in the `examples` directory, so if the command fails, please make sure you have GridDB up and running and change the DB connection details if needed.

### Connecting to GridDB

For the source code in this article we will place ALL code inside our `main` function for ease of use. 

Similar to the other GridDB connectors, we will connect to our database using the factory store and by inputting our connection details: 

<div class="clipboard">
<pre><code class="language-rust">        // get default factory
        let factory = StoreFactory::get_instance();
        let properties = vec![
            ("notification_member", "127.0.0.1:10001"),
            ("cluster_name", "myCluster"),
            ("user", "admin"),
            ("password", "admin"),
        ];</code></pre>
</div>

Differing slightly from the GridDB source code examples, we have hardcoded in our GridDB connection details right inside our code to make running and debugging a smoother experience. 

Once we have the proper connection details, we can establish our connection and get our `gridstore` function

<div class="clipboard">
<pre><code class="language-rust">        // get gridstore function
        let store = match factory.get_store(properties) {
            Ok(result) => result,
            Err(error) => panic!("Error factory get_store() with error code: {:?}", error),
        };</code></pre>
</div>

Here we are using Rust's `match statement`, which works a bit like the classic `switch` statement found in `C` and `JavaScript`. The first arm of the match is evaluated, `get_store` in this case, if all goes well, it returns `Ok` and we return the `result` into `store`; if it fails, the program will throw an error, panic, and then print out the error.

Once that store variable is populated, we are connected to our database and we can start creating containers.

## Create, Read, Update, Delete - CRUD with GridDB

To showcase the Rust Client, we wanted to show the basic functionality of interfacing with a database, SQL or otherwise, through the typical CRUD commands. We will have one big function which will run through and create some containers, drop containers, add rows, delete rows, query via TQL and via the API, and then finally running through a simple aggregation function through TQL.

Before I get into the full source code, let's again take a look at our `Cargo.toml` file. This time I will show the whole file which will showcase all of the dependencies we are using: 

<div class="clipboard">
<pre><code class="language-sh">[package]
name = "griddb_rust_client_blog"
version = "0.1.0"
edition = "2021"

[dependencies]
griddb = "0.5.0"
chrono = "0.4"
convert_case = "^0.3.0"</code></pre>
</div>

### Create (& Delete)

When you are making your `colinfo` variable which will house your GridDB container schema, please be mindful that the data types match up with the Rust ones. For example, a GridDB DOUBLE datatype must be `f64` and so forth. 

To get an idea of what translates into what, you can look at the API documentation: [here](https://griddb.org/rust_client/RustAPIReference.htm), namely the Data-Type Mapping section.

<div class="clipboard">
<pre><code class="language-rust">    // Creating Time Series Container
    let tsinfo = ContainerInfo::ContainerInfo(
        "device13",
        vec![
            ("ts", Type::Timestamp),
            ("co", Type::Double),
            ("humidity", Type::Double),
            ("light", Type::Bool),
            ("lpg", Type::Double),
            ("motion", Type::Bool),
            ("smoke", Type::Double),
            ("temp", Type::Double),
        ],
        ContainerType::TimeSeries,
        true,
    );</code></pre>
</div>

And once the schema and all information is set, we do more of the usual GridDB stuff: `put_container`.

But before we run through actually creating our container (and its schema) inside of our database, we will call `drop_container` on our container first. This ensures that everytime our example source code is run, it is starting from fresh. You will notice that no error is thrown deleting a container that does not exist, so it's similar to the SQL command `DROP TABLE IF EXISTS`.

<div class="clipboard">
<pre><code class="language-rust">
    store.drop_container("device13");

    match store.put_container(&tsinfo, false) {
        Ok(result) => result,
        Err(error) => panic!("Error store put_container() with error code: {:?}", error),
    };</code></pre>
</div>

We can also create a `Collection` container with a similar set up. In the following snippet, we will create a small "device master" type collection container which will sort of mimic a real-world type schema in which you have a sensor container and a sort-of record-keeping container. 

<div class="clipboard">
<pre><code class="language-rust">    // Creating Collection Container
    let colinfo  = ContainerInfo::ContainerInfo(
        "deviceMaster2",
        vec![
            ("sensorID", Type::String),
            ("location", Type::String),
        ],
        ContainerType::Collection,
        true,
    );

    store.drop_container("deviceMaster2");

    let con = match store.put_container(&colinfo, false) {
        Ok(result) => result,
        Err(error) => panic!("Error store put_container() with error code: {:?}", error),
    };
    con.set_auto_commit(false);
    con.create_index("sensorID", IndexType::Default);
    con.commit();
    println!("Successfully created Collection container: deviceMaster2");</code></pre>
</div>

The only difference between the time series container and the collection is that we manually created the index for our rowkey here, but in the time series containers, it is auto-made by definition of the container type.


And again, to run this example code: 

<div class="clipboard">
<pre><code class="language-sh">$ cargo run --example create_containers</code></pre>
</div>

Once you run this, you will now have these two containers created in your running GridDB server. You can verify with the [GridDB CLI](https://github.com/griddb/cli) tool. You can use it like so: 

<div class="clipboard">
<pre><code class="language-sh">$ sudo su gsadm
$ gs_sh
gs> showcontainer device13</code></pre>
</div>

    Database    : public
    Name        : device13
    Type        : TIME_SERIES
    Partition ID: 8
    DataAffinity: -

    Compression Method : NO
    Compression Window : -
    Row Expiration Time: -
    Row Expiration Division Count: -

    Columns:
    No  Name                  Type            CSTR  RowKey   Compression   
    ------------------------------------------------------------------------------
    0  ts                    TIMESTAMP       NN    [RowKey]  
    1  co                    DOUBLE                          
    2  humidity              DOUBLE                          
    3  light                 BOOL                            
    4  lpg                   DOUBLE                          
    5  motion                BOOL                            
    6  smoke                 DOUBLE                          
    7  temp                  DOUBLE 

### Insert Data (Create)

Next, let's try pushing some data into our container. We can accomplish this with a rather simple API call of `.put` like so: 

<div class="clipboard">
<pre><code class="language-rust">        // Grab current time to use as time value for container
        let timestamp: Timestamp = Timestamp {
            value: Utc::now().timestamp_millis(),
        };

        // following the schema laid out in the create_container.rs file
        ts.put(gsvec![timestamp, 0.004342, 49.0, false, 0.00753242, false, 0.0212323, 23.2]);

        let timestamp_second: Timestamp = Timestamp {
            value: Utc::now().timestamp_millis() + 1000,
        };
        ts.put(gsvec![timestamp_second, 0.0065342, 31.0, false, 0.753242, false, 0.02653323, 27.2]);
        // rows aren't pushed until the commit is called
        ts.commit();</code></pre>
</div>

Here we are using the variable of `ts`, which now represents our device container, to put data directly into there, we use the `gsvec` from the GridDB client and create a vector with all of the proper data types that our schema expects. We simply enter in all proper data directly into the container. Here we are placing two separate rows into our container, each with different times as the row key (for the 2nd rowkey, we simply add 1000ms to the original timestamp to guarantee a new row is made).

<div class="clipboard">
<pre><code class="language-sh">$ cargo run --example insert_data</code></pre>
</div>

Again, you can verify that the data was actually inserted: 

<div class="clipboard">
<pre><code class="language-sh">$ sudo su gsadm
$ gs_sh
gs[public]> select * from device13;
2 results. (5 ms)
gs[public]> get</code></pre>
</div>

    ts,co,humidity,light,lpg,motion,smoke,temp
    2022-11-28T21:40:12.250Z,0.004342,49.0,false,0.00753242,false,0.0212323,23.2
    2022-11-28T21:40:13.251Z,0.0065342,31.0,false,0.753242,false,0.02653323,27.2
    The 2 results had been acquired.


### Read (via TQL)

Next, let's try a more complex reading of our data; instead of calling a row directly via rowkey and api, let's instead use the `.query` api call to run a real [TQL](https://docs.griddb.net/tqlreference/introduction/) query. 

First the source code:

<div class="clipboard">
<pre><code class="language-rust">    let query = match ts.query("select *") {
        Ok(result) => result,
        Err(error) => panic!("Error container query data with error code: {:?}", error),
    };
    let row_set = match query.fetch() {
        Ok(result) => result,
        Err(error) => panic!("Error query fetch() data with error code: {:?}", error),
    };

    while row_set.has_next() {
        let row_data = match row_set.next() {
            Ok(result) => result,
            Err(error) => panic!("Error row set next() row with error code: {:?}", error),
        };
        let ts: Timestamp = get_value![row_data[0]];
        let timestamp_number: i64 = ts.value;
        let co: f64 = get_value![row_data[1]];
        let humidity: f64 = get_value![row_data[2]];
        let light: bool = get_value![row_data[3]];
        let lpg: f64 = get_value![row_data[4]];
        let motion: bool = get_value![row_data[5]];
        let smoke: f64 = get_value![row_data[6]];
        let temp: f64 = get_value![row_data[7]];
        let tup_query = (timestamp_number, co, humidity, light, lpg, motion, smoke, temp);
        println!(
            "Device13: 
            ts={0} co={1} humidity={2} light={3} lpg={4} motion={5} smoke={6} temp={7}",
            tup_query.0,
            tup_query.1,
            tup_query.2,
            tup_query.3,
            tup_query.4,
            tup_query.5,
            tup_query.6,
            tup_query.7
        );
    }</code></pre>
</div>

Again here we are using the con variable to make our API calls. Because we already know which container is being targeted, there is no need to indicate which container in our query; we simply select which columns we want with no qualifiers. From there, we take the query variable and run fetch against it to perform our search. Our results are saved inside a row_set. 

Once that row_set is populated, we can loop through each row returned and simply get the value for each column in the row and print out the results. This process is similar to the programming connectors available for GridDB.

Up to this point, you can run this and you'll end up with two rows in your device container. You can query using this program and you can also view the results using the [GridDB CLI](https://github.com/griddb/cli).

<div class="clipboard">
<pre><code class="language-sh">$ cargo run --example read_data</code></pre>
</div>

###  Update  

Next, let's read from our database. Despite only having two rows inside of our container at this point, I want to do a quick lookup for a specific row of data, grab the timestamp (rowkey) and then follow it up with a row update.

<div class="clipboard">
<pre><code class="language-rust">
        let query = match ts.query("select * where temp = 23.2") {
            Ok(result) => result,
            Err(error) => panic!("Error container query data with error code: {:?}", error),
        };
        let row_set = match query.fetch() {
            Ok(result) => result,
            Err(error) => panic!("Error query fetch() data with error code: {:?}", error),
        };

        // Init timestamp to current time
        let mut timestamp: Timestamp = Timestamp {
            value: Utc::now().timestamp_millis(),
        };

        while row_set.has_next() {
            let row_data = match row_set.next() {
                Ok(result) => result,
                Err(error) => panic!("Error row set next() row with error code: {:?}", error),
            };
            timestamp = get_value![row_data[0]];
        }

        ts.put(gsvec![timestamp, 0.214342, 43.32, true, 0.00753242, true, 0.0212323, 23.2]);

        ts.commit();</code></pre>
</div>

There is quite a lot going on here, so let's walk through it. First, we are using `query` again to do a TQL search to look up some data point. We are doing this because we need to grab the rowkey of our row we intend to update.

So in this case, we do a row lookup, if our query is successful, we will iterate through all of the rows that match our lookup query. We will then save the timestamp returned to us by our query into our `timestamp` variable and then use that information to update a row.

So once we have our rowkey, if we use `.put` on a row that already exists, instead of producing an error, it will simply update that row with the new values that we push onto it.  

<div class="clipboard">
<pre><code class="language-sh">$ cargo run --example update_data</code></pre>
</div>

Once this runs, we will update the humidity from our first row which matched our query of finding a row with a temperature of exactly 23.2. So the update will change humidity from 49 to 43.32. We can verify this with our CLI tool or with our `read_data` rust example:

<div class="clipboard">
<pre><code class="language-sh">$ cargo run --example read_data</code></pre>
</div>

    Finished dev [unoptimized + debuginfo] target(s) in 0.08s
    Running `target/debug/examples/read_data`
    Device13:
                ts=1669671612250 co=0.214342 *humidity=43.32* light=true lpg=0.00753242 motion=true smoke=0.0212323 temp=23.2
    Device13:
                ts=1669671613251 co=0.0065342 humidity=31 light=false lpg=0.753242 motion=false smoke=0.02653323 temp=27.2


### Delete

To delete a row, you can easily use the `.remove` API call. Similar to the update call, you will need the rowkey of the row you are targeting. In our case, we will need the precise timestamp of the row we intend to delete. Once we have that information (likely through a query lookup), you can easily delete the row: 

<div class="clipboard">
<pre><code class="language-rust">        while row_set.has_next() {
            let row_data = match row_set.next() {
                Ok(result) => result,
                Err(error) => panic!("Error row set next() row with error code: {:?}", error),
            };
            timestamp = get_value![row_data[0]];
        }

        ts.remove(timestamp);
        ts.commit();</code></pre>
</div>


<div class="clipboard">
<pre><code class="language-sh">$ cargo run --example delete_data</code></pre>
</div>

To check we can use CLI or `read_data`. This time, let's use the CLI: 

<div class="clipboard">
<pre><code class="language-sh">$ sudo su gsadm
$ gs_sh
gs[public]> select * from device13;
1 results. (2 ms)
gs[public]> get</code></pre>
</div>

    ts,co,humidity,light,lpg,motion,smoke,temp
    2022-11-28T21:40:13.251Z,0.0065342,31.0,false,0.753242,false,0.02653323,27.2
    The 1 results had been acquired.

We now only have one row of data inserted inside of our `device13` container.

## Aggregation Queries

Lastly, we'd like to go over a simple aggregation query through the use of TQL. You can find about them here: [TQL documentation](https://docs.griddb.net/tqlreference/tql-syntax-and-calculation-functions/#aggregation-operations-general)

For this example, we will simply perform a search for temps over a certain threshold, and then find the average temp in that time span. Because we only have two rows of data in this example, it won't exactly be useful data, but it will illuminate possibilities and how this function works.

For this example, we will go over both `TIME_AVG` and `TIME_SAMPLING`. Both of which are explained in more detail in the link above.

First we will go over the `TIME_AVG`. The way to do it is simply to enter in the column you'd like to get the weight time average for. It will then do the math for every single relevant row in your whole container and spit out a singular value. 

And because our previous working examples of data were very small (~2 rows of data does not make for interesting analysis), we will use one of the containers from this [Kaggle dataset](https://www.kaggle.com/datasets/garystafford/environmental-sensor-data-132k). You can read about ingesting this dataset from our previous [blog]().

In this case, we will simply use `device1`.

 <div class="clipboard">
<pre><code class="language-rust">    let ts = match store.get_container("device1") {
        Ok(result) => result,
        Err(error) => panic!("Error store put_container() with error code: {:?}", error),
    };

 // Aggregation Time Series Time Avg: https://www.toshiba-sol.co.jp/en/pro/griddb/docs-en/v4_0_3/GridDB_API_Reference.html#sec-3-3-3
    let average_query = format!("select TIME_AVG(humidity)");

    let agg_query = match ts.query(&average_query) {
        Ok(result) => result,
        Err(error) => panic!(
            "Error container query aggregation data with error code: {}",
            error
        ),
    };
    let agg_result = match agg_query.fetch() {
        Ok(result) => result,
        Err(error) => panic!(
            "Error query fetch() aggregation data with error code: {}",
            error
        ),
    };
    let agg_data = match agg_result.next_aggregation() {
        Ok(result) => result,
        Err(error) => panic!(
            "Error row set next() aggregation row with error code: {}",
            error
        ),
    };
    println!(" humidity time avg = {:}",agg_data.get_as_f64().1);</code></pre>
</div>

The above source code looks long but it's no different than what we've already been doing. It simply takes in our TQL query and then prints out the result. First we fetch the results, and then use the `next_aggregation` API call to get our value and print it out into the console.

Next we will try to use the `TIME_SAMPLE` function. 

<div class="clipboard">
<pre><code class="language-rust">    // TIME SAMPLE from Time Series Aggregation functionality using TQL
    let time_sample = format!("select TIME_SAMPLING(humidity, TIMESTAMP('2020-07-18T11:22:33.444Z'), TIMESTAMP('2020-07-22T11:22:33.444Z'), 1, HOUR)");
    println!("Running Query: {}",  time_sample);
    let agg_query_two = match ts.query(&time_sample) {
        Ok(result) => result,
        Err(error) => panic!(
            "Error container query aggregation data with error code: {}",
            error
        ),
    };
    let agg_result_two = match agg_query_two.fetch() {
        Ok(result) => result,
        Err(error) => panic!(
            "Error query fetch() aggregation data with error code: {}",
            error
        ),
    };

    while agg_result_two.has_next() {
        let row_data = match agg_result_two.next() {
            Ok(result) => result,
            Err(error) => panic!("Error row set next() row with error code: {:?}", error),
        };
        let ts: Timestamp = get_value![row_data[0]];
        let timestamp_number: i64 = ts.value;
        let co: f64 = get_value![row_data[1]];
        let humidity: f64 = get_value![row_data[2]];
        let light: bool = get_value![row_data[3]];
        let lpg: f64 = get_value![row_data[4]];
        let motion: bool = get_value![row_data[5]];
        let smoke: f64 = get_value![row_data[6]];
        let temp: f64 = get_value![row_data[7]];
        let tup_query = (timestamp_number, co, humidity, light, lpg, motion, smoke, temp);
        println!(
            "Device13: 
            ts={0} co={1} humidity={2} light={3} lpg={4} motion={5} smoke={6} temp={7}",
            tup_query.0,
            tup_query.1,
            tup_query.2,
            tup_query.3,
            tup_query.4,
            tup_query.5,
            tup_query.6,
            tup_query.7
        );
    }</code></pre>
</div>

Here we are essentially running the same code as our `read_data` function, just with a more interesting and complex query. We are looking up the `TIME_SAMPLING` in our `device1` dataset. This actual query string looks like this: `let time_sample = format!("select TIME_SAMPLING(humidity, TIMESTAMP('2020-07-18T11:22:33.444Z'), TIMESTAMP('2020-07-22T11:22:33.444Z'), 1, HOUR)");`. We are telling our program to give us a sampling of the dataset between the explicit values we are giving in the query, in this case four days of data, in intervals of one hour.

And then we are iterating through the returned results and printing out the results to the console. 

<div class="clipboard">
<pre><code class="language-sh">$ cargo run --example timeseries_aggregation</code></pre>
</div>

Here is what the result looks like (though this is not the entirety of the rows): 

    humidity time avg = 50.81422729710963
    Running Query: select TIME_SAMPLING(humidity, TIMESTAMP('2020-07-18T11:22:33.444Z'), TIMESTAMP('2020-07-22T11:22:33.444Z'), 1, HOUR)
    Device13:
                ts=1595071353444 co=0.0057382469167573434 humidity=52.29999923706055 light=false lpg=0.008506583886712459 motion=false smoke=0.022858971137213444 temp=22
    Device13:
                ts=1595074953444 co=0.0056719601223669276 humidity=52.70000076293945 light=false lpg=0.008435383766205812 motion=false smoke=0.022654654779928257 temp=22
    Device13:
                ts=1595078553444 co=0.0056825985719608325 humidity=53.5235710144043 light=false lpg=0.008446826202354823 motion=false smoke=0.022687482170072902 temp=22.3
    Device13:
                ts=1595082153444 co=0.005747325011217979 humidity=51.92147445678711 light=false lpg=0.008516317111357271 motion=false smoke=0.022886910748059562 temp=21.7
    Device13:
                ts=1595085753444 co=0.0062171975352361755 humidity=54.908409118652344 light=false lpg=0.009014482964305905 motion=false smoke=0.024319772878645333 temp=21.8
    Device13:
                ts=1595089353444 co=0.006172104950567534 humidity=51.70000076293945 light=false lpg=0.008967138511929578 motion=false smoke=0.02418336009283044 temp=21.3

And just for fun, let's take a look at these queries inside of our GridDB CLI: 

<div class="clipboard">
<pre><code class="language-sh">$ sudo su gsadm
$ gs_sh
gs[public]> tql device1 select TIME_AVG(humidity);
1 results. (2 ms)
gs[public]> get</code></pre>
</div>
    Result
    50.81422729710963
    The 1 results had been acquired.

<div class="clipboard">
<pre><code class="language-sh">gs[public]> tql device1 select TIME_SAMPLING(humidity, TIMESTAMP('2020-07-18T11:22:33.444Z'), TIMESTAMP('2020-07-22T11:22:33.444Z'), 1, HOUR);
37 results. (0 ms)
gs[public]> get</code></pre>
</div>
    ts,co,humidity,light,lpg,motion,smoke,temp
    2020-07-18T11:22:33.444Z,0.0030488793379940217,77.0,false,0.005383691572564569,false,0.014022828989540865,19.799999237060547
    2020-07-18T12:22:33.444Z,0.0029050147565559603,76.65528106689453,false,0.005198697479294309,false,0.013508733329556249,19.799999237060547
    2020-07-18T13:22:33.444Z,0.002872341154862943,76.61731719970703,false,0.005156332935627952,false,0.013391176782176004,19.799999237060547
    2020-07-18T14:22:33.444Z,0.002612589347788125,75.37261199951172,false,0.004814621044662395,false,0.012445419108693902,19.5
    2020-07-18T15:22:33.444Z,0.003655886924967606,75.85926818847656,false,0.006139350359130843,false,0.016134931599636852,19.299999237060547
    2020-07-18T16:22:33.444Z,0.003655886924967606,74.72727966308594,false,0.006139350359130843,false,0.016134931599636852,19.0


## Conclusion

And with that, we have installed and explored using the brand new GridDB rust client. If you are interested in learning more, I recommend looking at the other example code in the official repository and creating your own applications.

Full source can be found here: [GitHub](https://github.com/griddbnet/Blogs/tree/blog_1_rust)