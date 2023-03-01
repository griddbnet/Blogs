#!/usr/bin/python

import griddb_python as griddb
import sys
import datetime

factory = griddb.StoreFactory.get_instance()

#Get GridStore object
store = factory.get_store(
    host="239.0.0.1",
    port=31999,
    cluster_name="defaultCluster",
    username="admin",
    password="admin"
)

print("connected to store")

#Create TimeSeries
conInfo = griddb.ContainerInfo("blog",
				[["timestamp", griddb.Type.TIMESTAMP],
	            ["active", griddb.Type.BOOL],
	            ["voltage", griddb.Type.DOUBLE]],
			griddb.ContainerType.TIME_SERIES, True)
ts = store.put_container(conInfo)

print("connected to container")
#Put row to timeseries with TIMESTAMP value and None. None means NULL value
ts.put(['2020-10-01T15:00:00.000Z', True, None])

#Put row to timeseries with current timestamp
now = datetime.datetime.utcnow()
ts.put([now, False, 100])

#Create normal query for range of timestamp from 6 hours ago to now
query = ts.query("select * where timestamp > TIMESTAMPADD(HOUR, NOW(), -6)")
rs = query.fetch()

print("Getting store")

#Get result
while rs.has_next():
	data = rs.next()
	timestamp = str(data[0])
	active = data[1]
	voltage = data[2]
	print("Time={0} Active={1} Voltage={2}".format(timestamp, active, voltage))

