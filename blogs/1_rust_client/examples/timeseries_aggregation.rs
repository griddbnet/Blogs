#![allow(non_snake_case)]

use konektor_db::get_value;
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

        store.drop_container("device13");
    
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

        let ts_row = match ts.get(timestamp) {
            Ok(result) => result,
            Err(error) => panic!("Error container get row with error code: {:?}", error),
        };
        println!("Singular get row of ts: {:?}", ts_row);

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
    }
