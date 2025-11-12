package net.griddb;

import java.util.Properties;
import java.util.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.sql.Timestamp;
import com.toshiba.mwcloud.gs.*;

class GridDB {

    public GridStore store = null;

    // Connect to GridDB's NoSQL Interface
    public GridDB() throws GSException {
        try {
            Properties props = new Properties();

            String notificationProvider = System.getenv("GRIDDB_NOTIFICATION_PROVIDER");
            String clusterName = System.getenv("GRIDDB_CLUSTER_NAME");
            String username = System.getenv("GRIDDB_USERNAME");
            String password = System.getenv("GRIDDB_PASSWORD");
            String database = System.getenv("GRIDDB_DATABASE");

            props.setProperty("notificationProvider", notificationProvider);
            props.setProperty("clusterName", clusterName);
            props.setProperty("user", username);
            props.setProperty("password", password);
            props.setProperty("database", database);

            store = GridStoreFactory.getInstance().getGridStore(props);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Create GridDB Container based on Device class
    public void CreateContainer(String containerName) {
        System.out.println("Creating Container");
        try {
            // Device class is our schema for this container
            store.putTimeSeries(containerName, Device.class);
        } catch (GSException gse) {
            gse.printStackTrace();
        }
        System.out.println("Container Created");
    }

    // Pushing a row of hardcoded data to our newly created
    // container
    public void WriteToContainer(String containerName) {
        System.out.println("Writing some arbitrary data to container: " + containerName);
        try {
            TimeSeries<Device> ts = store.putTimeSeries(containerName, Device.class);
            // Must set auto commit to false if you intend to manually commit after you are
            // finished
            ts.setAutoCommit(false);

            Device device = new Device();
            device.ts = new Timestamp(System.currentTimeMillis());
            device.co = 1.2;
            device.humidity = 2.3;
            device.light = true;
            device.lpg = 2.2;
            device.motion = true;
            device.smoke = 0.02;
            device.temp = 99.19;

            System.out.println("Inserting data to GridDB");
            System.out.println(device.toString());
            ts.put(device);
            ts.commit();
        } catch (GSException gse) {
            System.out.println("Error commiting");
            gse.printStackTrace();
        }

    }

    // Print all contents of Container
    // In this case, a timeseries container of class Device
    public void DumpContainer(String containerName) {
        System.out.printf("Reading container %s\n", containerName);

        try {
            TimeSeries<Device> ts = store.putTimeSeries(containerName, Device.class);
            ts.setAutoCommit(false);

            Query<Device> query = ts.query("SELECT *");
            RowSet<Device> rs = query.fetch(true);

            while (rs.hasNext()) {
                Device d = rs.next();
                System.out.println(d.toString());
            }
        } catch (GSException gse) {
            gse.printStackTrace();
        }

    }

    // Example showcasing pushing many rows at once
    public void MultiPut() {
        try {
            Map<String, List<Row>> paramMap = new HashMap<String, List<Row>>();
            createContainer(store);
            {
                String containerName = "SampleJava_MultiPut1";
                Container<Integer, Row> container = store.getContainer(containerName);
                if (container == null) {
                    throw new Exception("Container not found.");
                }

                String[] nameList = { "notebook PC", "desktop PC", "keyboard", "mouse", "printer" };
                int[] numberList = { 55, 81, 39, 72, 14 };

                List<Row> rowList = new ArrayList<Row>();
                for (int i = 0; i < nameList.length; i++) {
                    Row row = container.createRow();
                    row.setInteger(0, (i + 1));
                    row.setString(1, nameList[i]);
                    row.setInteger(2, numberList[i]);
                    rowList.add(row);
                }
                paramMap.put(containerName, rowList);
            }
            store.multiPut(paramMap);

        } catch (GSException e) {
            Map<String, String> param = e.getParameters();
            for (Map.Entry<String, String> entry : param.entrySet()) {
                System.out.println(entry.getKey() + ":" + entry.getValue());
            }

            e.printStackTrace();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Example reading many rows at once
    public void MultiGet() {

        try {
            createContainerPutRow(store);
            Map<String, RowKeyPredicate<Integer>> predMap = new HashMap<String, RowKeyPredicate<Integer>>();
            {
                RowKeyPredicate<Integer> predicate = RowKeyPredicate.create(Integer.class);
                predicate.add(0);
                predMap.put("SampleJava_MultiGet1", predicate);
            }
            {
                RowKeyPredicate<Integer> predicate = RowKeyPredicate.create(Integer.class);
                predicate.add(2);
                predicate.add(4);
                predMap.put("SampleJava_MultiGet2", predicate);
            }

            Map<String, List<Row>> outMap = store.multiGet(predMap);

            System.out.println("MultiGet");

            for (Map.Entry<String, List<Row>> entry : outMap.entrySet()) {
                System.out.println("containerName=" + entry.getKey());

                for (Row row : entry.getValue()) {
                    int id = row.getInteger(0);
                    String name = row.getString(1);
                    int count = row.getInteger(2);

                    System.out.println("    id=" + id + " name=" + name + " count=" + count);
                }
            }

        } catch (GSException e) {
            Map<String, String> param = e.getParameters();
            for (Map.Entry<String, String> entry : param.entrySet()) {
                System.out.println(entry.getKey() + ":" + entry.getValue());
            }

            e.printStackTrace();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // helper function for multiput
    private static void createContainer(GridStore store) throws Exception {
        {
            ContainerInfo containerInfo = new ContainerInfo();
            List<ColumnInfo> columnList = new ArrayList<ColumnInfo>();
            columnList.add(new ColumnInfo("id", GSType.INTEGER));
            columnList.add(new ColumnInfo("productName", GSType.STRING));
            columnList.add(new ColumnInfo("count", GSType.INTEGER));
            containerInfo.setColumnInfoList(columnList);
            containerInfo.setRowKeyAssigned(true);

            store.putCollection("SampleJava_MultiPut1", containerInfo, false);

            System.out.println("Create Collection name=SampleJava_MultiPut1");
        }
        {
            ContainerInfo containerInfo = new ContainerInfo();
            List<ColumnInfo> columnList = new ArrayList<ColumnInfo>();
            columnList.add(new ColumnInfo("date", GSType.TIMESTAMP));
            columnList.add(new ColumnInfo("value", GSType.DOUBLE));
            containerInfo.setColumnInfoList(columnList);
            containerInfo.setRowKeyAssigned(true);

            store.putTimeSeries("SampleJava_MultiPut2", containerInfo, false);

            System.out.println("Create TimeSeries name=SampleJava_MultiPut2");
        }
    }

    // help function for multiget
    private static void createContainerPutRow(GridStore store) throws Exception {
        ContainerInfo containerInfo = new ContainerInfo();
        containerInfo.setType(ContainerType.COLLECTION);
        List<ColumnInfo> columnList = new ArrayList<ColumnInfo>();
        columnList.add(new ColumnInfo("id", GSType.INTEGER));
        columnList.add(new ColumnInfo("productName", GSType.STRING));
        columnList.add(new ColumnInfo("count", GSType.INTEGER));
        containerInfo.setColumnInfoList(columnList);
        containerInfo.setRowKeyAssigned(true);

        {
            Container<?, Row> container = store.putContainer("SampleJava_MultiGet1", containerInfo, false);

            String[] nameList = { "notebook PC", "desktop PC", "keyboard", "mouse", "printer" };
            int[] numberList = { 108, 72, 25, 45, 62 };
            List<Row> rowList = new ArrayList<Row>();
            for (int i = 0; i < nameList.length; i++) {
                Row row = container.createRow();
                row.setInteger(0, i);
                row.setString(1, nameList[i]);
                row.setInteger(2, numberList[i]);
                rowList.add(row);
            }
            container.put(rowList);

            System.out.println("Create Collection name=SampleJava_MultiGet1");
        }
        {
            Container<?, Row> container = store.putContainer("SampleJava_MultiGet2", containerInfo, false);

            String[] nameList = { "notebook PC", "desktop PC", "keyboard", "mouse", "printer" };
            int[] numberList = { 50, 11, 208, 23, 153 };
            List<Row> rowList = new ArrayList<Row>();
            for (int i = 0; i < nameList.length; i++) {
                Row row = container.createRow();
                row.setInteger(0, i);
                row.setString(1, nameList[i]);
                row.setInteger(2, numberList[i]);
                rowList.add(row);
            }
            container.put(rowList);

            System.out.println("Create Collection name=SampleJava_MultiGet2");
        }

    }

    /**
     * Example 1: Simple Aggregation (AVG)
     * Get the average temperature across all records.
     */
    public void queryAverageTemperature(String containerName) throws GSException {
        System.out.println("Geting " + containerName);
        TimeSeries<Device> container = store.getTimeSeries(containerName, Device.class);
        if (container == null) {
            System.out.println("CONTAINER IS NULL");
        }

        String tql = "SELECT AVG(temp)";
        System.out.println("Running: " + tql);

        Query<AggregationResult> query = container.query(tql, AggregationResult.class);
        RowSet<AggregationResult> rs = query.fetch();

        if (rs.hasNext()) {
            AggregationResult result = rs.next();
            double avgTemp = result.getDouble();
            System.out.println("Average Temperature: " + avgTemp);
        }
    }

    /**
     * Example 2: Simple Aggregation (MAX)
     * Get the maximum humidity value.
     */
    public void queryMaxHumidity(String containerName) throws GSException {
        TimeSeries<Device> container = store.getTimeSeries(containerName, Device.class);

        String tql = "SELECT MAX(humidity)";
        System.out.println("Running: " + tql);

        Query<AggregationResult> query = container.query(tql, AggregationResult.class);
        RowSet<AggregationResult> rs = query.fetch();

        if (rs.hasNext()) {
            AggregationResult result = rs.next();
            double maxHumidity = result.getDouble();
            System.out.println("Maximum Humidity: " + maxHumidity);
        }
    }

    /**
     * Example 3: Simple Aggregation (COUNT)
     * Count the number of times motion was detected (motion = true).
     */
    public void queryMotionCount(String containerName) throws GSException {
        TimeSeries<Device> container = store.getTimeSeries(containerName, Device.class);

        String tql = "SELECT COUNT(*)";
        System.out.println("Running: " + tql);

        Query<AggregationResult> query = container.query(tql, AggregationResult.class);
        RowSet<AggregationResult> rs = query.fetch();

        if (rs.hasNext()) {
            AggregationResult result = rs.next();
            // COUNT always returns a Long
            long count = result.getLong();
            System.out.println("Total Rows: " + count);
        }
    }

    /**
     * Example 4: Time-Bound Aggregation (TIME_AVG)
     * Get the time-weighted average 'co' value for a specific 1-minute window.
     */
    public void queryTimeBoundAverage(String containerName) throws GSException {
        TimeSeries<Device> container = store.getTimeSeries(containerName, Device.class);

        // We use the timestamps from your data for this example
        String tql = "SELECT TIME_AVG(co) FROM " + containerName + " WHERE " +
                "ts >= TIMESTAMP('2020-07-12T00:01:30.000Z') AND " +
                "ts <= TIMESTAMP('2020-07-12T00:02:30.000Z')";

        System.out.println("Running: " + tql);

        Query<AggregationResult> query = container.query(tql, AggregationResult.class);
        RowSet<AggregationResult> rs = query.fetch();

        if (rs.hasNext()) {
            AggregationResult result = rs.next();
            double timeAvgCO = result.getDouble();
            System.out.println("Time-Weighted Avg CO (00:01:30 - 00:02:30): " + timeAvgCO);
        }
    }

    /**
     * Example 5: TIME_SAMPLING Aggregation (Time Bucketing)
     * Get average temperature in 1-minute buckets.
     */
    public void queryTimeBucketedAverages(String containerName) throws GSException {
        Container<?, Row> container = store.getContainer(containerName);

        // Get the average temperature, bucketing the results into 1-minute intervals
        // TQL format: SELECT TIME_SAMPLING(col, start, end, interval, unit)
        String tql = "SELECT TIME_SAMPLING(" +
                "temp, " +
                "TIMESTAMP('2020-07-12T00:01:00.000Z'), " + // Start time
                "TIMESTAMP('2020-07-12T00:20:00.000Z'), " + // End time
                "1, " + // 1 (the interval value)
                "MINUTE " + // MINUTE (the interval unit)
                ")";

        System.out.println("Running: " + tql);

        Query<Row> query = container.query(tql);
        RowSet<Row> rs = query.fetch();

        System.out.println("Time-Bucketed Average Temperatures:");
        while (rs.hasNext()) {
            Row resultRow = rs.next();

            // In a TIME_SAMPLING result, results are returned by index:
            // Index 0: the GROUP BY column (ts)
            // Index 1: the aggregation column (AVG(temp))
            Date timestamp = resultRow.getTimestamp(0);
            double avgTemp = resultRow.getDouble(1);

            System.out.println("  [" + timestamp + "] = " + avgTemp);
        }
        rs.close();
        query.close();
    }

}
