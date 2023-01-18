
use konektor_db::griddb::ContainerInfo::*;
use konektor_db::griddb::StoreFactory::*;
use konektor_db::griddb::Type::*;
use konektor_db::griddb::Value::*;
use konektor_db::gsvec;
use chrono:: Utc;

fn main() {
    // get default factory
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
    
        let ts = match store.put_container(&tsinfo, false) {
            Ok(result) => result,
            Err(error) => panic!("Error store put_container() with error code: {:?}", error),
        };
        // Grab current time to use as time value for container
        let timestamp: Timestamp = Timestamp {
            value: Utc::now().timestamp_millis(),
        };

        ts.put(gsvec![timestamp, 0.004342, 49.0, false, 0.00753242, false, 0.0212323, 23.2]);

        let timestamp_second: Timestamp = Timestamp {
            value: Utc::now().timestamp_millis() + 1000,
        };
        ts.put(gsvec![timestamp_second, 0.0065342, 31.0, false, 0.753242, false, 0.02653323, 27.2]);
        ts.commit();

    }