#!/usr/bin/python

import griddb_python as griddb
import sys

factory = griddb.StoreFactory.get_instance()

argv = sys.argv
try:
    #Get GridStore object
    gridstore = factory.get_store(host=argv[1], port=int(argv[2]), cluster_name=argv[3], username=argv[4], password=argv[5])

    #Create Timeseries
    name_topic = ["D001", "D002", "T102", "BATP102", "T103", "BATP103", "T101", "MA016", "D003", "M009"]
    for name in name_topic:
        conInfo = griddb.ContainerInfo(name,
                        [["name", griddb.Type.TIMESTAMP],
                        ["status", griddb.Type.BOOL],
                        ["count", griddb.Type.LONG],
                        ["sign", griddb.Type.STRING]],
                        griddb.ContainerType.TIME_SERIES, True)
        col = gridstore.put_container(conInfo)
        #Change auto commit mode to false
        col.set_auto_commit(False)
        #Put row: RowKey is "name01"
        ret = col.put(['2020-10-01T15:00:00.000Z', False, 1, "griddb"])
        col.commit()
except griddb.GSException as e:
    for i in range(e.get_error_stack_size()):
        print("[", i, "]")
        print(e.get_error_code(i))
        print(e.get_location(i))
        print(e.get_message(i))
