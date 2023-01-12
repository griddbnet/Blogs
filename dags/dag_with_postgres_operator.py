from datetime import datetime, timedelta

from airflow import DAG
from airflow.providers.postgres.operators.postgres import PostgresOperator
from airflow.hooks.postgres_hook import PostgresHook

from airflow.operators.python import PythonOperator

import griddb_python as griddb
import pandas as pd

factory = griddb.StoreFactory.get_instance()
DB_HOST = "griddb-server:10001"
DB_CLUSTER = "myCluster"
DB_USER = "admin"
DB_PASS = "admin"

default_args = {
    'owner': 'israel',
    'retries': 5,
    'retry_delay': timedelta(minutes=5)
}

def create_container(gridstore, device_name):
    gridstore.drop_container(device_name)
    conInfo = griddb.ContainerInfo(name=device_name,
                column_info_list=[["ts", griddb.Type.TIMESTAMP],
                                    ["co", griddb.Type.DOUBLE],
                                    ["humidity", griddb.Type.DOUBLE],
                                    ["light", griddb.Type.BOOL],
                                    ["lpg", griddb.Type.DOUBLE],
                                    ["motion", griddb.Type.BOOL],
                                    ["smoke", griddb.Type.DOUBLE],
                                    ["temperature", griddb.Type.DOUBLE]],
                type=griddb.ContainerType.TIME_SERIES)
    # Create the container
    try:
        gridstore.put_container(conInfo)
        print(conInfo.name, "container succesfully created")
    except griddb.GSException as e:
        for i in range(e.get_error_stack_size()):
            print("[", i, "]")
            print(e.get_error_code(i))
            print(e.get_location(i))
            print(e.get_message(i))

def migrate_from_postgres_to_griddb(**context):
    """
    Queries Postgres and places all data into GridDB
    """

    gridstore = factory.get_store(
        notification_member=DB_HOST, cluster_name=DB_CLUSTER, username=DB_USER, password=DB_PASS
    )

    postgres = PostgresHook(postgres_conn_id="postgres")
    conn = postgres.get_conn()
    cursor = conn.cursor()
    cursor.execute("SELECT * FROM device;")
    rows = pd.DataFrame(cursor.fetchall())

    dfs = dict(tuple(rows.groupby([1])))

    device1 = dfs['b8:27:eb:bf:9d:51']
    device1 = device1.drop([1], axis=1)
    device1 = device1.values.tolist()

    device2 = dfs['00:0f:00:70:91:0a']
    device2 = device2.drop([1], axis=1)
    device2 = device2.values.tolist()

    device3 = dfs['1c:bf:ce:15:ec:4d']
    device3 = device3.drop([1], axis=1)
    device3 = device3.values.tolist()

    container_name_list = ["device1", "device2", "device3"]

    for container_name in container_name_list:
        create_container(gridstore, container_name)


    try: 
        d1_cont = gridstore.get_container("device1")
        d1_cont.multi_put(device1)

        d2_cont = gridstore.get_container("device2")
        d2_cont.multi_put(device1)

        d3_cont = gridstore.get_container("device3")
        d3_cont.multi_put(device1)

        print("checking if data is here")
        query = d1_cont.query("SELECT *")
        rs = query.fetch()
        row = rs.next()
        print(row)
    except griddb.GSException as e:
        for i in range(e.get_error_stack_size()):
            print("[", i, "]")
            print(e.get_error_code(i))
            print(e.get_location(i))
            print(e.get_message(i))

with DAG(
    dag_id='dag_migrating_postgres_to_griddb_v04',
    default_args=default_args,
    start_date=datetime(2021, 12, 19),
    schedule_interval='@once'
) as dag:

    task1 = PythonOperator(
        task_id='migrate_from_postgres_to_griddb',
        python_callable=migrate_from_postgres_to_griddb
    )

    task1