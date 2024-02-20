With the rollout of [GridDB v5.5](https://github.com/griddb/griddb), there have been some updates to the performance of SQL searches with joins, optimizations for partitioned indexes, and batch updates for SQL. All of the new features are there to help with the performance of your GridDB server, and batch updates especially provide the ability to update your GridDB containers with SQL in batches, which opens up a clear pathway for some optimizations in your applications. In this article, we will showcase how to conduct some SQL updates in batches through the lens of a simple benchmark to see how much faster (or not!) it is to run these commands in batches.  And then at the end, we will also compared it to TQL which generally operates much more quickly in GridDB due to loads of optimizations under the hood. We will also lightly discuss the other features.

## Getting Starts (Prereqs)

To follow along, you can clone the git repository from here: [GitHub]()

```bash
$ git clone -- branch sql_batch
```

And then you can run a `mvn install` and then run the resulting `.jar` file like so: `java -jar target/SqlBatch.jar`.

Alternatively, you can run this simple project with docker. 

```bash
$ docker build -t sql-batch .
$ docker run sql-batch --network=host 
```

Note: the commands above assume you are already running a GridDB server on your host machine with the default configs set. If you would like to run GridDB in a docker container, you will need to adjust how you are running this project.

Afterwards, you can feel free to drop into the [GridDB CLI](https://github.com/griddb/cli) and check the results of the operations as well.

## SQL Cost-Based Optimization 

Prior to the release of v5.5, SQL queries which required SQL joins had to be optmized solely by the user/developer who was forging those queries. For example, if you were making a query, the order in which you made your query would be the order in which the tables were joined to give you the results of the search -- this means that you needed to be purposeful in the way the query was worded and formed to be as efficient as possible. But with the release of v5.5, now there is a new setting called cost-based optimization (which is set to true by default, but can be turned off in the `gs_cluster.json` file). Note: if you are interested in turning this feature off, set `/sql/costBasedJoin` to false in the cluster config file. If you set the cost-based join to false, it will instead use what is called a rule-based join.

The cost-based method will do all optimizations automatically for you under the hood so that you aren't required to know all of the best optimizations off-hand and are no longer required to expend the effort into crafting efficient queries with every single call. The old method, the rule-based one, required the optimizations to be done on the side of the developer, who would be required to know off-hand the best ways to optimize table joins.

Under the hood, the cost-based method is constantly figuring out which queries' results will have more rows and will adjust accordingly. As an example, if you had the following query: 

`FROM A, C, B WHERE A.x>=C.z AND C.z>=B.y AND B.y=A.x`

Instead of joining in order in which it was written (rule-based), it will instead ascertain that the relationship between A and B is high, so it will join A with B and then the result of that with C.

Another method of optimizing is to find which table is more filtered (ie will have a smaller amount of rows in the results) and to join those first. So here's another example:

`FROM A, B, C WHERE A.x=C.z AND C.z=B.y AND A.x IN (1, 2) AND B.x IN (1, 2, 3)`

Here, table A has much more filtering into a narrower result, meaning it will be joined first.

### Hints

And though the cost-based optimizations setting is set to on by default, there is another method of optimizing your queries. Instead of leaving this setting on, you could also turn it off and instead use the `hint` system (Note: you can use the hints even if cost-based optimizations is kept to `true`).

The hint system allows you to set special markers in your queries to indicate that you are giving the system a hint of how to conduct the query. To do so, you need to set a comment (indicated with special reserved characters : `/*+`) RIGHT before or after the `SELECT` statement like so: 

```sql
/*+
Leading(t3 t2 t1)
 */
SELECT *
  FROM t1, t2, t3
    ON t1.x = t2.y and t2.y = t3.z
  ORDER BY t1.x
  LIMIT 10;
```
In this example, the hint `Leading` tells our query the order in which the table joins should be conducted. There are many more types of hints which can be utilized for this

## Optimization of Index Joins Related to Partitioning Tables

Before version 5.5, sometimes index joins would not occur if the underlying tables had too many partitions, resulting in full table scans. With this update, even if a table has many partitions or different types, plans that perform index joins will still run as expected.

## Benchmarking SQL Inserts

And with that out of the way, let's get started.

First, because we want to see how long inserting rows of data with SQL will take, we will be using [GridDB's JDBC Connector](https://github.com/griddb/jdbc/tree/master) to make SQL calls from within a simple Java program. We will insert 10,000 rows of blob data; first with SQL singular insert (one row a time), then with SQL batch update/insert (1,000 rows at a time), and then finally with TQL (one row at a time). So in order, we must: make a connection to our GridDB server via JDBC, then we must drop/create the appropriate containers, then we must insert our rows of data, and finally we must print out how long each operation took. 

### Connecting to GridDB via JDBC

For this part, we must have the GridDB JDBC connector installed. If you are following with the project and using maven, you will see it listed as a dependency (of course, along with GridDB Server): 

```bash
      <dependency>
            <groupId>com.github.griddb</groupId>
            <artifactId>gridstore-jdbc</artifactId>
            <version>5.5.0</version>
        </dependency>
```

With JDBC, we make our connection with the appropriate JDBC URL. Here is the code to build that out in Java: 

```java
			String notificationMember = "127.0.0.1:20001";
			String clusterName = "myCluster";
			String databaseName = "public";
			String username = "admin";
			String password = "admin";
			String applicationName = "SampleJDBC";

			String encodeClusterName = URLEncoder.encode(clusterName, "UTF-8");
			String encodeDatabaseName = URLEncoder.encode(databaseName, "UTF-8");

			String jdbcUrl = "jdbc:gs://" + notificationMember + "/" + encodeClusterName + "/" + encodeDatabaseName;

			Properties prop = new Properties();
			prop.setProperty("user", username);
			prop.setProperty("password", password);
			prop.setProperty("applicationName", applicationName);

			Connection con = DriverManager.getConnection(jdbcUrl, prop);
			Statement stmt = con.createStatement();
```

Here, we are assuming a default GridDB server installed on your local machine. If you are using a remote server, or perhaps docker, your notificationMember may differ.

If successful, you have a working `stmt` variable which we can use to make SQL statements to our GridDB server.

## Creating Tables

Next, we want to create our tables. We will keep it simple and have just two columns, the column rowkey(id) and the data (as datatype blob).

Let's create our SQL statements.

```java
			stmt.executeUpdate("DROP TABLE IF EXISTS SQL_Single_Input");
			stmt.executeUpdate("CREATE TABLE SQL_Single_Input ( id integer PRIMARY KEY, data blob )");
			PreparedStatement pstmt = con.prepareStatement("INSERT INTO SQL_Single_Input(id, data) VALUES(?, ?)");

            stmt.executeUpdate("DROP TABLE IF EXISTS SQL_Batch_Input");
			stmt.executeUpdate("CREATE TABLE SQL_Batch_Input ( id integer PRIMARY KEY, data blob )");
			PreparedStatement pstmtBatch = con.prepareStatement("INSERT INTO SQL_Batch_Input(id, data) VALUES(?, ?)");

            stmt.executeUpdate("DROP TABLE IF EXISTS SampleTQL_BlobData");
			stmt.executeUpdate("CREATE TABLE SampleTQL_BlobData ( id integer PRIMARY KEY, data blob )");
```

When we execute these, our tables will dropped and then re-made again, waiting for data to be inserted.

## Inserting Data

Our blob data will be the same across all inserts to make it more of an apples-to-apples sort of comparison. 

```java
			byte[] b = new byte[1000];
			new Random().nextBytes(b);
			SerialBlob serialBlob = new SerialBlob(b);
```

Now let's insert the data: 

```java
// SQL Single Update
			Long startTime = System.nanoTime();
			for (int i = 1; i <= 10000; i++) {
				pstmt.setInt(1, i);
				pstmt.setBlob(2, serialBlob);
				pstmt.executeUpdate();
			}
			Long endTime = System.nanoTime();
			Long duration = (endTime - startTime)/ 1000000; // milliseconds
			System.out.println("singular SQL took: " + Long.toString(duration) + " milliseconds");
			pstmt.close();
```

And now we get our result: `singular SQL took: 5462 milliseconds`

Let's see if batch updating can do a better job

```java
// SQL Batch Update
			startTime = System.nanoTime();
			for (int i = 1; i <= 10000; i++) {
				pstmtBatch.setInt(1, i);
				pstmtBatch.setBlob(2, serialBlob);
				pstmtBatch.addBatch();

				if (i % 1000 == 0) {
					@SuppressWarnings("unused")
					int[] cnt = pstmtBatch.executeBatch();
				}
				
			}
			endTime = System.nanoTime();
			duration = (endTime - startTime)/ 1000000;
			System.out.println("add batch SQL took: " + Long.toString(duration) + " milliseconds");
```

For this snippet of code, we are inserting 1,000 rows of data ten times. The result: `add batch SQL took: 4116 milliseconds`. This gives us an increase of about ~33% in speed. Not bad considering how effortless how simple it is to implement from the developer side. 

## Inserting Data with TQL (NoSQL)

Lastly, let's try this same basic operation but from the TQL side. First, we must make a new connection with our GridDB server, as our previous connection was strictly through JDBC (SQL).

```java
            // Tql.java
            Properties props = new Properties();
            props.setProperty("notificationMember", "127.0.0.1:10001");
            props.setProperty("clusterName", "myCluster");
            props.setProperty("user", "admin"); 
            props.setProperty("password", "admin");
            store = GridStoreFactory.getInstance().getGridStore(props);
```

The port number changes from `20001` to `10001` and we need a few less details to make our connection.

Once connected, we can make our data and insert our rows; we will be using the same blob data as before.


```java
    // Tql.java
	protected static void runSinglePut(SerialBlob blob) throws GSException, SerialException, SQLException {
		Tql tql = new Tql();
		
		Collection<String, Row> col = tql.store.getCollection("SampleTQL_BlobData");	
		col.setAutoCommit(true);
 
		for (int i = 1; i <= 10000; i++) {
			Row row;
			row = col.createRow();
			row.setInteger(0, i);
			row.setBlob(1, blob);
			col.put(row);
		}	
	}

    //main()
    startTime = System.nanoTime();
    Tql.runSinglePut(serialBlob);
    endTime = System.nanoTime();
    duration = (endTime - startTime)/ 1000000;
    System.out.println("TQL took: " + Long.toString(duration) + " milliseconds");
```

And here again are all of the results (ran a separate time for varying results):

```bash
singular SQL took: 5317 milliseconds
add batch SQL took: 4353 milliseconds
TQL took: 941 milliseconds
```

And just for fun, let's try a `multiput` in Java: 

```java
	protected static void runMulti (SerialBlob blob) throws GSException, SerialException, SQLException {
		final Map<String, List<Row>> rowListMap = new HashMap<String, List<Row>>();
		
		Collection<String, Row> col = store.getCollection("MultiPutTQL_Blobdata");	
		col.setAutoCommit(true);

		List<Row> rows = new ArrayList<>();
		for (int i = 1; i <= 10000; i++) {
			Row row;
			row = col.createRow();
			row.setInteger(0, i);
			row.setBlob(1, blob);
			rows.add(row);
		}
		rowListMap.put("MultiPutTQL_Blobdata", rows);
		store.multiPut(rowListMap);
	}
```

And the results: `Multi Put took: 213 milliseconds`. Although the result is much faster than our previous tries, it is not surprising. Here we are inserting all 10,000 rows in one go, on top of the fact that TQL is already the faster operation, results in a much faster overall speed.

## Conclusion

In this article, we have showcased GridDB's new SQL Batch update feature and directly compared it against single sql input, TQL Single Input, and TQL Multi put. 
