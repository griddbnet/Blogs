use konektor_db::get_value;
use konektor_db::griddb::StoreFactory::*;
use konektor_db::griddb::Value::Timestamp;

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

    let ts = match store.get_container("device1") {
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
    println!(" humidity time avg = {:}",agg_data.get_as_f64().1);

    // TIME SAMPLE from Time Series Aggregation functionality using TQL
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
    }
}
