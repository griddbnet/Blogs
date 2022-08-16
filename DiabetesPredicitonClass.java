
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

// ----------- Weka ---------
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.classifiers.trees.RandomForest;
import weka.classifiers.Evaluation;




public class randomForestClass {

    public static void main(String[] args){
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
            columnList.add(new ColumnInfo("Pregnancies", GSType.INTEGER));
            columnList.add(new ColumnInfo("Glucose", GSType.INTEGER));
            columnList.add(new ColumnInfo("BloodPressure", GSType.INTEGER));
            columnList.add(new ColumnInfo("SkinThickness", GSType.INTEGER))
            columnList.add(new ColumnInfo("Insulin", GSType.INTEGER));
            columnList.add(new ColumnInfo("BMI", GSType.FLOAT));
            columnList.add(new ColumnInfo("DiabetesPedigreeFunction", GSType.FLOAT));
            columnList.add(new ColumnInfo("Age", GSType.INTEGER));
            columnList.add(new ColumnInfo("Outcome", GSType.INTEGER));

            containerInfo.setColumnInfoList(columnList);
            containerInfo.setRowKeyAssigned(true);

            Collection<Void, Row> collection = store.putCollection(containerName, containerInfo, false);
            List<Row> rowList = new ArrayList<Row>();

             // Handlig Dataset and storage to GridDB
            File data = new File("/home/ubuntu/griddb/gsSample/diabetes.csv");
            Scanner sc = new Scanner(data);  
            sc.useDelimiter("\n");

            while (sc.hasNext()) 
            {  
                int i = 0;
                Row row = collection.createRow();

                String line = sc.next();
                String columns[] = line.split(",");

                int pregnancies = Integer.parseInt(columns[0]);
                int glucose = Integer.parseInt(columns[1]);
                int bloodpressure = Integer.parseInt(columns[2]);
                int skinthickness = Integer.parseInt(columns[3]);
                int insulin = Integer.parseInt(columns[4]);
                float bmi = Float.parseFloat(columns[5]);
                float diabetespedigreefunction = Float.parseFloat(columns[6]);
                int age = Integer.parseInt(columns[7]);
                int outcome = Integer.parseInt(columns[8]);
                
                row.setInteger(0, i);
                row.setInteger(1, pregnancies);
                row.setInteger(2, glucose);
                row.setInteger(3, bloodpressure);
                row.setInteger(4, skinthickness);
                row.setInteger(5, insulin);
                row.setFloat(6, bmi);
                row.setFloat(7, diabetespedigreefunction);
                row.setInteger(8, age);
                row.setInteger(9, outcome);

                rowList.add(row);
        
                i++;
        }   
        
            // Retrieving data from GridDB
        
            Container<?, Row> container = store.getContainer(containerName);

            if (container == null){
                throw new Exception("Container not found.");
            }
            Query<Row> query = container.query("SELECT * ");
            RowSet<Row> rowset = query.fetch();
        

            int numFolds = 10;
            DataSource source = new DataSource("/home/ubuntu/griddb/gsSample/diabetes.csv");
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
        while ( rowset.hasNext() ) {
            Row row = rowset.next();

            int pregnancies = row.getInt(0);
            int glucose = row.getInt(1);
            int bloodpressure = row.getInt(2);
            int skinthickness = row.getInt(3);
            int insulin = row.getInt(4);
            float bmi = row.getFloat(5);
            float diabetespedigreefunction = row.getFloat(6);
            int age = row.getFgetIntloat(7);
            int outcome = row.getInt(8);

            System.out.println(pregnancies);
            System.out.println(glucose);
            System.out.println(bloodpressure);
            System.out.println(skinthickness);
            System.out.println(insulin);
            System.out.println(bmi);
            System.out.println(diabetespedigreefunction);
            System.out.println(age);
            System.out.println(outcome);
        }

        // Terminating processes
        collection.put(rowList);
        sc.close();
        rowset.close();
        query.close();
        container.close();
        store.close();

        } catch ( Exception e ){
            e.printStackTrace();
        }
    }
}