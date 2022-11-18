use konektor_db::get_value;
use konektor_db::griddb::ContainerInfo::*;
use konektor_db::griddb::StoreFactory::*;
use konektor_db::griddb::Type::*;
use konektor_db::griddb::Value::*;
use konektor_db::gsvec;
use chrono::Utc;

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

    }