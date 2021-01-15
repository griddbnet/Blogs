#!/usr/bin/env python3


import griddb_python as griddb
import json
import datetime
import requests

def getJSON(bdate, edate):

    url = "https://aqs.epa.gov/data/api/sampleData/byCounty?email=EMAILADDRESS&key=SECRETKEY&param=42602&bdate="+bdate+"&edate="+edate+"&state=06&county=037"

    payload={}
    headers = {}

    response = requests.request("GET", url, headers=headers, data=payload)

    return (response.text)


def writeContainer():

    factory = griddb.StoreFactory.get_instance()
    gridstore = factory.get_store(host='239.0.0.1', port=31999, cluster_name='defaultCluster', username='admin', password='admin')

    #Create Collection
    conInfo = griddb.ContainerInfo("LosAngelesNO2",
                        [["timestamp", griddb.Type.TIMESTAMP],
                        ["notwo", griddb.Type.DOUBLE]],
                        griddb.ContainerType.TIME_SERIES, True)

    rows = []
    data = getJSON('20191201', '20191231')
    data = json.loads(data)
    for p in data['Data']:
        date_time_str = p['date_local'] + ' '+  p['time_local']
        date_time_obj = datetime.datetime.strptime(date_time_str, '%Y-%m-%d %H:%M')
        row = [None, None]
        row[0] = date_time_obj.timestamp()
        row[1] = p['sample_measurement']
        rows.append(row)

    con = gridstore.put_container(conInfo);
    con.set_auto_commit(False)
    con.multi_put(rows)
    length = str(len(rows))
    print("Wrote "+ length + " rows")
    con.commit()

def readContainer():
    try:
        cn = gridstore.get_container("air");
        query = cn.query("select *")
        rs = query.fetch(False)
        rs.timestamp_output_with_float = True
    except:
        return []
    retval= []
    while rs.has_next():
        data = rs.next()
        retval.append(data)

    print("retval", retval)

if __name__ == "__main__":
    writeContainer()
