from datetime import datetime, timedelta

from airflow import DAG
from airflow.operators.python import PythonOperator


default_args = {
    'owner': 'israel',
    'retry': 5,
    'retry_delay': timedelta(minutes=5)
}


def get_sqlparse():
    import sqlparse
    print(f"sqlparse with version: {sqlparse.__version__} ")


def get_jaydebeapi():
    import jaydebeapi
    print(f"jaydebeapi with version: {jaydebeapi.__version__}")


with DAG(
    default_args=default_args,
    dag_id="dag_with_python_dependencies_v02",
    start_date=datetime(2022, 12, 20),
    schedule_interval='0 0 * * *'
) as dag:
    task1 = PythonOperator(
        task_id='get_sqlparse',
        python_callable=get_sqlparse
    )
    
    task2 = PythonOperator(
        task_id='get_jaydebeapi',
        python_callable=get_jaydebeapi
    )

    task1 >> task2
