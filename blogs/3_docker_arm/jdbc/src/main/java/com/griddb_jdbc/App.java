package jdbc.src.main.java.com.griddb_jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Properties;
import java.util.Date;

import com.toshiba.mwcloud.gs.GSException;
import com.toshiba.mwcloud.gs.RowKey;

public class App {

	static class Device {
		@RowKey Date ts;
		double co;
		double humidity;
		boolean light;
		double lpg;
		boolean motion;
		double smoke;
		double temp;
	}

    public static void main(String[] args) throws GSException {

        try {
            // ===================================================
            // Connecting to GridDB Cluster via JDBC/SQL Interface
            // ===================================================

            String url = args[0];
            String clusterName = args[1];
            String dbName = args[2];
            String jdbcUrl = "jdbc:gs://" + url + "/" + clusterName + "/" + dbName;

            Properties prop = new Properties();
            prop.setProperty("user", "admin");
            prop.setProperty("password", "admin");

            Connection con = DriverManager.getConnection(jdbcUrl, prop);

            System.out.println("Connected to cluster via SQL Interface");

			String SQL = "CREATE TABLE IF NOT EXISTS devices (ts TIMESTAMP PRIMARY KEY, co DOUBLE, humidity DOUBLE,light BOOL,lpg DOUBLE,motion BOOL,smoke DOUBLE,temp DOUBLE) USING TIMESERIES WITH (expiration_type='PARTITION',expiration_time=90,expiration_time_unit='DAY') PARTITION BY RANGE (ts) EVERY (60, DAY)SUBPARTITION BY HASH (ts) SUBPARTITIONS 64;";

			Statement stmt = con.createStatement();
			stmt.executeUpdate("DROP TABLE IF EXISTS devices");
			stmt.executeUpdate(SQL);
			System.out.println("Successfully created container called: devices");

			con.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}