We have written before about how to pair [pandas](https://pandas.pydata.org/docs/index.html) dataframes with GridDB before in our article: [Using Pandas Dataframes with GridDB](https://griddb.net/en/blog/using-pandas-dataframes-with-griddb/). In there, we read from our GridDB database via the python API (which uses TQL under the hood) and convert the resulting rows of data to a dataframe. If you're unfamilar with dataframes, they are the main purpose of using a library like Pandas and can argued  as being a superior data structure for analysis and data science. 

In this article, we want to again visit converting rows of GridDB data into dataframes, but would like to showcase using SQL with JDBC during the querying portion of our code. The reason one might want to use SQL instead of TQL is two fold: 

1. You can conduct more intricate queries with SQL because of TQL's [limited functionality](https://docs.griddb.net/tqlreference/introduction/)
2. [Partitioned tables](https://griddb.net/en/blog/griddb-partitioning-and-expiry/) are sometimes not available to be read by TQL, meaning SQL can be the only option for those specific containers

So, in this article, we will showcase how to connect to GridDB and make SQL queries with Python and directly feed those results into a pandas dataframe. And please note, we are not simply using JayDeBeApi as we have showcased in our previous article: [Using Python to interface with GridDB via JDBC with JayDeBeApi](https://griddb.net/en/blog/using-python-to-interface-with-griddb-via-jdbc-with-jaydebeapi/), because the results of *those* sql queries are not in a valid datatype to be read by pandas.

## Prerequisites

The code for this article has been containerized into a Docker container. To run it, simply install Docker and build and run the project. The source code can be found in the GridDBnet github: 

`$ git clone https://github.com/griddbnet/Blogs.git --branch sql-pandas`

You can take a look at the Dockerfile contained in the repo to see how to run this in baremetal -- essentially you just need to install Python and the appropriate SQL/pandas libraries. You will also need java installed as Java is what is used to make connections with JDBC (Java Database Connection).

## Python Libraries 

As hinted above, we will need to find and use a JDBC python library which produces rows of data that can fed into pandas' `read_sql` method call. According to the Pandas docs, the connection fed into the `read_sql` method needs to be either: "ADBC Connection, SQLAlchemy connectable, str, or sqlite3 connection".

This, of course, rules out JayDeBeApi but we were able to find a fork of the popular [SQLAlchemy](https://www.sqlalchemy.org/) library which allows for generic connections to any database which can connect via JDBC; that library can be found [here](https://pypi.org/project/sqlalchemy-jdbc-generic/) and is what allows this entire premise to work. Other than that, we will of course also need the pandas and numpy libraries to conduct our data analysis.

## Making SQL Connection with SQLAlchemy

Reading the docs for SQLAlchemy JDBC Generic: [https://pypi.org/project/sqlalchemy-jdbc-generic/](https://pypi.org/project/sqlalchemy-jdbc-generic/) along with the docs for GridDB JDBC: [https://github.com/griddb/docs-en/blob/master/manuals/GridDB_JDBC_Driver_UserGuide.md](https://github.com/griddb/docs-en/blob/master/manuals/GridDB_JDBC_Driver_UserGuide.md) allowed for us to ascertain the proper way of building out the JDBC connection string -- again, note that it's *not* the same process as building out the connection string with the JayDeBeApi library. Having set the table, here is how to create that connection string: 

```python
from sqlalchemy.engine.url import URL

eng_url = URL.create(
    drivername='sqlajdbc',
    host='/myCluster/public',
    query={
        '_class': 'com.toshiba.mwcloud.gs.sql.Driver',
        '_driver': 'gs',
        'user': 'admin',
        'password': 'admin',
        'notificationMember': 'griddb-server:20001',
        '_jars':  '/app/lib/gridstore-jdbc-5.6.0.jar'
    }
)
```

First, the drivername *must* be set as `sqlajdbc`, this is the name of the generic JDBC driver. Next, the connection order might seem a bit backwards, but this is the correct way of building the URL. One other thing, the `_jars` option expects the library jar so please make sure the path points to where you keep your GridDB JDBC jar file. If you are using the included Dockerfile, it already points to the correct path.

One last *gotcha* when trying to make this connection is that before you feed in the connection details and try to make the connection to GridDB, you will need to start the JVM (Java Virtual Machine) like so:

```python
import jpype
jpype.startJVM(jpype.getDefaultJVMPath(), "-ea", "-Djava.class.path=/app/lib/gridstore-jdbc-5.6.0.jar")
```

With this information all set, you can now make the connection and run some queries to be saved into dataframes:

```python
from sqlalchemy import create_engine
eng = create_engine(eng_url)
with eng.connect() as c:
    print("Connected")
    df = pd.read_sql("SELECT * FROM LOG_agent_intrusion WHERE exploit = True", c)

    print(df.head())
```