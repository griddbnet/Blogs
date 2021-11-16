import griddb_python
griddb = griddb_python
factory = griddb.StoreFactory.get_instance()

gridstore = factory.get_store(
    host="239.0.0.1",
    port=31999,
    cluster_name="defaultCluster",
    username="admin",
    password="admin"
)

