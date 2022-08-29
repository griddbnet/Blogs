You've ingested a set of timeseries data into GridDB and want to see aggregates across indidvidual time periods (also called windows or buckets) within the data set? GridDB's TIME_SAMPLING function allows you to perform time weighted averages but what about counts or the minimum or maximum values in each time period? For example, you want to find the maximum value of a column every day or the number of samples every hour. GridDB's multi-query function along with your programming language's time, date, and calendar functions allows you to do just that.

In this blog, we'll cover not only using GridDB's TIME_SAMPLE TQL function but also build a generic function that can fetch multiple aggregates from the NYC Taxi Data data set.

## Ingest Parquet

In our past (NYC Taxi Data blog)[], the data was available as a CSV which is easy to parse. Now, the NY Taxi Comission releases the data in Parquet format which has its advantages but is more difficult to parse.

First, we need to include the many libraries required to use the Apache Parquet Library: parqet-avro, parqet-hadoop, parqet-format, hadoop-common, hadoop-client. Since we're using Maven, we add the following to our dependencies:

<div class="clipboard">
<pre><code class="language-java">&lt;dependency&gt;
    &lt;groupId&gt;org.apache.parquet&lt;/groupId&gt;
    &lt;artifactId&gt;parquet-hadoop&lt;/artifactId&gt;
    &lt;version&gt;1.9.0&lt;/version&gt;
&lt;/dependency&gt;
&lt;dependency&gt;
    &lt;groupId&gt;org.apache.parquet&lt;/groupId&gt;
    &lt;artifactId&gt;parquet-format&lt;/artifactId&gt;
    &lt;version&gt;2.9.0&lt;/version&gt;
&lt;/dependency&gt;
&lt;dependency&gt;
    &lt;groupId&gt;org.apache.parquet&lt;/groupId&gt;
    &lt;artifactId&gt;parquet-avro&lt;/artifactId&gt;
        &lt;version&gt;1.9.0&lt;/version&gt;
&lt;/dependency&gt;
&lt;dependency&gt;
  &lt;groupId&gt;org.apache.hadoop&lt;/groupId&gt;
  &lt;artifactId&gt;hadoop-common&lt;/artifactId&gt;
  &lt;version&gt;2.10.0&lt;/version&gt;
&lt;/dependency&gt;
&lt;dependency&gt;
    &lt;groupId&gt;org.apache.hadoop&lt;/groupId&gt;
    &lt;artifactId&gt;hadoop-client&lt;/artifactId&gt;
    &lt;version&gt;2.10.1&lt;/version&gt;
    &lt;scope&gt;provided&lt;/scope&gt;
&lt;/dependency&gt;</code></pre>
</div>


Now in our Java source we can read the parquet file and iterate through its groups of rows. 

<div class="clipboard">
<pre><code class="language-java">Path path = new Path("yellow_tripdata_2021-01.parquet");
Configuration conf = new Configuration();

try {
    ParquetMetadata readFooter = ParquetFileReader.readFooter(conf, path, ParquetMetadataConverter.NO_FILTER);
    MessageType schema = readFooter.getFileMetaData().getSchema();
    ParquetFileReader r = new ParquetFileReader(conf, path, readFooter);

    PageReadStore pages = null;
    try {
        while (null != (pages = r.readNextRowGroup())) {
            final long rows = pages.getRowCount();
            System.out.println("Number of rows: " + rows);

            final MessageColumnIO columnIO = new ColumnIOFactory().getColumnIO(schema);
            final RecordReader recordReader = columnIO.getRecordReader(pages, new GroupRecordConverter(schema));
            for (int i = 0; i < rows; i++) {
                final Group g = (Group)recordReader.read();
                writeGroup(ts, g);

            }
        }
    } finally {
        r.close();
    }
} catch (IOException e) {
    System.out.println("Error reading parquet file.");
    e.printStackTrace();
}</code></pre>
</div>

The writeGroup function is called for each group of rows and actually does the writing and populates and writes an instance of our TaxiTrip class with the data in each row.


<div class="clipboard">
<pre><code class="language-java">private static void writeGroup(TimeSeries ts, Group g) throws GSException {

    int fieldCount = g.getType().getFieldCount();
    int valueCount = g.getFieldRepetitionCount(0);
    for (int index = 0; index < valueCount; index++) {
        TaxiTrip r = new TaxiTrip();
        for (int field = 0; field < fieldCount; field++) {
        
            try {
            Type fieldType = g.getType().getType(field);
            String fieldName = fieldType.getName();

            if (fieldType.isPrimitive()) {
                switch(fieldName) {
                    case "tpep_pickup_datetime":
                        r.tpep_pickup_datetime = new Date(g.getLong(field, index)/1000);
                        break;
                    /* .... snip ... */
                    case "fare_amount":
                        r.fare_amount = (float)g.getDouble(field, index);
                        break;
                    /* .... snip .... */
                    default:
                        System.out.println("Unknown field: "+fieldName+" value="+g.getValueToString(field, index));
                }
            } 
            } catch (Exception e) {
            }
        }
        ts.put(r);
    }
}</code></pre>
</div>

# GridDB's TIME_SAMPLING Function 

GridDB's TIME_SAMPLING function returns a linear interpolation of the column values within each window. It has the following function signature:

<div class="clipboard">
<pre><code class="language-sh">TIME_SAMPLING(column, start, end, window_count, window_size)</code></pre>
</div>

The code simply executes the query and iterates through each window returned.


<div class="clipboard">
<pre><code class="language-java">public static void timeSampling(GridStore store, String column, Date start, Date end, String windowstr) throws GSException {

    TimeZone tz = TimeZone.getTimeZone("UTC");
    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:00'Z'"); 
    df.setTimeZone(tz);

    Container<?, Row> ts = store.getContainer("NYC_TaxiTrips");
    String querystr = "select time_sampling("+column+", TIMESTAMP('"+df.format(start)+"'), TIMESTAMP('"+df.format(end) +"') , 1, "+windowstr+") "; 

    Query<Row> q = ts.query(querystr);
    RowSet<Row> rs = q.fetch();
    while (rs.hasNext()) {
        Row result = rs.next();
        System.out.println(result.getTimestamp(0)+"="+result.getDouble(10));
    }

}</code></pre>
</div>

Calling `timeSampling(store, "fare_amount", start, end, "DAY");` results in the following values:

    | Window | Sample |
    | --- | --- |
    |Fri Jan 01 00:00:00 GMT 2021 | 14.833333015441895|
    |Sat Jan 02 00:00:00 GMT 2021 | 8.55555534362793|
    |Sun Jan 03 00:00:00 GMT 2021 | 15.710000038146973|
    |Mon Jan 04 00:00:00 GMT 2021 | 25.31818199157715|
    |Tue Jan 05 00:00:00 GMT 2021 | 22.024999618530273|
    |Wed Jan 06 00:00:00 GMT 2021 | 9.0|
    |Thu Jan 07 00:00:00 GMT 2021 | 43.5|
    |Fri Jan 08 00:00:00 GMT 2021 | 6.0|
    |Sat Jan 09 00:00:00 GMT 2021 | 28.950000762939453|
    |Sun Jan 10 00:00:00 GMT 2021 | 18.450000762939453|
    |Mon Jan 11 00:00:00 GMT 2021 | 41.75|
    |Tue Jan 12 00:00:00 GMT 2021 | 6.5|
    |Wed Jan 13 00:00:00 GMT 2021 | 17.710525512695312|
    |Thu Jan 14 00:00:00 GMT 2021 | 10.8125|
    |Fri Jan 15 00:00:00 GMT 2021 | 52.0|
    |Sat Jan 16 00:00:00 GMT 2021 | 28.200000762939453|
    |Sun Jan 17 00:00:00 GMT 2021 | 9.977272987365723|
    |Mon Jan 18 00:00:00 GMT 2021 | 9.100000381469727|
    |Tue Jan 19 00:00:00 GMT 2021 | 25.66666603088379|
    |Wed Jan 20 00:00:00 GMT 2021 | 34.349998474121094|
    |Thu Jan 21 00:00:00 GMT 2021 | 5.5|
    |Fri Jan 22 00:00:00 GMT 2021 | 7.472727298736572|
    |Sat Jan 23 00:00:00 GMT 2021 | 10.5|
    |Sun Jan 24 00:00:00 GMT 2021 | 17.0|
    |Mon Jan 25 00:00:00 GMT 2021 | 21.0|
    |Tue Jan 26 00:00:00 GMT 2021 | 15.852941513061523|
    |Wed Jan 27 00:00:00 GMT 2021 | 7.984375|
    |Thu Jan 28 00:00:00 GMT 2021 | 42.83333206176758|
    |Fri Jan 29 00:00:00 GMT 2021 | 9.083333015441895|
    |Sat Jan 30 00:00:00 GMT 2021 | 11.892857551574707|
    |Sun Jan 31 00:00:00 GMT 2021 | 14.371428489685059|

While TIME_SAMPLING is useful, it has has some limitations since it only returns a linear interpolation for each window, each of which is typically similar to the average value (like medians are similar) but is not a true average. Likewise, you can't find the number of rows in the time window or the minimum or maximum values. 

## Windowed Aggregations with Multi-Query

To find any aggregation for a set of timeseries windows, we can use GridDB's multi-query function. This runs the aggregation for each time window seperately. You might think performing the operations like this would be slow, but by using multi-query we can optimize sending the queries to GridDB and the data retrieval. The database performs the same operations as it would if the function were built-in.

The first steps in our generic windowed aggregation function is to open the containeer and create a query for each window and add it to the list of queries that will be executed by GridDB. Each query is built by using Java's Calendar class and incrementing the interval by the specified Calendar ENUM value such as Calendar.HOUR or Calendar.DATE.

<div class="clipboard">
<pre><code class="language-java">public static void windowAggregation(GridStore store, String aggregation, Date start, Date end, int windowsize) throws GSException {
    Calendar c = Calendar.getInstance();
    ArrayList<Query<AggregationResult>> queryList = new ArrayList<Query<AggregationResult>>();
    ArrayList<Date> dates = new ArrayList<Date>();
    Container<?, Row> ts = store.getContainer("NYC_TaxiTrips");

    c.setTime(start);
    Date interval = c.getTime();

    do {
        c.add(windowsize, 1);
        System.out.println("interval="+interval);
        String windowquery = "select "+aggregation+" where tpep_pickup_datetime > TO_TIMESTAMP_MS("+interval.getTime()+") and tpep_pickup_datetime < TO_TIMESTAMP_MS("+c.getTime().getTime()+")"; 
        Query<AggregationResult> q = ts.query(windowquery, AggregationResult.class);
        dates.add(interval);
        queryList.add(q);
        interval = c.getTime();
    } while (interval.getTime() <= end.getTime());</code></pre>
</div>

After we have our list of queries, we can execute them and iterate through the results.

<div class="clipboard">
<pre><code class="language-java">    store.fetchAll(queryList);

    for (int i = 0; i < queryList.size(); i++) {
        Query<AggregationResult> query = queryList.get(i);
        RowSet<AggregationResult> rs = query.getRowSet();
        while (rs.hasNext()) {
            AggregationResult result = rs.next();
            double value = result.getDouble();
            if (value != 0)
                System.out.println(dates.get(i)+"= "+ value);
        }
    }


}</code></pre>
</div>

Calling `windowAggregate(store, "avg(fare_amount)", start, end, Calendar.DATE);` results in the following results:

    | Window | avg(fare_amount) |
    | --- | --- |
    |Fri Jan 01 00:00:00 GMT 2021 | 13.851391098873778|
    |Sat Jan 02 00:00:00 GMT 2021 | 13.770499047323876|
    |Sun Jan 03 00:00:00 GMT 2021 | 15.249551398785178|
    |Mon Jan 04 00:00:00 GMT 2021 | 13.473628681590766|
    |Tue Jan 05 00:00:00 GMT 2021 | 12.676972567160476|
    |Wed Jan 06 00:00:00 GMT 2021 | 12.243332028212064|
    |Thu Jan 07 00:00:00 GMT 2021 | 12.464050631310585|
    |Fri Jan 08 00:00:00 GMT 2021 | 12.091422930601727|
    |Sat Jan 09 00:00:00 GMT 2021 | 12.537748556275371|
    |Sun Jan 10 00:00:00 GMT 2021 | 13.496787364093032|
    |Mon Jan 11 00:00:00 GMT 2021 | 12.350597800281259|
    |Tue Jan 12 00:00:00 GMT 2021 | 12.189052448708432|
    |Wed Jan 13 00:00:00 GMT 2021 | 11.970959482137403|
    |Thu Jan 14 00:00:00 GMT 2021 | 12.338237139653735|
    |Fri Jan 15 00:00:00 GMT 2021 | 12.24978189382601|
    |Sat Jan 16 00:00:00 GMT 2021 | 12.348960280360648|
    |Sun Jan 17 00:00:00 GMT 2021 | 12.987822185285506|
    |Mon Jan 18 00:00:00 GMT 2021 | 12.66304436182826|
    |Tue Jan 19 00:00:00 GMT 2021 | 12.19362357336725|
    |Wed Jan 20 00:00:00 GMT 2021 | 11.760170207452589|
    |Thu Jan 21 00:00:00 GMT 2021 | 12.153647415269047|
    |Fri Jan 22 00:00:00 GMT 2021 | 12.026541242751565|
    |Sat Jan 23 00:00:00 GMT 2021 | 11.73361113486781|
    |Sun Jan 24 00:00:00 GMT 2021 | 12.876742517934895|
    |Mon Jan 25 00:00:00 GMT 2021 | 12.229045667216711|
    |Tue Jan 26 00:00:00 GMT 2021 | 11.716670845330835|
    |Wed Jan 27 00:00:00 GMT 2021 | 11.823579132291002|
    |Thu Jan 28 00:00:00 GMT 2021 | 11.842199614451049|
    |Fri Jan 29 00:00:00 GMT 2021 | 11.784085302551507|
    |Sat Jan 30 00:00:00 GMT 2021 | 11.843539460445053|
    |Sun Jan 31 00:00:00 GMT 2021 | 12.406643581985854|


We can also see the number of rows by calling `windowAggregate(store, "count(*)", start, end, Calendar.DATE)`:

    | Window | COUNT(*) |
    | --- | --- |
    |Fri Jan 01 00:00:00 GMT 2021 | 20430.0|
    |Sat Jan 02 00:00:00 GMT 2021 | 25429.0|
    |Sun Jan 03 00:00:00 GMT 2021 | 20909.0|
    |Mon Jan 04 00:00:00 GMT 2021 | 30733.0|
    |Tue Jan 05 00:00:00 GMT 2021 | 31667.0|
    |Wed Jan 06 00:00:00 GMT 2021 | 32965.0|
    |Thu Jan 07 00:00:00 GMT 2021 | 33306.0|
    |Fri Jan 08 00:00:00 GMT 2021 | 33628.0|
    |Sat Jan 09 00:00:00 GMT 2021 | 28626.0|
    |Sun Jan 10 00:00:00 GMT 2021 | 23034.0|
    |Mon Jan 11 00:00:00 GMT 2021 | 31583.0|
    |Tue Jan 12 00:00:00 GMT 2021 | 33201.0|
    |Wed Jan 13 00:00:00 GMT 2021 | 33633.0|
    |Thu Jan 14 00:00:00 GMT 2021 | 34580.0|
    |Fri Jan 15 00:00:00 GMT 2021 | 34706.0|
    |Sat Jan 16 00:00:00 GMT 2021 | 28094.0|
    |Sun Jan 17 00:00:00 GMT 2021 | 24373.0|
    |Mon Jan 18 00:00:00 GMT 2021 | 26830.0|
    |Tue Jan 19 00:00:00 GMT 2021 | 33525.0|
    |Wed Jan 20 00:00:00 GMT 2021 | 32902.0|
    |Thu Jan 21 00:00:00 GMT 2021 | 34652.0|
    |Fri Jan 22 00:00:00 GMT 2021 | 35478.0|
    |Sat Jan 23 00:00:00 GMT 2021 | 29830.0|
    |Sun Jan 24 00:00:00 GMT 2021 | 23776.0|
    |Mon Jan 25 00:00:00 GMT 2021 | 32064.0|
    |Tue Jan 26 00:00:00 GMT 2021 | 31969.0|
    |Wed Jan 27 00:00:00 GMT 2021 | 34148.0|
    |Thu Jan 28 00:00:00 GMT 2021 | 35670.0|
    |Fri Jan 29 00:00:00 GMT 2021 | 35219.0|
    |Sat Jan 30 00:00:00 GMT 2021 | 28349.0|
    |Sun Jan 31 00:00:00 GMT 2021 | 23674.0|

## Conclusion

As shown, it's easy to perform time window aggregation with GridDB. The complete source code for this blog can be found on GridDB.net's GitHub page [here](). 
