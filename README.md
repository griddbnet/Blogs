In a previous blog, we discussed migrating a [sample IoT dataset](https://www.kaggle.com/datasets/garystafford/environmental-sensor-data-132k) from PostgreSQL to GridDB. To accomplish our feat, we used the official GridDB import/export tools, walking through exactly how to use that tool, along with considering WHY a user may want to shift from using PostgreSQL over to GridDB.

In this article, we will again consider the idea of migrating from PostgreSQL, but rather than stick to the import/export tools, we will be showcasing [Apache Airflow](https://airflow.apache.org/). If you are unfamilar with Airflow, it "is a platform to programmatically author, schedule, and monitor workflows". To put Airflow's description into simpler terms: Airflow allows for you to use python code to schedule workflows -- and these worksflows usually are broken up into smaller tasks which can be orchestrated to run in a sequence of your choosing.

For this blog, we will be using Airflow to migrate the same dataset from our previous blog; we will be moving data from PostgreSQL to GridDB. And then once that feat is accomplished, we will also be scheduling a [DAG ( Directed Acyclic Graph )](https://airflow.apache.org/docs/apache-airflow/1.10.12/concepts.html) to periodically migrate new rows from PostgeSQL over to GridDB to ensure that our two databases are always at parity.

Before we get into the technical aspects of this project, let's first get Airflow installed onto our machine, along with all prerequisites.

## Preparing and Installation

As stated, this section will go over installing this project onto your machine. This nice thing about Airflow is that they provide docker images to make installing, sharing, and extending the installation an easy problem. This article works off of using Airflow in a docker container, but more on that later.

### Prerequisites

To follow along you will need the following: 

- Docker
- Docker-compose

That's all. All other databases and libraries are installed via docker containers.

### Grabbing Source Code

To grab all source code for this project, please clone the following repository: []()

```bash
$ git clone --branch 
```

Once it's cloned, you will have a directory with all of the necessary docker files needed to start.

### The Docker Containers

We will go through first the included Dockerfiles and then the docker-compose file before running our project.

#### Dockerfile for Extending the Airflow Image

To start, you will notice there is a file called `Dockerfile.airflow`. This file [extends](https://airflow.apache.org/docs/docker-stack/build.html#quick-start-scenarios-of-image-extending) the original `apache/airflow` image to add some of the Python libraries we need for our project.

The file looks like so: 

```bash
FROM apache/airflow:latest-python3.10
COPY requirements.txt /requirements.txt

USER root
RUN apt-get update \
  && apt-get install -y --no-install-recommends \
         default-jre  wget build-essential swig \
  && apt-get autoremove -yqq --purge \
  && apt-get clean \
  && rm -rf /var/lib/apt/lists/*

RUN wget https://repo1.maven.org/maven2/com/github/griddb/gridstore-jdbc/5.1.0/gridstore-jdbc-5.1.0.jar -P /usr/share/java

# Install GridDB c_client
WORKDIR /
RUN wget --no-check-certificate https://github.com/griddb/c_client/releases/download/v5.0.0/griddb-c-client_5.0.0_amd64.deb
RUN dpkg -i griddb-c-client_5.0.0_amd64.deb

USER airflow

RUN pip install --user --upgrade pip
RUN pip install --no-cache-dir --user -r /requirements.txt
RUN pip install --no-cache-dir apache-airflow-providers-common-sql
RUN pip install --no-cache-dir apache-airflow-providers-jdbc
RUN pip install --no-cache-dir griddb-python

ENV JAVA_HOME=/usr/share/java/gridstore-jdbc-5.1.0.jar
```

To start, for this project, we are installing JDBC and the GridDB Python client. For the GridDB Python client to install, we need python3.10, which is why we chose to extend `apache/airflow:latest-python3.10`. With the GridDB Python Client installed, we can access GridDB via TQL, but if you would like to use SQL, you can also use JayDeBeAPI and the JDBC connector, which is also included within this file.

#### Dockerfile for Extending the GridDB Image

The next file I'd like to showcase here is `Dockerfile.griddb`. Here are the contents: 

```bash
from griddb/griddb

USER root

# Install GridDB c_client
WORKDIR /
RUN wget --no-check-certificate https://github.com/griddb/c_client/releases/download/v5.0.0/griddb-c-client_5.0.0_amd64.deb
RUN dpkg -i griddb-c-client_5.0.0_amd64.deb

RUN wget --no-check-certificate https://github.com/griddb/cli/releases/download/v5.0.0/griddb-ce-cli_5.0.0_amd64.deb
RUN dpkg -i griddb-ce-cli_5.0.0_amd64.deb

USER gsadm
```

We are extending the base GridDB image solely to add in the GridDB C-Client as a prereq for other GridDB libraries; we are also adding in the GridDB CLI tool so that we may query our DB easily if needed.

#### Airflow's Docker-Compose File

The most important part of the installation process is using the `docker-compose.yml` file provided by the Airflow team. This file contains all of the different services/containers which are needed to run the Airflow tool. The nice thing about using docker-compose is that all of the services contained within the file are automatically placed within a shared network space. Not only that, but we can bring up and bring down all services with one singular command -- including those images which we extended ourselves. Handy!

I will not be showcasing the entirety of the contents here as the file is really large, but here is a snippet: 

```bash
---
version: '3'
x-airflow-common:
  &airflow-common
  image: ${AIRFLOW_IMAGE_NAME:-extending_airflow:latest}
  environment:
    &airflow-common-env
    AIRFLOW__CORE__EXECUTOR: CeleryExecutor
    AIRFLOW__DATABASE__SQL_ALCHEMY_CONN: postgresql+psycopg2://airflow:airflow@postgres/airflow
    # For backward compatibility, with Airflow <2.3
    AIRFLOW__CORE__SQL_ALCHEMY_CONN: postgresql+psycopg2://airflow:airflow@postgres/airflow
    AIRFLOW__CELERY__RESULT_BACKEND: db+postgresql://airflow:airflow@postgres/airflow
    AIRFLOW__CELERY__BROKER_URL: redis://:@redis:6379/0
    AIRFLOW__CORE__FERNET_KEY: ''
    AIRFLOW__CORE__DAGS_ARE_PAUSED_AT_CREATION: 'true'
    AIRFLOW__CORE__LOAD_EXAMPLES: 'true'
    AIRFLOW__API__AUTH_BACKENDS: 'airflow.api.auth.backend.basic_auth'
    _PIP_ADDITIONAL_REQUIREMENTS: ${_PIP_ADDITIONAL_REQUIREMENTS:-}
  volumes:
    - ./dags:/opt/airflow/dags
    - ./logs:/opt/airflow/logs
    - ./plugins:/opt/airflow/plugins
  user: "${AIRFLOW_UID:-50000}:0"
  depends_on:
    &airflow-common-depends-on
    redis:
      condition: service_healthy
    postgres:
      condition: service_healthy

  griddb-server:
    build:
      context: .
      dockerfile: Dockerfile.griddb
    expose:
      - "10001"
      - "10010"
      - "10020"
      - "10040"
      - "20001"
      - "41999"
    environment:
      NOTIFICATION_MEMBER: 1
      GRIDDB_CLUSTER_NAME: myCluster


  postgres:
    image: postgres:13
    environment:
      POSTGRES_USER: airflow
      POSTGRES_PASSWORD: airflow
      POSTGRES_DB: airflow
    volumes:
      - postgres-db-volume:/var/lib/postgresql/data
      - ./dags:/var/lib/postgresql/dags
    ports: 
      - 5432:5432
    healthcheck:
      test: ["CMD", "pg_isready", "-U", "airflow"]
      interval: 5s
      retries: 5
    restart: always

      airflow-webserver:
    <<: *airflow-common
    command: webserver
    ports:
      - 8080:8080
    healthcheck:
      test: ["CMD", "curl", "--fail", "http://localhost:8080/health"]
      interval: 10s
      timeout: 10s
      retries: 5
    restart: always
    depends_on:
      <<: *airflow-common-depends-on
      airflow-init:
        condition: service_completed_successfully
```

As can be seen here, this file contains instructions for running various containers, including the two databases being showcased here. You will also notice that the GridDB service is being build directly from the local `Dockerfile.griddb` file. Astute oberservers may also notice that Airflow is not getting the same treatment despite it also having its own `Dockerfile` in this repository.

The reason for that is that the original Airflow compose orchestration file uses the `x-airflow-common` environment variables to keep things consistent with this file among the many different services. To play nice with this sort of docker-compose file building, we must build our image locally so that the compose file can use that built image for the purposes of this project.

### Running the Docker Containers

As stated above, the very first thing we must do is build our `Dockerfile.airflow` image first, and once it's done, we can finally use the `docker-compose` command to run everything at once.

```bash
$ docker build -f Dockerfile.airflow . --tag extending_airflow:latest
```

Once this image is built, you should be able to see the `extending_airflow:latest` image in your local environment (`$ docker images`).

And now that this is ready, we can go ahead and run all of our services.

```bash
$ docker-compose up -d
```

    [+] Running 8/8
    ⠿ Container airflow-griddb-server-1      Running                                                                  0.0s
    ⠿ Container airflow-redis-1              Healthy                                                                 18.6s
    ⠿ Container airflow-postgres-1           Healthy                                                                 18.6s
    ⠿ Container airflow-airflow-init-1       Exited                                                                  37.2s
    ⠿ Container airflow-airflow-triggerer-1  Started                                                                 37.9s
    ⠿ Container airflow-airflow-scheduler-1  Started                                                                 37.9s
    ⠿ Container airflow-airflow-worker-1     Started                                                                 37.9s
    ⠿ Container airflow-airflow-webserver-1  Started                                                                 37.5s

This will go through and grab all relevant images from dockerhub or build the images locally (`Dockerfile.griddb`) and then once it's ready, all containers will be running directly on your machine.

This can be verified by running the `process status` command:

```bash
$ docker ps
```

    CONTAINER ID   IMAGE                                 COMMAND                  CREATED             STATUS
                PORTS

                                                                                    NAMES
    8c0a24ed7cf5   extending_airflow:latest              "/usr/bin/dumb-init …"   48 seconds ago      Up 21 seconds (healthy)            0.0.0.0:8080->8080/tcp, :::8080->8080/tcp

                                                                                    airflow-airflow-webserver-1
    754c4dc920b9   extending_airflow:latest              "/usr/bin/dumb-init …"   56 seconds ago      Up 21 seconds (health: starting)   8080/tcp

                                                                                    airflow-airflow-worker-1
    8e270bdb15b6   extending_airflow:latest              "/usr/bin/dumb-init …"   58 seconds ago      Up 21 seconds (healthy)            8080/tcp

                                                                                    airflow-airflow-triggerer-1
    9b9f903469c0   extending_airflow:latest              "/usr/bin/dumb-init …"   58 seconds ago      Up 21 seconds (healthy)            8080/tcp

                                                                                    airflow-airflow-scheduler-1
    5d109c0d5812   postgres:13                           "docker-entrypoint.s…"   59 seconds ago      Up 48 seconds (healthy)            0.0.0.0:5432->5432/tcp, :::5432->5432/tcp

                                                                                    airflow-postgres-1
    2defcd5a15ad   redis:latest                          "docker-entrypoint.s…"   59 seconds ago      Up 48 seconds (healthy)            6379/tcp

## Using Apache Airflow 

Now that we have our tool up and running, the next thing we will want to do is of course interface with it. One of the containers/services within our docker-compose file hosts a webserver with a nice UI used for managing your workflows.

So in a browser, head over to http://localhost:8080/ and enter in your credentials (username and password are both `airflow`). From here, you will a big list of premade DAGs, these are the workflows which we manage to orchestrate our data flows. In our case, we want to make two different DAGs, one for the one-time migration of all of our Postgresql data, and then another for a continuous migration. 

But before we do that, let's first make sure our newly created PostgreSQL database has the data we wish to showcase being migrated over.


### Using the Airflow UI to Make Connections to our Databases

Before we head into writing our Python code (DAGs), let's first make sure our Airflow scheduler/worker/webserver can interact with our GridDB server and PostgreSQL databases. As explained before, because all of these services share a docker-compose file, they will automatically all share the same network space. 

First let's connect to PostgreSQL. 


#### Connect to PostgreSQL

So first let's connect to our PostgreSQL database:

From the browser, navigate over to Admin --> Connections.

Now we can make an explicit connection to our targeted database. For PostgreSQL, select it from the connection type dropdown and then enter in your credentials 

```bash
Host: postgres
Schema: postgres
Login: airflow
Password: airflow
Port: 5432
```

Once you hit test, it should show up as successful. And as a note, the host is the name of your service (hostname equates to IP address in a shared network).

![image]

#### Connect to GridDB

To connect to GridDB you can connect via JDBC in the same vein as the way mentioned above. 

![image]

But you can also connect via the Python client

```python
import griddb_python as griddb

factory = griddb.StoreFactory.get_instance()
DB_HOST = "griddb-server:10001" #griddb-server is the hostname of the service
DB_CLUSTER = "myCluster"
DB_USER = "admin"
DB_PASS = "admin"

gridstore = factory.get_store(
    notification_member=DB_HOST, cluster_name=DB_CLUSTER, username=DB_USER, password=DB_PASS
)
```


### Ingesting Data into PostgreSQL Container Database

For demo purposes, we will ingest the relevant data that we need into our database by copying over the CSV to our PostgreSQL container and then using the `COPY` command.

So first, please grab your PostgreSQL's container's image id and SSH copy over the csv to that container. If you working out of the root of our project's repo, you can find the `.csv` file within `dags/data/device.csv`

```bash
$ docker cp dags/data/device.csv <PostgreSQL image id>:/tmp/
```

Once you copy over the csv file, please ssh into your PostgreSQL container: 

```bash
$ docker exec -it <PostgreSQL image id> bash
```

Once in there, drop into the psql shell as user airflow.

```bash
# # psql -U airflow
psql (13.9 (Debian 13.9-1.pgdg110+1))
Type "help" for help.

airflow=#
```

From here, it's trivial to ingest the CSV data into our DB. First we create our table and then tell our database to copy the CSV rows into that table.

```bash
airflow=# CREATE TABLE if not exists device ( ts timestamp, device varchar(30), co float8, humidity float8, light bool, lpg float8, motion bool, smoke float8, temp float8 );
```
    CREATE TABLE

And then COPY everything

```bash
airflow=# copy device(ts, device, co, humidity, light, lpg, motion, smoke, temp) from '/tmp/device.csv' DELIMITER ',' CSV HEADER;
```

### Migrating from PostgreSQL to GridDB

And now we will create our first DAG to accomplish our task. Because Airflow is built on Python, our DAGs are simply Python code with some simple dressing. Before we get into the file snippet, I will point out that since we using the GridB Python client, we can easily interface with our GridDB database from directly within our Airflow. So when you see the code involved, a lot of it will not be too different from a normal python application. 

Anyway, here is is a snippet of our GridDB Migration DAG. This first portion will be kept to only showcasing the Airflow-specific portion of the code. First, we will import the Airflow libraries necessary to finish our task.

```python
from airflow import DAG
from airflow.providers.postgres.operators.postgres import PostgresOperator
from airflow.hooks.postgres_hook import PostgresHook

from airflow.operators.python import PythonOperator

default_args = {
    'owner': 'israel',
    'retries': 5,
    'retry_delay': timedelta(minutes=5)
}

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
```

Once that's out of the way, we will put in config options for our DAG, namely its ID (name) and our chosen interval for this task to be run. One thing I will point out about this specific DAG: the scheduled interval is simpyly `@once`, which means once we run this DAG once, will cease to schedule itself to run again -- the continuos migration will be handled by a separate DAG.
 
Moving on, at the very bottom of the file we select a sequence of what tasks we would like done and in what order. For this workflow, we are simply calling one thing, task1, which simply calls a python function `migrate_from_postgres_to_griddb`. 

The actual migration works how you may expect: it will query PostgreSQL, take all relevant data, transform it for our needs, and then `put` into our GridDB server.

Here is that Python function which handles the actual migration: 

```python
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
```

