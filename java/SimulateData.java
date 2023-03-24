import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import com.toshiba.mwcloud.gs.Container;
import com.toshiba.mwcloud.gs.ContainerInfo;
import com.toshiba.mwcloud.gs.GSException;
import com.toshiba.mwcloud.gs.GridStore;
import com.toshiba.mwcloud.gs.GridStoreFactory;
import com.toshiba.mwcloud.gs.IndexType;
import com.toshiba.mwcloud.gs.Row;
import com.toshiba.mwcloud.gs.RowKey;
import com.toshiba.mwcloud.gs.TimeSeries;
import com.toshiba.mwcloud.gs.TimeUnit;
import com.toshiba.mwcloud.gs.TimestampUtils;

public class SimulateData {

    static class Iot {
        @RowKey Date timestamp;
        double data;
        double temperature;
    }

    public static Map<String, List<Row>> Generate(GridStore store, List<String> containerNameList, int rowCount)
            throws Exception {

        final Map<String, List<Row>> rowListMap = new HashMap<String, List<Row>>();

        try {

            Random rnd = new Random();

            for (String containerName : containerNameList) {
                final List<Row> rowList = new ArrayList<Row>();
                ContainerInfo containerInfo;
                try {
                    containerInfo = store.getContainerInfo(containerName);
                    System.out.println(containerName);
                    SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
                    Date tm1 = sf.parse("2022-03-07 10:00:00.000");
                    for (int j = 0; j < rowCount; j++) {
                        Row row = store.createRow(containerInfo);
                        row.setTimestamp(0, TimestampUtils.add(tm1, j, TimeUnit.MINUTE));
                        row.setDouble(1, rnd.nextInt(10000));
                        row.setDouble(2, rnd.nextInt(100));
                        rowList.add(row);
                    }
                    rowListMap.put(containerName, rowList);

                } catch (GSException e) {
                    System.out.println("An error occurred by the making of row data.");
                    e.printStackTrace();
                    System.exit(1);
                } catch (ParseException e) {
                    System.out.println("The format of the date had an error");
                    e.printStackTrace();
                    System.exit(1);
                }
            }

            return rowListMap;

        } catch (Exception e) {
            System.out.println("An error occurred by the making of row data.");
            e.printStackTrace();
            System.exit(1);
        }

        return rowListMap;
    }

    public static void main(String[] args) throws Exception {

        GridStore store = null;

        try {
            String provider =
            "https://dbaasshareextconsta.blob.core.windows.net/dbaas-share-extcon-blob/trial1602.json?sv=2015-04-05&sr=b&st=2023-03-14T00%3A00%3A00.0000000Z&se=2073-03-14T00%3A00%3A00.0000000Z&sp=r&sig=h2VJ0xAqsnRsqWV5CAS66RifPIZ1PDCJ0x%2FiXb2FOhA%3D";

            Properties props = new Properties();
            props.setProperty("notificationProvider", provider);
            props.setProperty("clusterName", "gs_clustertrial1602");
            props.setProperty("user", "israel");
            props.setProperty("password", "israel");
            props.setProperty("sslMode", "PREFERRED");
            props.setProperty("connectionRoute", "PUBLIC");
            store = GridStoreFactory.getInstance().getGridStore(props);

            // Properties props = new Properties();
            // props.setProperty("notificationMember", "127.0.0.1:10001");
            // props.setProperty("clusterName", "myCluster");
            // props.setProperty("user", "admin");
            // props.setProperty("password", "admin");
            // store = GridStoreFactory.getInstance().getGridStore(props);

            int numSensors = Integer.parseInt(args[0]);
            int rowCount = Integer.parseInt(args[1]);

            try {
                List<String> containerNameList = new ArrayList<String>();
                for (int i = 0; i < numSensors; i++) {
                    String name = "iot" + i;
                    containerNameList.add(name);
                    TimeSeries<Iot> ts;
                    try {
                        ts = store.putTimeSeries(name, Iot.class);
                    } catch (GSException e) {
                        System.out.println("An error occurred when creating the container.");
                        e.printStackTrace();
                        System.exit(1);
                    }
                }

                final Map<String, List<Row>> rowListMap = Generate(store, containerNameList, rowCount);
                try {
                    store.multiPut(rowListMap);
                } catch (Exception e) {
                    System.out.println("An error occurred when updating.");
                    System.exit(1);
                }

            } catch (Exception e) {

            }

        } finally {
            if (store != null) {
                try {
                    store.close();
                } catch (GSException e) {
                    System.out.println("An error occurred when releasing the recsource.");
                    e.printStackTrace();
                }
            }
        }

    }

}
