import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import java.lang.*;

import java.io.IOException;
import java.util.Scanner;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;

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

import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.classifiers.trees.J48;
import weka.classifiers.Evaluation;

public class FraudulentTransactions {

    public static void main(String[] args){
        try {

            // Manage connection to GridDB
            Properties prop = new Properties();

            prop.setProperty("notificationAddress", "239.0.0.1");
            prop.setProperty("notificationPort", "31999");
            prop.setProperty("clusterName", "cluster");
            prop.setProperty("database", "public");
            prop.setProperty("user", "admin");
            prop.setProperty("password", "admin");

            // Get Store and Container
            GridStore store = GridStoreFactory.getInstance().getGridStore(prop);
            
            store.getContainer("newContainer");

            String containerName = "mContainer";
        
            // Define container schema and columns
            ContainerInfo containerInfo = new ContainerInfo();
            List<ColumnInfo> columnList = new ArrayList<ColumnInfo>();

            columnList.add(new ColumnInfo("key", GSType.INTEGER));
            columnList.add(new ColumnInfo("Time", GSType.INTEGER));
            columnList.add(new ColumnInfo("V1", GSType.FLOAT));
            columnList.add(new ColumnInfo("V2", GSType.FLOAT));
            columnList.add(new ColumnInfo("V3", GSType.FLOAT));
            columnList.add(new ColumnInfo("V4", GSType.FLOAT));
            columnList.add(new ColumnInfo("V5", GSType.FLOAT));
            columnList.add(new ColumnInfo("V6", GSType.FLOAT));
            columnList.add(new ColumnInfo("V7", GSType.FLOAT));
            columnList.add(new ColumnInfo("V8", GSType.FLOAT));
            columnList.add(new ColumnInfo("V9", GSType.FLOAT));
            columnList.add(new ColumnInfo("V10", GSType.FLOAT));
            columnList.add(new ColumnInfo("V11", GSType.FLOAT));
            columnList.add(new ColumnInfo("V12", GSType.FLOAT));
            columnList.add(new ColumnInfo("V13", GSType.FLOAT));
            columnList.add(new ColumnInfo("V14", GSType.FLOAT));
            columnList.add(new ColumnInfo("V15", GSType.FLOAT));
            columnList.add(new ColumnInfo("V16", GSType.FLOAT));
            columnList.add(new ColumnInfo("V17", GSType.FLOAT));
            columnList.add(new ColumnInfo("V18", GSType.FLOAT));
            columnList.add(new ColumnInfo("V19", GSType.FLOAT));
            columnList.add(new ColumnInfo("V20", GSType.FLOAT));
            columnList.add(new ColumnInfo("V21", GSType.FLOAT));
            columnList.add(new ColumnInfo("V22", GSType.FLOAT));
            columnList.add(new ColumnInfo("V23", GSType.FLOAT));
            columnList.add(new ColumnInfo("V24", GSType.FLOAT));
            columnList.add(new ColumnInfo("V25", GSType.FLOAT));
            columnList.add(new ColumnInfo("V26", GSType.FLOAT));
            columnList.add(new ColumnInfo("V27", GSType.FLOAT));
            columnList.add(new ColumnInfo("V28", GSType.FLOAT));
            columnList.add(new ColumnInfo("Amount", GSType.FLOAT));
            columnList.add(new ColumnInfo("Class", GSType.INTEGER));

            containerInfo.setColumnInfoList(columnList);
            containerInfo.setRowKeyAssigned(true);
            Collection<Void, Row> collection = store.putCollection(containerName, containerInfo, false);
            List<Row> rowList = new ArrayList<Row>();

            // Handling Dataset and storage to GridDB
            File data = new File("/home/ubuntu/griddb/gsSample/creditcard.csv");
            Scanner sc = new Scanner(data);  
            sc.useDelimiter("\n");

            // Returns a boolean value
            while (sc.hasNext())  
            { 
                int i = 0;
                Row row = collection.createRow();

                String line = sc.next();
                String columns[] = line.split(",");
            
                int Time = Integer.parseInt(columns[0]);

                float V1 = Float.parseFloat(columns[1]);
                float V2 = Float.parseFloat(columns[2]);
                float V3 = Float.parseFloat(columns[3]);
                float V4 = Float.parseFloat(columns[4]);
                float V5 = Float.parseFloat(columns[5]);
                float V6 = Float.parseFloat(columns[6]);
                float V7 = Float.parseFloat(columns[7]);
                float V8 = Float.parseFloat(columns[8]);
                float V9 = Float.parseFloat(columns[9]);
                float V10 = Float.parseFloat(columns[10]);
                float V11 = Float.parseFloat(columns[11]);
                float V12 = Float.parseFloat(columns[12]);
                float V13 = Float.parseFloat(columns[13]);
                float V14 = Float.parseFloat(columns[14]);
                float V15 = Float.parseFloat(columns[15]);
                float V16 = Float.parseFloat(columns[16]);
                float V17 = Float.parseFloat(columns[17]);
                float V18 = Float.parseFloat(columns[18]);
                float V19 = Float.parseFloat(columns[19]);
                float V20 = Float.parseFloat(columns[20]);
                float V21 = Float.parseFloat(columns[21]);
                float V22 = Float.parseFloat(columns[22]);
                float V23 = Float.parseFloat(columns[23]);
                float V24 = Float.parseFloat(columns[24]);
                float V25 = Float.parseFloat(columns[25]);
                float V26 = Float.parseFloat(columns[26]);
                float V27 = Float.parseFloat(columns[27]);
                float V28 = Float.parseFloat(columns[28]);
                    
                int Amount = Integer.parseInt(columns[29]);
                int Class = Integer.parseInt(columns[30]);
            
                row.setInteger(0,i);
                row.setInteger(1, Time);

                row.setFloat(2, V1);
                row.setFloat(3, V2);
                row.setFloat(4, V3);
                row.setFloat(5, V4);
                row.setFloat(6, V5);
                row.setFloat(7, V6);
                row.setFloat(8, V7);
                row.setFloat(9, V8);
                row.setFloat(10, V9);
                row.setFloat(11, V10);
                row.setFloat(12, V11);
                row.setFloat(13, V12);
                row.setFloat(14, V13);
                row.setFloat(15, V14);
                row.setFloat(16, V15);
                row.setFloat(17, V16);
                row.setFloat(18, V17);
                row.setFloat(19, V18);
                row.setFloat(20, V19);
                row.setFloat(21, V20);
                row.setFloat(22, V21);
                row.setFloat(23, V22);
                row.setFloat(24, V23);
                row.setFloat(25, V24);
                row.setFloat(26, V25);
                row.setFloat(27, V26);
                row.setFloat(28, V27);
                row.setFloat(29, V28);
                row.setFloat(30, V28);

                row.setInteger(31,Amount);
                row.setInteger(32,Class);

                rowList.add(row);
        
                i++;
            }   
        
            // Retrieving data from GridDB
            Container<?, Row> container = store.getContainer(containerName);

            if ( container == null ){
                throw new Exception("Container not found.");
            }

            Query<Row> query = container.query("SELECT * ");
            RowSet<Row> rs = query.fetch();
  
            DataSource source = new DataSource("/home/ubuntu/griddb/gsSample/creditcard.csv");
            Instances datasetInstances = source.getDataSet();
    
            datasetInstances.setClassIndex(0);

            String[] options = new String[4];
            options[0] = "-C";
            options[1] = "0.25";
            options[2] = "-M";
            options[3] = "30";
         
            J48 mytree = new J48();
            mytree.setOptions(options);
            datasetInstances.setClassIndex(datasetInstances.numAttributes()-1);

            mytree.buildClassifier(datasetInstances);
    
            //Perform Evaluation 
            Evaluation eval = new Evaluation(datasetInstances);
            eval.crossValidateModel(mytree, datasetInstances, 10, new Random(1));

            System.out.println(eval.toSummaryString("\n === Summary === \n", true));

            // Print GridDB data
            while (rs.hasNext()) {
                Row row = rs.next();
        
                System.out.println(" Row=" + row);
            }

            // Terminating process
            collection.put(rowList);
            
            //closes the scanner 
            sc.close();  
            rs.close();
            query.close();
            container.close();
            store.close();
            System.out.println("success!");          
            

        } catch ( Exception e ){
            e.printStackTrace();
        }
    }

}