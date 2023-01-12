import os
from datetime import datetime, timedelta

from airflow import DAG

from airflow.operators.postgres_operator import PostgresOperator
from airflow.hooks.postgres_hook import PostgresHook
from airflow.providers.jdbc.operators.jdbc import JdbcOperator
from airflow.providers.jdbc.hooks.jdbc import JdbcHook

from airflow.operators.python import PythonOperator

from airflow.decorators import task

import griddb_python as griddb

factory = griddb.StoreFactory.get_instance()
DB_HOST = "griddb-server:10001"
DB_CLUSTER = "myCluster"
DB_USER = "admin"
DB_PASS = "admin"

default_args = {
    'owner': 'israel',
    'retries': 5,
    'retry_delay': timedelta(minutes=35)
}

def main():

    gridstore = factory.get_store(
        notification_member=DB_HOST, cluster_name=DB_CLUSTER, username=DB_USER, password=DB_PASS)

    try:
        d1_cont = gridstore.get_container("device1")
        d2_cont = gridstore.get_container("device2")
        d3_cont = gridstore.get_container("device3")

        sql = "SELECT MAX(ts)"
        d1_query = d1_cont.query(sql)
        d2_query = d2_cont.query(sql)
        d3_query = d3_cont.query(sql)
        
        d1_rs = d1_query.fetch()
        d2_rs = d2_query.fetch()
        d3_rs = d3_query.fetch()

        d1_row = d1_rs.next().get(griddb.Type.TIMESTAMP)
        d1_latest_time = d1_row.replace(microsecond=999999) # adding in max microseconds as GridDB does not save these values

        d2_row = d2_rs.next().get(griddb.Type.TIMESTAMP)
        d2_latest_time = d2_row.replace(microsecond=999999)

        d3_row = d3_rs.next().get(griddb.Type.TIMESTAMP)
        d3_latest_time = d3_row.replace(microsecond=999999)

    except griddb.GSException as e:
        for i in range(e.get_error_stack_size()):
            print("[", i, "]")
            print(e.get_error_code(i))
            print(e.get_location(i))
            print(e.get_message(i))

    postgres = PostgresHook(postgres_conn_id='postgres')
    postgres_conn = postgres.get_conn()
    cursor = postgres_conn.cursor()

    #device1 = b8:27:eb:bf:9d:51 \ device2 = 00:0f:00:70:91:0a \ device3 = 1c:bf:ce:15:ec:4d
    d1_sql = "SELECT DISTINCT ON (ts) * FROM device WHERE ts > '" + str(d1_latest_time)+ "' AND device = 'b8:27:eb:bf:9d:51' ORDER BY ts DESC;"
    d2_sql = "SELECT DISTINCT ON (ts) * FROM device WHERE ts > '" + str(d2_latest_time)+ "' AND device = '00:0f:00:70:91:0a' ORDER BY ts DESC;"
    d3_sql = "SELECT DISTINCT ON (ts) * FROM device WHERE ts > '" + str(d3_latest_time)+ "' AND device = '1c:bf:ce:15:ec:4d' ORDER BY ts DESC;"
    
    cursor.execute(d1_sql)
    d1_result = cursor.fetchall()

    cursor.execute(d2_sql)
    d2_result = cursor.fetchall()

    cursor.execute(d3_sql)
    d3_result = cursor.fetchall()

    if not d1_result:
        print("d1 is empty")
    else:
        for row in d1_result:
            print("putting Device 1 to GridDB")
            print(d1_latest_time)
            print(d1_sql)
            row = list(row)
            del row[1] #get rid of device name
            print(row)
            d1_cont.put(row)

    if not d2_result:
        print("d2 is empty")
    else:
        for row in d2_result:
            print("putting Device 2 to GridDB")
            row = list(row)
            print(d2_latest_time)
            print(d2_sql)
            del row[1] #get rid of device name
            print(row)
            d2_cont.put(list(row))

    if not d3_result:
        print("d3 is empty")
    else:
        for row in d3_result:
            print("putting Device 3 to GridDB")
            row = list(row)
            print(d3_latest_time)
            print(d3_sql) #get rid of device name
            del row[1]
            print(row)
            d3_cont.put(list(row))

with DAG(
    dag_id='griddb_postgres_migration_v03',
    default_args=default_args,
    start_date=datetime(2022, 12, 19),
    schedule_interval='0 * * * *'
) as dag:

    task1 = PythonOperator(
        task_id='main',
        python_callable=main
    )

task1
