#!/usr/bin/python3
import griddb_python
import Geohash
import proximityhash
import time
griddb = griddb_python
factory = griddb.StoreFactory.get_instance()


gridstore = factory.get_store(
    host="239.0.0.1",
    port=31999,
    cluster_name="defaultCluster",
    username="admin",
    password="admin"
)
 


lat = 40.758315
lon = -73.985549
distance=500

radius_hashes = proximityhash.create_geohash(lat, lon, distance, 7, georaptor_flag=False).split(',')
container_hashes = proximityhash.create_geohash(lat, lon, distance, 6, georaptor_flag=False).split(',')

ts = gridstore.get_container("geo_"+container_hashes[0])
start = time.time()
for container in container_hashes:

    ts = gridstore.get_container("geo_"+container)
    tql = "SELECT * WHERE "
    orstmt=""
    for geohash in radius_hashes:
        if geohash.startswith(container):
            tql = tql + orstmt + "geohash LIKE '"+geohash+"%'"
            orstmt=" OR "

    if orstmt != "":
        try:
            query = ts.query(tql) 
            rs = query.fetch(False) 

            while rs.has_next():
                data = rs.next()
                print(repr(data))
        except:
            pass
        
print("took ",time.time()-start) 
