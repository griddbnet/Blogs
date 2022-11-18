use konektor_db::griddb::ContainerInfo::*;
use konektor_db::griddb::StoreFactory::*;
use konektor_db::griddb::Type::*;
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

    match store.put_container(&tsinfo, false) {
        Ok(result) => result,
        Err(error) => panic!("Error store put_container() with error code: {:?}", error),
    };
}