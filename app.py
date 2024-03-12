import jpype
import jpype.dbapi2
from datetime import datetime, timedelta
import pandas as pd


url = "jdbc:gs://127.0.0.1:20001/myCluster/public"
jpype.startJVM(jpype.getDefaultJVMPath(), "-ea", "-Djava.class.path=./lib/gridstore-jdbc.jar")
conn = jpype.dbapi2.connect(url, driver="com.toshiba.mwcloud.gs.sql.Driver", driver_args={"user": "admin", "password": "admin"})
curs = conn.cursor()

createIntervalPartition = """CREATE TABLE IF NOT EXISTS pyIntPart (
  date TIMESTAMP NOT NULL PRIMARY KEY,
  value STRING
)
WITH
(expiration_type='PARTITION',expiration_time=10,expiration_time_unit='DAY')
PARTITION BY RANGE (date) EVERY (5, DAY);"""
curs.execute(createIntervalPartition)

createSubHashPartition= """CREATE TABLE IF NOT EXISTS pyIntHashPart (
  date TIMESTAMP,
  value STRING,
  PRIMARY KEY (date, value)
) 
WITH (
  expiration_type='PARTITION',
  expiration_time=4,
  expiration_time_unit='DAY'
) PARTITION BY RANGE (date) EVERY (2, DAY)
SUBPARTITION BY HASH (value) SUBPARTITIONS 64;"""
curs.execute(createSubHashPartition)

now = datetime.utcnow()
for x in range (0, 20):
    pyIntPart = "INSERT INTO pyIntPart values ( TO_TIMESTAMP_MS(" + str(now.timestamp()*1000) +"), 'val" + str(x) + "')"
    pyIntHashPart = "INSERT INTO pyIntHashPart values ( TO_TIMESTAMP_MS(" + str(now.timestamp()*1000) +"), 'val" + str(x) + "')"
    #print(pyIntPart)
    #print(pyIntHashPart)
    curs.execute(pyIntPart)
    curs.execute(pyIntHashPart)
    now = now - timedelta(days=1)

curs.execute("SELECT * FROM pyIntPart")
cols = tuple(zip(*curs.description))[0]
df = pd.DataFrame(curs.fetchall(), columns=cols)
print(df)

curs.execute("SELECT * FROM pyIntHashPart")
cols = tuple(zip(*curs.description))[0]
df = pd.DataFrame(curs.fetchall(), columns=cols)
print(df)