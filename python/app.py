
import jaydebeapi
import urllib.parse

notification_provider = "https://dbaasshareextconsta.blob.core.windows.net/dbaas-share-extcon-blob/trial1602.json?sv=2015-04-05&sr=b&st=2023-03-14T00%3A00%3A00.00Z&se=2073-03-14T00%3A00%3A00.0000000Z&sp=r&sig=h2VJ0xAqsnfdgfgdfgRsqWVgfgfg5CAS66RifPIZ1PDCJ0x%2FiXb2FOhA%3D"
np_encode = urllib.parse.quote(notification_provider)

cluster_name = "gs_clustertrial1602"
cn_encode = urllib.parse.quote(cluster_name)

database_name = "public"
dbn_encode = urllib.parse.quote(database_name)

sslMode = "&sslMode=VERIFY"
sm_encode = urllib.parse.quote(sslMode)

username = "israel"
password = "israel"

url = "jdbc:gs:///" + cn_encode +  "/" + dbn_encode + "?notificationProvider=" + np_encode + sm_encode 

print("JDBC URL = " + url)

conn = jaydebeapi.connect("com.toshiba.mwcloud.gs.sql.Driver",
    url, 
    {'user': username, 'password': password,
     'connectionRoute': 'PUBLIC'}
    , "../lib/gridstore-jdbc.jar")

curs = conn.cursor()

curs.execute("DROP TABLE IF EXISTS Sample")
curs.execute("CREATE TABLE IF NOT EXISTS Sample ( id integer PRIMARY KEY, value string )")
print('SQL Create Table name=Sample')
curs.execute("INSERT INTO Sample values (0, 'test0'),(1, 'test1'),(2, 'test2'),(3, 'test3'),(4, 'test4')")
print('SQL Insert')
curs.execute("SELECT * from Sample where ID > 2")
print(curs.fetchall())

curs.close()
conn.close()
print('success!')
