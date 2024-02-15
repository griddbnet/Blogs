With the rollout of [GridDB v5.5](https://github.com/griddb/griddb), there have been some updates to the performance of SQL searches with joins, optimizations for partitioned indexes, and finally, the subject of this article: batch updates for SQL. Being able to update your GridDB containers with SQL in batches opens up a clear pathway for some optimizations in your applications. In this article, we will showcase how to conduct some SQL updates in batches through the lens of a simple benchmark to see how much faster (or not!) it is to run these commands in batches. And then at the end, we will also compared it to TQL which generally operates much more quickly in GridDB due to loads of optimizations under the hood.

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
