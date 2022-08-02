// ---------- Java Util ---------
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;

// ---------- Java IO ---------
import java.io.IOException;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;

// ---------- GridDB ---------
import com.toshiba.mwcloud.gs.Collection;
import com.toshiba.mwcloud.gs.ColumnInfo;
import com.toshiba.mwcloud.gs.Container;
import com.toshiba.mwcloud.gs.ContainerInfo;
import com.toshiba.mwcloud.gs.GSType;
import com.toshiba.mwcloud.gs.GridStore;
import com.toshiba.mwcloud.gs.GridStoreFactory;
import com.toshiba.mwcloud.gs.Query;
import com.toshiba.mwcloud.gs.Row;
import com.toshiba.mwcloud.gs.RowSet;

//----------- Weka ---------
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.classifiers.trees.RandomForest;
import weka.classifiers.Evaluation;


public class randomForestClass {

    public static void main(String[] args) {
        try {

            // Manage connection to GridDB
            Properties properties = new Properties();
            properties.setProperty("notificationAddress", "239.0.0.1");
            properties.setProperty("notificationPort", "31999");
            properties.setProperty("clusterName", "cluster");
            properties.setProperty("database", "public");
            properties.setProperty("user", "admin");
            properties.setProperty("password", "admin");

            // Get Store and Container
            GridStore store = GridStoreFactory.getInstance().getGridStore(properties);

            store.getContainer("newContainer");

            String containerName = "mContainer";

            // Define container schema and columns
            ContainerInfo containerInfo = new ContainerInfo();
            List<ColumnInfo> columnList = new ArrayList<ColumnInfo>();

            columnList.add(new ColumnInfo("key", GSType.INTEGER));
            columnList.add(new ColumnInfo("Serial No.", GSType.INTEGER));
            columnList.add(new ColumnInfo("GRE Score", GSType.INTEGER));
            columnList.add(new ColumnInfo("TOEFL Score", GSType.INTEGER));
            columnList.add(new ColumnInfo("University Rating", GSType.INTEGER));

            columnList.add(new ColumnInfo("SOP", GSType.FLOAT));
            columnList.add(new ColumnInfo("LOR", GSType.FLOAT));
            columnList.add(new ColumnInfo("CGPA", GSType.FLOAT));

            columnList.add(new ColumnInfo("Research", GSType.INTEGER));

            columnList.add(new ColumnInfo("Chance of Admit", GSType.FLOAT));

            containerInfo.setColumnInfoList(columnList);
            containerInfo.setRowKeyAssigned(true);
            Collection<Void, Row> collection = store.putCollection(containerName, containerInfo, false);
            List<Row> rowList = new ArrayList<Row>();

            // Handling Dataset and storage to GridDB
            File data = new File("/home/ubuntu/griddb/gsSample/Admission_Predict.csv");
            Scanner sc = new Scanner(data);
            sc.useDelimiter("\n");

            while (sc.hasNext())  // Returns a boolean value
            {
                int i = 0;
                Row row = collection.createRow();

                String line = sc.next();
                String columns[] = line.split(",");

                int serial = Integer.parseInt(columns[0]);
                int gre = Integer.parseInt(columns[1]);
                int toefl = Integer.parseInt(columns[2]);
                int rating = Integer.parseInt(columns[3]);

                float sop = Float.parseFloat(columns[4]);
                float lor = Float.parseFloat(columns[5]);
                float cgpa = Float.parseFloat(columns[6]);

                int research = Integer.parseInt(columns[7]);

                float admitclass = Float.parseFloat(columns[8]);

                row.setInteger(0, i);
                row.setInteger(1, serial);
                row.setInteger(2, gre);
                row.setInteger(3, toefl);
                row.setInteger(4, rating);

                row.setFloat(5, sop);
                row.setFloat(6, lor);
                row.setFloat(7, cgpa);

                row.setInteger(8, research);

                row.setFloat(9, admitclass);

                rowList.add(row);

                i++;
            }

            // Retrieving data from GridDB
            Container<?, Row> container = store.getContainer(containerName);

            if (container == null) {
                throw new Exception("Container not found.");
            }

            Query<Row> query = container.query("SELECT * ");
            RowSet<Row> rowset = query.fetch();

            int numFolds = 10;
            DataSource source = new DataSource("/home/ubuntu/griddb/gsSample/Admission_Predict.csv");
            Instances datasetInstances = source.getDataSet();

            datasetInstances.setClassIndex(datasetInstances.numAttributes() - 1);

            // Implement Random Forest Algorithm
            String[] parameters = new String[14];

            parameters[0] = "-P";
            parameters[1] = "100";
            parameters[2] = "-I";
            parameters[3] = "100";
            parameters[4] = "-num-slots";
            parameters[5] = "1";
            parameters[6] = "-K";
            parameters[7] = "0";
            parameters[8] = "-M";
            parameters[9] = "1.0";
            parameters[10] = "-V";
            parameters[11] = "0.001";
            parameters[12] = "-S";
            parameters[13] = "1";

            RandomForest randomForest = new RandomForest();
            randomForest.setOptions(parameters);

            randomForest.buildClassifier(datasetInstances);

            Evaluation evaluation = new Evaluation(datasetInstances);


            evaluation.crossValidateModel(randomForest, datasetInstances, numFolds, new Random(1));

            System.out.println(evaluation.toSummaryString("\nResults\n======\n", true));

            // Print GridDB data
            while (rowset.hasNext()) {

                Row row = rowset.next();

                int serial = row.getInt(0);

                float gre = row.getFloat(1);
                float toefl = row.getFloat(2);
                float rating = row.getFloat(3);

                int sop = row.getInt(4);
                int lor = row.getInt(5);
                int cgpa = row.getInt(6);
                int research = row.getInt(7);

                float admitclass =row.getFloat(8);

                System.out.println(admitclass);
            }

            // Terminating processes
            collection.put(rowList);
            sc.close();  //closes the scanner
            rowset.close();
            query.close();
            container.close();
            store.close();
            System.out.println("Success!");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}