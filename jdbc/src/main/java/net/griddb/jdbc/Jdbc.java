package net.griddb.jdbc;

import java.net.URLEncoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Properties;


public class Jdbc {

    public static Connection con;
    public static void main(String[] args) {

        try {
            // ===================================================
            // Connecting to GridDB Cluster via JDBC/SQL Interface
            // ===================================================

            String notificationMember = args[0];
            String clusterName = args[1];
            String databaseName = args[2];
            // String notificationMember = "griddb-server:20001";
            // String clusterName = "myCluster";
            // String databaseName = "public";
            String username = "admin";
            String password = "admin";
            String encodeClusterName = URLEncoder.encode(clusterName, "UTF-8");
            String encodeDatabaseName = URLEncoder.encode(databaseName, "UTF-8");
            String jdbcUrl = "jdbc:gs://" + notificationMember + "/" + encodeClusterName + "/" + encodeDatabaseName;
            System.out.println(jdbcUrl);
    
            Properties prop = new Properties();
            prop.setProperty("user", username);
            prop.setProperty("password", password);

            con = DriverManager.getConnection(jdbcUrl, prop);

            System.out.println("Connected to cluster via SQL Interface");

			String SQL = "CREATE TABLE IF NOT EXISTS devices (ts TIMESTAMP PRIMARY KEY, co DOUBLE, humidity DOUBLE,light BOOL,lpg DOUBLE,motion BOOL,smoke DOUBLE,temp DOUBLE) USING TIMESERIES WITH (expiration_type='PARTITION',expiration_time=90,expiration_time_unit='DAY') PARTITION BY RANGE (ts) EVERY (60, DAY)SUBPARTITION BY HASH (ts) SUBPARTITIONS 64;";

			Statement stmt = con.createStatement();
			stmt.executeUpdate("DROP TABLE IF EXISTS devices");
			stmt.executeUpdate(SQL);
			System.out.println("Successfully created container called: devices");

			con.close();

        } catch (Exception e) {
            System.out.println("Could not connect to GridDB via JDBC, exiting.");
            e.printStackTrace();
            System.exit(-1);
        }


    }
}
