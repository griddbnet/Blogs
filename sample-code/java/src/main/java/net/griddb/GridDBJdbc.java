package net.griddb;

import java.sql.*;
import java.util.Properties;
import java.net.URLEncoder;

public class GridDBJdbc {
    public Connection conn;

    public GridDBJdbc() {
        try {
            String notificationProvider = System.getenv("GRIDDB_NOTIFICATION_PROVIDER");
            String clusterName = System.getenv("GRIDDB_CLUSTER_NAME");
            String username = System.getenv("GRIDDB_USERNAME");
            String password = System.getenv("GRIDDB_PASSWORD");
            String database = System.getenv("GRIDDB_DATABASE");

            String encodeClusterName = URLEncoder.encode(clusterName, "UTF-8");
            String encodeDatabaseName = URLEncoder.encode(database, "UTF-8");
            String jdbcUrl = "jdbc:gs:///" + encodeClusterName + "/"
                    + encodeDatabaseName + "?notificationProvider=" + notificationProvider;

            Properties prop = new Properties();
            prop.setProperty("user", username);
            prop.setProperty("password", password);

            System.out.println(jdbcUrl);

            conn = DriverManager.getConnection(jdbcUrl, prop);

        } catch (Exception e) {
            System.out.println("Could not connect to GridDB via JDBC, exiting.");
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public void CreateTable(String containerName) throws SQLException {
        Statement stmt = conn.createStatement();
        String sqlCreate = String.format("CREATE TABLE IF NOT EXISTS %s (id integer, value string);", containerName);
        System.out.println(sqlCreate);
        stmt.executeUpdate(sqlCreate);

        String sqlUpdate = String.format(
                "INSERT INTO %s values " + " (0, 'test0'),(1, 'test1'),(2, 'test2'),(3, 'test3'),(4, 'test4')",
                containerName);
        System.out.println(sqlUpdate);
        stmt.executeUpdate(sqlUpdate);
    }

    public void DumpContainer(String containerName) throws SQLException {
        Statement stmt = conn.createStatement();
        String sqlQuery = String.format("SELECT * FROM %s", containerName);
        System.out.println(sqlQuery);

        ResultSet rs = stmt.executeQuery(sqlQuery);
        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();

        for (int i = 1; i <= columnCount; i++) {
            System.out.print(rsmd.getColumnName(i) + "\t");
        }

        while (rs.next()) {
            for (int i = 1; i <= columnCount; i++) {
                System.out.print(rs.getString(i) + "\t");
            }
        }
        System.out.println("");
    }

    /**
     * It performs a time-bucketed aggregation using the GridDB-specific
     * SQL 'GROUP BY RANGE' function, as shown in the blog post.
     */
    public void queryTimeBucketedAverages_SQL(String containerName) throws SQLException {

        String sql = String.format(
                "SELECT ts, AVG(temp) as avg_temp FROM %s " +

                        "WHERE ts BETWEEN TIMESTAMP('2020-07-12T00:01:20Z') AND TIMESTAMP('2020-07-12T00:14:00Z') " +

                        "GROUP BY RANGE (ts) EVERY(20, SECOND) ",
                containerName);

        System.out.println("Running SQL: " + sql);

        Statement stmt = null;
        ResultSet rs = null;

        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery(sql);

            System.out.println("Time-Bucketed Average Temperatures (SQL GROUP BY RANGE):");

            while (rs.next()) {
                java.sql.Timestamp bucket = rs.getTimestamp("ts");
                double avgTemp = rs.getDouble("avg_temp");

                System.out.println("  [" + bucket + "] = " + avgTemp);
            }
        } finally {
            if (rs != null) {
                rs.close();
            }
            if (stmt != null) {
                stmt.close();
            }
        }
    }

}
