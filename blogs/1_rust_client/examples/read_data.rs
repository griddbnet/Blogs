use konektor_db::get_value;
use konektor_db::griddb::ContainerInfo::*;
use konektor_db::griddb::StoreFactory::*;
use konektor_db::griddb::Type::*;
use konektor_db::griddb::Value::*;

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

}