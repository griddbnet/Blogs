The Rust programming language is a static, compiled language which "emphasizes performance, type safety, and concurrency." It has grown very quickly in the limited time it has been available and consistently ranks as the most loved language in the [annual stackoverflow developer survey](https://insights.stackoverflow.com/survey/2021#section-most-loved-dreaded-and-wanted-programming-scripting-and-markup-languages). 

Rust's growing popularity is exactly why the GridDB development team has written the [GridDB rust client](https://github.com/griddb/rust_client) for interfacing with the database. As with the other [GridDB connectors](https://github.com/griddb), this will allow you to write programs which can directly read and write to your running GridDB server.

For this article, we will discuss installing Rust, the Rust client, and then go over some simple CRUD commands to showcase the basic functionality of this client; this includes actions such as querying your [containers](https://docs.griddb.net/latest/architecture/data-model/#container).

Before we dive into the article, you can follow along with the full source code here: 

```bash
$ git clone --branch griddb_rust_client_blog git@github.com:griddbnet/Blogs.git
```

## Getting Started

To get started, you will need to have GridDB up and running either via [direct installation](https://docs.griddb.net/latest/gettingstarted/using-apt/#install-with-deb), or through [Docker](https://griddb.net/en/blog/improve-your-devops-with-griddb-server-and-client-docker-containers/).

You will also need the [GridDB c_client](https://github.com/griddb/c_client) (NOTE: if you installed GridDB via apt or yum, the c_client is already included in your installation). And lastly you will need to install the Rust programming language as well: 

```bash
$ curl https://sh.rustup.rs -sSf | sh
```

Another thing you will need is to install the compiler clang. 

```bash 
# Ubuntu
$ sudo apt-get install clang-10 libclang-10-dev 
```

```bash 
# CentOS
$ sudo yum install llvm-toolset-7.0
$ scl enable llvm-toolset-7.0 bash
```

Once you have these prereqs ready on your machine, you can navigate into the `rust_client` directory and run the Rust build command:

```bash
$ cargo build
```

This command is a part of the Rust toolchain which will read the `Cargo.toml` file and build out the project for you. From here you can run the sample code included in the official client's repo to see if everything is working as intended. If instead you have cloned the repo for this project, please hang on and we will discuss a bit further before we include how to run the included code.

### Running The Sample Code From the Official Rust Client Repository

Once the project has been built, you can run the sample code by using the Rust toolchain commands. Again, this is assuming you cloned the official github repo and have built the project. If you don't want to clone the official repo and instead and only need to follow along with this article, you can skip this section.

Anyway, if you are running your GridDB server in `fixed_list mode` (the chances say are you are, if you decided to run GridDB as a daemon), you will need to edit the sample source code before running the commands.

For example, please open up `sample1.rs` and change the following: 

```rust
fn main() {
    // get default factory OLD
    let factory = StoreFactory::get_instance();
    let args: Vec<_> = env::args().collect();
    let properties = vec![
        ("notification_address", args[1].as_str()),
        ("notification_port", args[2].as_str()),
        ("cluster_name", args[3].as_str()),
        ("user", args[4].as_str()),
        ("password", args[5].as_str()),
    ];
    
    fn main() {
    // CHANGE TO THIS
    let factory = StoreFactory::get_instance();
    let args: Vec<_> = env::args().collect();
    let properties = vec![
        ("notification_member", args[1].as_str()),
        ("cluster_name", args[2].as_str()),
        ("user", args[3].as_str()),
        ("password", args[4].as_str()),
    ];
```

All we have changed here is removed `notification_port` and changed `notification_address` to `notification_member`.

And now you can run this particular code: 

```bash
$ cargo run --example sample1 127.0.0.1:10001 myCluster admin admin

 --> Person: name=name01 status=false count=100 lob=[ABCDEFGHIJ]
 ```

And now of course, to run the other sample code, if you are running in fixed_list mode, you will need to modify the properties again. You can also try hard-coding the values of your server into the properties vector if you don't like using the command line arguments everytime you wish to run your code.

## Writing Rust Source Code

To begin writing Rust source code to interface with your GridDB server, we will begin with importing the proper GridDB crate and then connecting to our server.

So, to start, let's import the library and then import the functions we aim to use in our code.

And note: we are now working out of the source code included at the top and bottom of this article; we are no longer using the official repo.

```rust
use std::time::Duration;
use konektor_db::get_value;
use konektor_db::griddb::ContainerInfo::*;
use konektor_db::griddb::StoreFactory::*;
use konektor_db::griddb::Type::*;
use konektor_db::griddb::Value::*;
use konektor_db::gsvec;
use chrono:: Utc;
```

```bash
[package]
name = "griddb_rust_client_blog"
version = "0.1.0"
edition = "2021"
```

### Using the GridDB Rust Client

To add the GridDB Rust client into your own repo/project, you will need to add the following to your `Cargo.toml` file.

```bash
[dependencies]
konektor_db = "0.5.0"
chrono = "0.4"
convert_case = "^0.3.0"
```

This simply means that the Rust toolchain will make sure the GridDB rust connector source code gets built and included with our project during compile time when we run `cargo build` or `cargo run`. 

To run this project, you can clone the repository and then run each of the examples similar to the sample code from the official repo 

```bash
$ cargo run --example connect

Successfully Connected to GridDB
```

Our server values are hardcoded into the example files in the `examples` directory, so if the command fails, please make sure you have GridDB up and running and change the DB connection details if needed.

### Connecting to GridDB

For the source code in this article we will place ALL code inside our `main` function for ease of use. 

Similar to the other GridDB connectors, we will connect to our database using the factory store and by inputting our connection details: 

```rust
        // get default factory
        let factory = StoreFactory::get_instance();
        let properties = vec![
            ("notification_member", "127.0.0.1:10001"),
            ("cluster_name", "myCluster"),
            ("user", "admin"),
            ("password", "admin"),
        ];
```

Differing slightly from the GridDB source code examples, we have hardcoded in our GridDB connection details right inside our code to make running and debugging a smoother experience. 

Once we have the proper connection details, we can establish our connection and get our `gridstore` function

```rust
        // get gridstore function
        let store = match factory.get_store(properties) {
            Ok(result) => result,
            Err(error) => panic!("Error factory get_store() with error code: {:?}", error),
        };
```

Here we are using Rust's `match statement`, which works a bit like the classic `switch` statement found in `C` and `JavaScript`. The first arm of the match is evaluated, `get_store` in this case, if all goes well, it returns `Ok` and we return the `result` into `store`; if it fails, the program will throw an error, panic, and then print out the error.

Once that store variable is populated, we are connected to our database and we can start creating containers.

## Create, Read, Update, Delete - CRUD with GridDB

To showcase the Rust Client, we wanted to show the basic functionality of interfacing with a database, SQL or otherwise, through the typical CRUD commands. We will have one big function which will run through and create some containers, drop containers, add rows, delete rows, query via TQL and via the API, and then finally running through a simple aggregation function through TQL.

Before I get into the full source code, let's again take a look at our `Cargo.toml` file. This time I will show the whole file which will showcase all of the dependencies we are using: 

```bash
[package]
name = "griddb_rust_client_blog"
version = "0.1.0"
edition = "2021"

[dependencies]
konektor_db = "0.5.0"
chrono = "0.4"
convert_case = "^0.3.0"
```

### Create (& Delete)

When you are making your `colinfo` variable which will house your GridDB container schema, please be mindful that the data types match up with the Rust ones. For example, a GridDB DOUBLE datatype must be `f64` and so forth. 

To get an idea of what translates into what, you can look at the API documentation: [here](https://griddb.org/rust_client/RustAPIReference.htm), namely the Data-Type Mapping section.

```rust
    // Creating Time Series Container
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
    );
```

And once the schema and all information is set, we do more of the usual GridDB stuff: `put_container`.

But before we run through actually creating our container (and its schema) inside of our database, we will call `drop_container` on our container first. This ensures that everytime our example source code is run, it is starting from fresh. You will notice that no error is thrown deleting a container that does not exist, so it's similar to the SQL command `DROP TABLE IF EXISTS`.

```rust

    store.drop_container("device13");

    match store.put_container(&tsinfo, false) {
        Ok(result) => result,
        Err(error) => panic!("Error store put_container() with error code: {:?}", error),
    };
```

Again, to run this example code: 

```bash
$ cargo run --example create_containers
```

### Insert Data (Create)

Next, let's try pushing some data into our container. We can accomplish this with a rather simple API call of `.put` like so: 

```rust
        // Grab current time to use as time value for container
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
        ts.commit();
```

Here we are using the variable of `ts`, which now represents our device container, to put data directly into there, we use the `gsvec` from the GridDB client and create a vector with all of the proper data types that our schema expects. We simply enter in all proper data directly into the container. Here we are placing two separate rows into our container, each with different times as the row key (for the 2nd rowkey, we simply add 1000ms to the original timestamp to guarantee a new row is made).

```bash
$ cargo run --example insert_data
```

### Read (via TQL)

Next, let's try a more complex reading of our data; instead of calling a row directly via rowkey and api, let's instead use the `.query` api call to run a real [TQL](https://docs.griddb.net/tqlreference/introduction/) query. 

First the source code:

```rust
        let query = match ts.query("select *") {
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
        }

```

Again here we are using the con variable to make our API calls. Because we already know which container is being targeted, there is no need to indicate which container in our query; we simply select which columns we want with no qualifiers. From there, we take the query variable and run fetch against it to perform our search. Our results are saved inside a row_set. Once that row_set is populated, we can loop through each row returned and simply get the value for each column in the row and print out the results. This process is similar to the programming connectors available for GridDB.

Up to this point, you can run this and you'll end up with two rows in your device container. You can query using this program and you can also view the results using the [GridDB CLI](https://github.com/griddb/cli).

```bash
$ cargo run --example read_data
```

###  Update  

Next, let's read from our database. Despite only having two rows inside of our container at this point, I want to do a quick lookup for a specific row of data, grab the timestamp (rowkey) and then follow it up with a row update.

```rust

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

        ts.commit();
```

There is quite a lot going on here, so let's walk through it. First, we are using `query` again to do a TQL search to look up some data point. We are doing this because we need to grab the rowkey of our row we intend to update.

So in this case, we do a row lookup, if our query is successful, we will iterate through all of the rows that match our lookup query. We will then save the timestamp returned to us by our query into our `timestamp` variable and then use that information to update a row.

So once we have our rowkey, if we use `.put` on a row that already exists, instead of producing an error, it will simply update that row with the new values that we push onto it.  

```bash
$ cargo run --example update_data
```

### Delete

To delete a row, you can easily use the `.remove` API call. Similar to the update call, you will need the rowkey of the row you are targeting. In our case, we will need the precise timestamp of the row we intend to delete. Once we have that information (likely through a query lookup), you can easily delete the row: 

```rust
        while row_set.has_next() {
            let row_data = match row_set.next() {
                Ok(result) => result,
                Err(error) => panic!("Error row set next() row with error code: {:?}", error),
            };
            timestamp = get_value![row_data[0]];
        }

        ts.remove(timestamp);
        ts.commit();
```


```bash
$ cargo run --example delete_data
```

And then once your row is deleted, you can feel free to run the Read_Data file again to see if the row disappeared, or you can of course use the [GridDB CLI](https://github.com/griddb/cli)

## Aggregation Queries


Lastly, we'd like to go over a simple aggregation query through the use of TQL. You can find about them here: [TQL documentation](https://docs.griddb.net/tqlreference/tql-syntax-and-calculation-functions/#aggregation-operations-general)

For this example, we will simply perform a search for temps over a certain threshold, and then find the average temp in that time span. Because we only have two rows of data in this example, it won't exactly be useful data, but it will illuminate possibilities and how this function works.

 ```rust
         let agg_query_str = match ts.query("select * from device13 where temp > 24") {
            Ok(result) => result,
            Err(error) => panic!("Error container query data with error code: {:?}", error),
        };
        let agg_row_set = match agg_query_str.fetch() {
            Ok(result) => result,
            Err(error) => panic!("Error query fetch() data with error code: {:?}", error),
        };
        let mut agg_query;
        let mut agg_ts;
        while agg_row_set.has_next() {
            let agg_row = match agg_row_set.next() {
                Ok(result) => result,
                Err(error) => panic!("Error row set next() row with error code: {}", error),
            };
            let timestamp: Timestamp = get_value![agg_row[0]];
            agg_ts = timestamp.value;
            println!("{:?}", agg_ts);
            let average_query = format!("select AVG(temp) from device13 where ts > TIMESTAMPADD(MINUTE, TO_TIMESTAMP_MS({agg_ts}), -10) AND ts < TIMESTAMPADD(MINUTE, TO_TIMESTAMP_MS({agg_ts}), 10)");
            agg_query = match ts.query(&average_query[..]) {
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
            println!(
                "[Timestamp = {:?}] temp = {:.2}",
                agg_ts,
                agg_data.get_as_f64().1
            );
        }
```

The above source code looks long but it's no different than what we've already been doing. It simply runs two queries and then prints out the results of the aggregation function.

```bash
$ cargo run --example timeseries_aggregation
```

## Conclusion

And with that, we have installed and explored using the brand new GridDB rust client. If you are interested in learning more, I recommend looking at the other example code in the official repository and creating your own applications.

Full source can be found here: [GitHub](https://github.com/griddbnet/Blogs/tree/griddb_rust_client_blog/blogs/1_rust_client)