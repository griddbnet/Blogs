import java.time.*;
import java.util.Random;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.Arrays;
import java.io.File;
import java.nio.file.Files;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.stream.*;
import java.util.Properties;
import java.lang.Integer;
import java.nio.charset.StandardCharsets;
import java.io.Reader;
import java.io.FileReader;
import java.lang.StringBuffer;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.text.SimpleDateFormat;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;


import java.net.URLEncoder;

class JdbcCreateTables{


    private static Properties sourceProps = new Properties();
    private static Connection conn = null;
    private static Statement stmt = null;
    private static InputStream input;
    
    private static void createTables() throws Exception {

        try {
            System.out.println("creating tables");
            stmt = conn.createStatement();
            for ( int i = 1; i <= 8; i++) {
                String s = String.valueOf(i);
                String sql = sourceProps.getProperty(s);
                stmt.executeUpdate(sql);
                System.out.println("Created table "+ i);
            }


        } catch (Exception e) {
            throw new Error("Issue with creating table: ", e);
        }

    }

    private static void connectDB() {

    	try {
			input = new FileInputStream("gridstore-jdbc.properties");
			sourceProps.load(input);

		} catch (Exception e) {
			System.out.println("COULD NOT LOAD source.properties: " + e);
		}

        try {
            String provider = "https://dbaasshareextconsta.blob.core.windows.net/dbaas-share-extcon-blob/trial1602.json?sv=2015-04-05&sr=b&st=2023-03-14T00%3A00%3A00.0000000Z&se=2073-03-14T00%3A00%3A00.0000000Z&sp=r&sig=h2VJ0xAqsnRsqWV5CAS66RifPIZ1PDCJ0x%2FiXb2FOhA%3D";
            String encodeProviderUrl = URLEncoder.encode(provider, "UTF-8");
            String clusterName = "gs_clustertrial1602";
            String encodeClusterName = URLEncoder.encode(clusterName, "UTF-8");
            String databaseName = "public";
            String encodeDatabaseName = URLEncoder.encode(databaseName, "UTF-8");
            String ssl = "&sslMode=VERIFY";
            String encodeSsl = URLEncoder.encode(ssl, "UTF-8");

            Properties props = new Properties();
            String jdbcUrl = "jdbc:gs:///" + encodeClusterName + "/" + encodeDatabaseName
      + "?notificationProvider="+encodeProviderUrl + encodeSsl;
            props.setProperty("user", "israel");
            props.setProperty("password", "israel");
            props.setProperty("loginTimeout", "60");
            props.setProperty("connectionRoute", "PUBLIC");

            System.out.println(jdbcUrl);
            conn = DriverManager.getConnection(jdbcUrl, props );


        System.out.println("Sucessfully connected!");

        } catch (Exception e) {
            System.out.println("error connecting to DB: "+e);
        }
    }

    public static void main(String[] args) {
        try {
            connectDB();
            createTables();
            System.out.println("Complete!");
        } catch (Exception e) {
            throw new Error("Problem", e);
        }
    }


}

