The Rust programming language is a static, compiled language which "emphasizes performance, type safety, and concurrency." It has grown very quickly in the limited time it has been available and consistently ranks as the most loved language in the [annual stackoverflow developer survey](https://insights.stackoverflow.com/survey/2021#section-most-loved-dreaded-and-wanted-programming-scripting-and-markup-languages). 

Rust's growing popularity is exactly why the GridDB development team has written the [GridDB rust client](https://github.com/griddb/rust_client) for interfacing with the database. As with the other [GridDB connectors](https://github.com/griddb), this will allow you to write programs which can directly read and write to your running GridDB server.

For this article, we will discuss installing Rust, the Rust client, and then go over a simple example of ingesting a `.csv` file using the newly downloaded client. The GitHub repo also contains some sample code which can be ran to see how to do things which aren't included in our working example, such as querying your [containers](https://docs.griddb.net/latest/architecture/data-model/#container)

Before we dive into the article, you can follow along here: 

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

This command is a part of the Rust toolchain which will read the `Cargo.toml` file and build out the project for you. From here you can run the sample code included in the repo to see if everything is working as intended. If instead you have cloned the repo for this project instead, please hang on and we will discuss a bit further before we include how to run the included code.

### Running The Sample Code 

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
#![allow(non_snake_case)]
extern crate griddb_rust_client_blog;

use griddb_rust_client_blog::get_value;
use griddb_rust_client_blog::griddb::ContainerInfo::*;
use griddb_rust_client_blog::griddb::StoreFactory::*;
use griddb_rust_client_blog::griddb::Type::*;
use griddb_rust_client_blog::griddb::Value::*;
use griddb_rust_client_blog::gsvec;
```

The first line of this snippet is grabbing our project name which is included inside our `Cargo.toml` file.

```bash
[package]
name = "griddb_rust_client_blog"
version = "0.1.0"
edition = "2021"
```

### Using the GridDB Rust Client

To add the GridDB Rust client into your own repo/project, you will need to add the following to your `Cargo.toml` file and then copy over the proper directories (griddb-sys and the src from the official repo): 

```bash
[dependencies]
griddb-sys = { version = "5.0.0", path = "griddb-sys" }
```

This simply means that the Rust toolchain will make sure the GridDB rust connector source code gets built and included with our project during compile time when we run `cargo build` or `cargo run`. 

### Connecting to GridDB

Similar to the other GridDB Connectors, we will connect to our database using the factory store and by inputting our connection details: 

```rust
        let factory = StoreFactory::get_instance();
        let args: Vec<_> = env::args().collect();
        let properties = vec![
            ("notification_member", args[1].as_str()),
            ("cluster_name", args[2].as_str()),
            ("user", args[3].as_str()),
            ("password", args[4].as_str()),
        ];
```

As explained before, here we are inputting our connection details as command line arguments when we run our program, similar to all other GridDB example source code. 

Once we have the proper connection details, we can establish our connection and get our `gridstore` function

```rust
        // get gridstore function
        let store = match factory.get_store(properties) {
            Ok(result) => result,
            Err(error) => panic!("Error factory get_store() with error code: {:?}", error),
        };
```

Here we are using Rust's `match statement`, which works a bit like the classic `switch` statement found in `C` and `JavaScript`. The first arm of the match is evaluated, `get_store` in this case, if all goes well, it returns `OK` and we return the `result` into `store`; if it fails, the program will throw an error, panic, and then print out the error.

Once that store variable is populated, we are connected to our database and we can start creating containers.

## Ingesting Data from CSV File

To showcase the Rust Client, we wanted to show a typical usecase, such as ingesting historical data for analysis. In this case, we are ingesting a "fun" dataset from Kaggle which has some easy-to-digest information on American breakfast cereals found here: [Kaggle - 80 Cereals](https://www.kaggle.com/datasets/crawford/80-cereals).

To ingest with Rust, it's not so different than the other programming languages:  we set the schema by creating a `colinfo` variable with all of the fields. To actually parse the CSV, we also create a `Rust struct` which will also tell our program the datatypes for each field.

To parse the csv, we are using a library called [serde](https://serde.rs/) which will handle deserializing our data.

Before I get into how we will use `serde`, let's again take a look at our `Cargo.toml` file. This time I will show the whole file which will showcase all of the dependencies we are using: 

```bash
[package]
name = "griddb_rust_client_blog"
version = "0.1.0"
edition = "2021"


[dependencies]
griddb-sys = { version = "5.0.0", path = "griddb-sys" }
chrono = "0.4"
convert_case = "^0.3.0"
csv = "1.1"
serde = { version = "1", features = ["derive"] }
tuple-conv = "1.0.1"
```

First, here's the struct with the serde traits added above it: 

```rust
use serde::Deserialize;

#[derive(Debug, Deserialize)]
#[serde(rename_all = "PascalCase")]
struct Record {
    Name: String,
    Mfr: String,
    Type: String,
    Calories: i32,
    Protein: i32,
    Fat: i32,
    Sodium: i32,
    Fiber: f64,
    Carbo: f64,
    Sugars: i32,
    Potass: i32,
    Vitamins: i32,
    Shelf: f64,
    Cups: f64,
    Rating: f64,
}
```

A couple of things to notice here: the datatypes are *very* particular. To get an idea of what translates into what, you can look at the API documentation: [here](https://griddb.org/rust_client/RustAPIReference.htm), namely the Data-Type Mapping section.

When you are making your `colinfo` variable which will house your GridDB container schema, please be mindful that the data types match up with the Rust ones. For example, a GridDB DOUBLE datatype must be `f64` and so forth. 

```rust
        let colinfo = ContainerInfo::ContainerInfo(
            "cereal",
            vec![
                ("name", Type::String),
                ("mfr", Type::String),
                ("type", Type::String),
                ("calories", Type::Integer),
                ("protein", Type::Integer),
                ("fat", Type::Integer),
                ("sodium", Type::Integer),
                ("fiber", Type::Double),
                ("carbo", Type::Double),
                ("sugar", Type::Integer),
                ("potass", Type::Integer),
                ("vitamins", Type::Integer),
                ("shelf", Type::Double),
                ("cups", Type::Double),
                ("rating", Type::Double),
            ],
            ContainerType::Collection,
            true,
        );
```

And once the schema and all information is set, we do more of the usual GridDB stuff: `put_container` and setting the auto commit to false and creating the index (`name`)

```rust
        let con = match store.put_container(&colinfo, false) {
            Ok(result) => result,
            Err(error) => panic!("Error store put_container() with error code: {:?}", error),
        };
        con.set_auto_commit(false);
        con.create_index("name", IndexType::Default);
```

### Reading A CSV File

To read the CSV file, we will send the contents of our downloaded file via stdin through the command line. So when we run our program, we will send in the connection details and then pipe in the csv file contents. 

Once we have the contents read in by our program, we will use `serde` to deserialize and then iterate through each record as a datatype of our created `struct` called `Record` and then finally `put` each row into the DB, one by one: 

```rust
        let mut rdr = csv::Reader::from_reader(io::stdin());
        for result in rdr.deserialize() {
            match result {
                Ok(records) => {
                    let record: Record = records;
                    let err = con.put(gsvec![
                        record.Mfr,
                        record.Type,
                        record.Calories,
                        record.Protein,
                        record.Fat,
                        record.Sodium,
                        record.Fiber,
                        record.Carbo,
                        record.Sugars,
                        record.Potass,
                        record.Vitamins,
                        record.Shelf,
                        record.Cups,
                        record.Rating,
                        ]);
                        println!("Error: {}", err);
                        con.commit();
                }

            Err(err) => {
                println!("error reading CSV from <stdin>: {}", err);
                process::exit(1);
            }

        }
    }
```

As stated above, once we parse out the record (row), we call the rust client API to `con.put` that row into our database. Once the program is done we can query the results using the [GridDB CLI](https://github.com/griddb/cli).

If you are following along in your own environment and would like to ingest the cereal data, please download the csv from Kaggle and place it in the same directory as your source code. You can then run it like so: 

```bash
$ cargo build
$  ./target/debug/griddb_rust_client_blog 127.0.0.1:10001 myCluster admin admin < cereal.csv
```

## Conclusion

And with that, we have installed and explored using the brand new GridDB rust client. If you are interested in learning more, I recommend looking at the other example code in the official repository and creating your own applications.

Full source can be found here: [GitHub](https://github.com/griddbnet/Blogs/tree/griddb_rust_client_blog/blogs/1_rust_client)