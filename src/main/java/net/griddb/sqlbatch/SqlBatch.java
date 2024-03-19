package net.griddb.sqlbatch;

import java.net.URLEncoder;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.Random;

import javax.sql.rowset.serial.SerialBlob;

import com.toshiba.mwcloud.gs.GSException;

public class SqlBatch {

	public static Connection con;
	public static Statement stmt;
	public static SerialBlob serialBlob;

	public SqlBatch() throws SQLException {
		try {
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

			con = DriverManager.getConnection(jdbcUrl, prop);
			stmt = con.createStatement();

			// set up random blob data
			byte[] b = new byte[1000];
			new Random().nextBytes(b);
			serialBlob = new SerialBlob(b);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) throws SQLException, GSException {
		new SqlBatch();
		
		stmt.executeUpdate("DROP TABLE IF EXISTS SQL_Single_Input");
		stmt.executeUpdate("CREATE TABLE SQL_Single_Input ( id integer PRIMARY KEY, data blob )");
		PreparedStatement pstmt = con.prepareStatement("INSERT INTO SQL_Single_Input(id, data) VALUES(?, ?)");

		Long startTime = System.nanoTime();
		for (int i = 1; i <= 10000; i++) {
			pstmt.setInt(1, i);
			pstmt.setBlob(2, serialBlob);
			pstmt.executeUpdate();
		}
		Long endTime = System.nanoTime();
		Long duration = (endTime - startTime) / 1000000; // milliseconds
		System.out.println("singular SQL took: " + Long.toString(duration) + " milliseconds");
		pstmt.close();

		stmt.executeUpdate("DROP TABLE IF EXISTS SQL_Batch_Input");
		stmt.executeUpdate("CREATE TABLE SQL_Batch_Input ( id integer PRIMARY KEY, data blob )");
		PreparedStatement pstmtBatch = con.prepareStatement("INSERT INTO SQL_Batch_Input(id, data) VALUES(?, ?)");

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
		duration = (endTime - startTime) / 1000000;
		System.out.println("add batch SQL took: " + Long.toString(duration) + " milliseconds");

		stmt.executeUpdate("DROP TABLE IF EXISTS SampleTQL_BlobData");
		stmt.executeUpdate("CREATE TABLE SampleTQL_BlobData ( id integer PRIMARY KEY, data blob )");
		stmt.executeUpdate("DROP TABLE IF EXISTS MultiPutTQL_Blobdata");
		stmt.executeUpdate("CREATE TABLE MultiPutTQL_Blobdata ( id integer PRIMARY KEY, data blob )");

		stmt.close();
		pstmtBatch.close();
		con.close();

		NoSQL noSQL = new NoSQL();

		startTime = System.nanoTime();
		noSQL.runSinglePut(serialBlob);
		endTime = System.nanoTime();
		duration = (endTime - startTime) / 1000000;
		System.out.println("TQL took: " + Long.toString(duration) + " milliseconds");

		startTime = System.nanoTime();
		noSQL.runMulti(serialBlob);
		endTime = System.nanoTime();
		duration = (endTime - startTime) / 1000000;
		System.out.println("Multi Put took: " + Long.toString(duration) + " milliseconds");

	}
}
