#!/usr/bin/python

import griddb_python as griddb
import sys
import calendar
import datetime

factory = griddb.StoreFactory.get_instance()
year_in_mili = 31536000000

#Get GridStore object
store = factory.get_store(
    host="239.0.0.1",
    port=31999,
    cluster_name="defaultCluster",
    username="admin",
    password="admin"
)

ts = store.get_container("population")
query = ts.query("select * from population where value > 280000")
rs = query.fetch()


data = rs.next()
timestamp = calendar.timegm(data[0].timetuple())
gsTS = (griddb.TimestampUtils.get_time_millis(timestamp))
time = datetime.datetime.fromtimestamp(gsTS/1000.0)

added = gsTS + (year_in_mili * 7)
addedTime = datetime.datetime.fromtimestamp(added/1000.0)

x = ts.aggregate_time_series(time, addedTime, griddb.Aggregation.AVERAGE, "value")
print("aggregation: ", x.get(griddb.Type.DOUBLE))
    
rangeQuery = ts.query_by_time_series_range(time, addedTime, griddb.QueryOrder.ASCENDING)
rangeRs = rangeQuery.fetch()
while rangeRs.has_next():
    d = rangeRs.next()
    print("d: ", d)
  
try:   
    samplingQuery = ts.query_by_time_series_sampling(time, addedTime, ["value"], griddb.InterpolationMode.LINEAR_OR_PREVIOUS, 1, griddb.TimeUnit.DAY)
    samplingRs = samplingQuery.fetch()
    while samplingRs.has_next(): 
        d = samplingRs.next()
        print("sampling: ", d)
except griddb.GSException as e:
    for i in range(e.get_error_stack_size()):
        print("[", i, "]")
        print(e.get_error_code(i))
        print(e.get_message(i))

#ts.aggregate_time_series()
#ts.query_by_time_series_range()
#ts.query_by_time_series_sampling()
