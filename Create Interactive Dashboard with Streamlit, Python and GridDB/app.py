#!/usr/bin/python3
import pandas as pd
import streamlit as st
import griddb_python
import datetime
import traceback

griddb = griddb_python
factory = griddb.StoreFactory.get_instance()

gridstore = factory.get_store(
    host="239.0.0.1",
    port=31999,
    cluster_name="defaultCluster",
    username="admin",
    password="admin"
)

cols = {}
for x in range(1, 125):
    col = gridstore.get_container("precinct_"+str(x))
    if col != None:
        cols[x] = [col]
cols['all'] = (e[0] for e in cols.values())

date_range = st.date_input("Period", [datetime.date(2010, 1,1), datetime.date(2020, 12, 31) ])
precinct = st.select_slider("Precinct", list(cols.keys()), value='all' )
br_name = st.selectbox("Borough", ["ALL", "MANHATTAN", "QUEENS", "BRONX" , "BROOKLYN", "STATEN ISLAND" ])
ctype = st.radio("Type", ["ALL", "VIOLATION", "MISDEMEANOR" , "FELONY" ])

start = datetime.datetime.combine(date_range[0], datetime.datetime.min.time())
end = datetime.datetime.combine(date_range[1], datetime.datetime.min.time())
tql = "select * where CMPLNT_FR > TO_TIMESTAMP_MS("+str(int(start.timestamp()*1000))+") AND CMPLNT_FR < TO_TIMESTAMP_MS("+str(int(end.timestamp()*1000))+")"

delta = end - start

if br_name != "ALL":
    tql = tql + " AND BORO_NM = '"+br_name+"'"

if ctype != "ALL":
    tql = tql + " AND LAW_CAT_CD = '"+ctype+"'"

df=pd.DataFrame()
for col in cols[precinct]:
    try:
        q = col.query(tql)
        rs = q.fetch(False)
        df = df.append(rs.fetch_rows())
    except:
        pass


period='Y'
if delta.days < 800:
    period = 'M'
elif delta.days < 90:
    period = 'D'


df['CMPLNT_FR'] = pd.to_datetime(df['CMPLNT_FR'])
df = df.groupby([pd.Grouper(key='CMPLNT_FR',freq=period)]).size().reset_index(name='count')

if period=='M':
    df['CMPLNT_FR'] = d['CMPLNT_FR'].dt.to_period('M').dt.to_timestamp()
df = df.set_index('CMPLNT_FR')
print(df)

st.bar_chart(data=df)
