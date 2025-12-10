package net.griddb_cloud;

import java.util.Properties;
import java.util.Date;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.sql.Timestamp;
import com.toshiba.mwcloud.gs.*;

public class GridDB {

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

    public void CreateContainer(String containerName) {
        System.out.println("Creating Container");
        try {
            store.putTimeSeries(containerName, TelemetryData.class);
        } catch (GSException gse) {
            gse.printStackTrace();
        }
        System.out.println("Container Created");
    }

    public void WriteToContainer(String containerName, TelemetryData data) {
        System.out.println("Writing some arbitrary data to container: " + containerName);
        try {
            TimeSeries<TelemetryData> ts = store.putTimeSeries(containerName, TelemetryData.class);
            // Must set auto commit to false if you intend to manually commit after you are
            // finished
            ts.setAutoCommit(false);

            System.out.println("Inserting data to GridDB");
            System.out.println(data.toString());
            ts.put(data);
            ts.commit();
        } catch (GSException gse) {
            System.out.println("Error commiting");
            gse.printStackTrace();
        }

    }
}
