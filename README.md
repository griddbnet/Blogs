GridDB v5.7.0 has released and we would like to go over some of the new features. You can download the new release directly from [GitHub]() and from the [Downloads](/en/download) page. In this article, we specifically want to take a closer look at two of the new features: WebAPI changes and SQL Working Memory limit. 

## GridDB Web API Changes in v5.7.0

The GridDB WebAPI allows for you to interact with your GridDB server via HTTP Requests. The major changes in this release are expanded capability of the SQL functionality, as well as a new way to install the package itself. Packaged within this new release are SQL commands which were previously unavailable, including DDL, DML, and DCL commands. On the github page, you will also find `.deb` and `.rpm` files for installation, which will also add installing the web api as a service.

The following sections' information can be found on the project's github page: [https://github.com/griddb/webapi/blob/master/GridDB_Web_API_Reference.md](https://github.com/griddb/webapi/blob/master/GridDB_Web_API_Reference.md)

### Installation Changes 

As explained above, you can now install the GridDB Web API as a package, meaning it will auto-populate all necessary directories for you and also create a symlink so that you can launch the service using `systemctl`, similar to how the GridDB server operates. To do so, grab the `.deb` file from [https://github.com/griddb/webapi/releases/tag/5.7.0](https://github.com/griddb/webapi/releases/tag/5.7.0) and run: 

`$ sudo dpkg -i https://github.com/griddb/webapi/releases/download/5.7.0/griddb-ce-webapi_5.7.0_amd64.deb`

Once installed, you can make the changes you deem necessary in the following directory: `/var/lib/gridstore/webapi/conf`. And then to start: `$ sudo systemctl start griddb-webapi.service`

### SQL DDL (Data Definition Language)

To me, the standout feature for the Web API is the inclusion of SQL DDL Commands. With this, we can now create tables using the Web API with the familar SQL syntax and are no longer required to use the TQL API for creating our tables. And though the previous methods worked just fine before, the benefits of this addition are two-fold: 1, SQL is a widely-known query language and therefore easier to work with, and 2, TQL commands did not allow for manipulating or creating partitioned tables/containers.

Now let's take a look at creating a table using the new SQL DDL Command. The URL path is as follows: `/:cluster/dbs/:database/sql/ddl`. Here's a working example:

```bash
curl --location 'http://192.168.50.206:8082/griddb/v2/myCluster/dbs/public/sql/ddl' \
--header 'Content-Type: application/json' \
--header 'Authorization: ••••••' \
--data '[
 {"stmt" : "CREATE TABLE IF NOT EXISTS pyIntPart2 (date TIMESTAMP NOT NULL PRIMARY KEY, value STRING) WITH (expiration_type='\''PARTITION'\'',expiration_time=10,expiration_time_unit='\''DAY'\'') PARTITION BY RANGE (date) EVERY (5, DAY);"},
 {"stmt" : "ALTER TABLE pyIntPart2 ADD temp STRING"}
]'
```

Here we are making two statements, one to create a partitioned table with expiry rules (you can read about that here: [https://griddb.net/en/blog/griddb-partitioning-and-expiry/](https://griddb.net/en/blog/griddb-partitioning-and-expiry/), and another to alter that same table and add a new column. Again, this was not possible before because of the nature of partitioned tabled.

### SQL DML (Data Manipulation Language)

With DML commands, we can run SELECT, INSERT, UPDATE, etc commands on our containers. This functionality was partly there prior to the v5.7.0 release, though it was a lot more limited and is no longer recommended to be used. To conduct a DML request, the path is similar to the one above: `/:cluster/dbs/:database/sql/dml/query`. With this, you can run a SELECT statement to run a lookup of some data from your container. For example

```bash
curl --location 'http://192.168.50.206:8082/griddb/v2/myCluster/dbs/public/sql/dml/query' \
--header 'Content-Type: application/json' \
--header 'Authorization: ••••••' \
--data '[
  {"stmt" : "select AVG(total_points_per_game) from Top_NBA_Playoff_Scorers"}
]
'
```

Again, if we wanted to query data from a partitioned table using the TQL method, the command will fail:

```bash
curl --location 'http://192.168.50.206:8082/griddb/v2/myCluster/dbs/public/tql' \
--header 'Content-Type: application/json' \
--header 'Authorization: ••••••' \
--data '[
  {"name" : "pyIntPart2", "stmt" : "select *", "columns" : null}
]'
```

#### DML Update

We can also update rows by using the same rowkey or we can add new ones by using a fresh key. For example:

```bash
curl --location 'http://192.168.50.206:8082/griddb/v2/myCluster/dbs/public/sql/dml/update' \
--header 'Content-Type: application/json' \
--header 'Authorization: ••••••' \
--data '[
  {"stmt" : "INSERT INTO pyIntPart2(date, value, temp) VALUES (NOW(), '\''blog_test'\'', '\''cold'\'')"}
]
'
```

Again, the nice thing about using SQL here instead of the old TQL is that we can use the special GridDB Time Series SQL commands such as `NOW()`. You can read more about those commands in the docs: [https://docs.griddb.net/sqlreference/sql-commands-supported/#time-functions](https://docs.griddb.net/sqlreference/sql-commands-supported/#time-functions).

If we tried to use the `NOW()` command with the TQL version of an insert, it fails:

```bash
curl --location --request PUT 'http://192.168.50.206:8082/griddb/v2/myCluster/dbs/public/containers/pyIntPart2/rows' \
--header 'Content-Type: application/json' \
--header 'Authorization: ••••••' \
--data '[
 [NOW(), "failure","hot"]
]'
```

This command will fail.

### SQL DCL (Data Control Language)

The SQL Data Control Language is mostly concerned with rights/permissions for your database. This one isn't as exciting but it can be useful if managing many databases or users. For eexample, if you create a new database and a new user, you can grant that user access to that DB with the Web API.

```bash
curl --location 'http://192.168.50.206:8082/griddb/v2/myCluster/dbs/public/sql/dcl' \
--header 'Content-Type: application/json' \
--header 'Authorization: ••••••' \
--data '[
 {"stmt" : "REVOKE all on testing1 from israel"},
 {"stmt" : "GRANT all on testing1 to israel"}
]'
```

And you can verify this by using the GridDB CLI tool

```bash
gs[public]> showuser israel
Name     : israel
Type     : General User
GrantedDB: public
           testing1         ALL
```

