package net.griddb;

import java.io.IOException;
import java.net.URLEncoder;
import java.sql.*;
import java.util.logging.Level;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.*;
import java.util.Properties;
import java.util.ArrayList;
import java.util.List;

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

    public BatchResult GetTelemetryDataNewerThanControlTimeStamp(ExecutionContext context, String containerName,
            Timestamp last_pushed_timestamp)
            throws SQLException {

        String sqlQuery = String.format("SELECT * FROM %s WHERE ts > ?", containerName);
        System.out.println(sqlQuery);

        ObjectMapper mapper = new ObjectMapper().findAndRegisterModules();
        List<String> recordsToPublish = new ArrayList<>();
        Timestamp maxTimestamp = last_pushed_timestamp;

        try (PreparedStatement pstmt = conn.prepareStatement(sqlQuery)) {

            pstmt.setTimestamp(1, last_pushed_timestamp);

            try (ResultSet rs = pstmt.executeQuery()) {

                while (rs.next()) {

                    try {
                        Timestamp currentRecordTs = rs.getTimestamp("ts");
                        if (currentRecordTs.after(maxTimestamp)) {
                            maxTimestamp = currentRecordTs;
                        }

                        TelemetryData data = new TelemetryData();
                        data.setTs(rs.getTimestamp("ts").toInstant());
                        data.setTemperature(rs.getDouble("Temperature"));
                        data.setHumidity(rs.getInt("Humidity"));
                        data.setPressure(rs.getDouble("Pressure"));
                        data.setDataPointId(rs.getInt("DataPointId"));

                        String jsonString = mapper.writeValueAsString(data);
                        recordsToPublish.add(jsonString);
                    } catch (SQLException | IOException e) {
                        context.getLogger().log(Level.SEVERE, "Error processing row or serializing JSON", e);
                    }
                }
            }

        }

        return new BatchResult(recordsToPublish, maxTimestamp);
    }

    public void WriteLastPushedTimeStampToControlTable(
            ExecutionContext context,
            Timestamp last_pushed_timestamp) throws SQLException {

        String sqlUpdate = "UPDATE ControlTable SET last_pushed_time = ? WHERE app_name = 'telemetryData'";

        try (PreparedStatement pstmt = conn.prepareStatement(sqlUpdate)) {

            pstmt.setTimestamp(1, last_pushed_timestamp);

            int rowsAffected = pstmt.executeUpdate();
            context.getLogger().info("Watermark updated. Rows affected: " + rowsAffected);

        }
    }

    public java.sql.Timestamp GetMaxTime(ExecutionContext context, String containerName, String columnName) {
        java.sql.Timestamp last_pushed_timestamp = null;
        try {
            Statement stmt = conn.createStatement();
            String sqlQuery = String.format("SELECT MAX(%s) FROM %s", columnName, containerName);
            ResultSet rs = stmt.executeQuery(sqlQuery);
            rs.next();
            last_pushed_timestamp = rs.getTimestamp(1);

        } catch (SQLException e) {
            context.getLogger().severe("Error getting last time " + e);
        }
        return last_pushed_timestamp;
    }
}
