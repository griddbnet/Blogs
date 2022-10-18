#![allow(non_snake_case)]
extern crate griddb_rust_client_blog;

use griddb_rust_client_blog::get_value;
use griddb_rust_client_blog::griddb::ContainerInfo::*;
use griddb_rust_client_blog::griddb::StoreFactory::*;
use griddb_rust_client_blog::griddb::Type::*;
use griddb_rust_client_blog::griddb::Value::*;
use griddb_rust_client_blog::gsvec;
use std::env;
use std::io;
use std::process;

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

fn main() {

        // get default factory
        let factory = StoreFactory::get_instance();
        let args: Vec<_> = env::args().collect();
        let properties = vec![
            ("notification_member", args[1].as_str()),
            ("cluster_name", args[2].as_str()),
            ("user", args[3].as_str()),
            ("password", args[4].as_str()),
        ];
        // get gridstore function
        let store = match factory.get_store(properties) {
            Ok(result) => result,
            Err(error) => panic!("Error factory get_store() with error code: {:?}", error),
        };
    
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
    
        let con = match store.put_container(&colinfo, false) {
            Ok(result) => result,
            Err(error) => panic!("Error store put_container() with error code: {:?}", error),
        };
        con.set_auto_commit(false);
        con.create_index("name", IndexType::Default);

        let mut rdr = csv::Reader::from_reader(io::stdin());
        for result in rdr.deserialize() {
            match result {
                Ok(record) => {
                    let records: Record = record;
                    let err = con.put(gsvec![
                        records.Name,
                        records.Mfr,
                        records.Type,
                        records.Calories,
                        records.Protein,
                        records.Fat,
                        records.Sodium,
                        records.Fiber,
                        records.Carbo,
                        records.Sugars,
                        records.Potass,
                        records.Vitamins,
                        records.Shelf,
                        records.Cups,
                        records.Rating,
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

}
