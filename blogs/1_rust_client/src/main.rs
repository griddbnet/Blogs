#![allow(non_snake_case)]

use konektor_db::get_value;
use konektor_db::griddb::ContainerInfo::*;
use konektor_db::griddb::StoreFactory::*;
use konektor_db::griddb::Type::*;
use konektor_db::griddb::Value::*;
use konektor_db::gsvec;
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

    let factory = StoreFactory::get_instance();
    let properties = vec![
        ("notification_member", "127.0.0.1:10001"),
        ("cluster_name", "myCluster"),
        ("user", "admin"),
        ("password", "admin"),
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