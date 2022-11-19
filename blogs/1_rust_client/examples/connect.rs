use konektor_db::griddb::StoreFactory::*;

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
    let _store = match factory.get_store(properties) {
        Ok(_result) => println!("Successfully Connected to GridDB"),
        Err(error) => panic!("Error factory get_store() with error code: {:?}", error),
    };

}