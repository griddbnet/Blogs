import java.util.Properties;
import java.util.Scanner;
import java.io.File;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;

import com.toshiba.mwcloud.gs.Collection;
import com.toshiba.mwcloud.gs.GSException;
import com.toshiba.mwcloud.gs.GridStore;
import com.toshiba.mwcloud.gs.GridStoreFactory;
import com.toshiba.mwcloud.gs.Query;
import com.toshiba.mwcloud.gs.RowKey;
import com.toshiba.mwcloud.gs.Row;
import com.toshiba.mwcloud.gs.RowSet;

import weka.classifiers.Evaluation;
import weka.classifiers.functions.LinearRegression;
import weka.core.Instances;
import weka.core.converters.ArffSaver;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.Filter;
import weka.filters.unsupervised.attribute.Normalize;


public class NormalizationJava {
     static class House {
        @RowKey int housesize;
        int lotsize;
        int bedrooms;
        int granite;
        int bathroom;
        int sellingprice;
       
    }
        
    public static void main(String[] args) throws GSException {
// Creating GridDB connection
        Properties props = new Properties();
        props.setProperty("notificationMember", "127.0.0.1:10001");
        props.setProperty("clusterName", "myCluster");
        props.setProperty("user", "admin");
        props.setProperty("password", "admin");
        GridStore store = GridStoreFactory.getInstance().getGridStore(props);
       
        //
        Collection coll = store.putCollection("House", House.class);
       
        try {
           File file1 = new File("house.csv");
           Scanner scan = new Scanner(file1);
           String data = scan.next();
           
            while (scan.hasNext()){
                String scanData = scan.next();
                String dataList[] = scanData.split(",");
                String housesize = dataList[0];
                String lotsize = dataList[1];
                String bedrooms = dataList[2];
                String granite = dataList[3];
                String bathroom = dataList[4];
                String sellingprice = dataList[5];
               
                House hs = new House();
               
                hs.housesize = Integer.parseInt(housesize);
                hs.lotsize = Integer.parseInt(lotsize);
                hs.bedrooms = Integer.parseInt(bedrooms);
                hs.granite = Integer.parseInt(granite);
                hs.bathroom = Integer.parseInt(bathroom);
                hs.sellingprice = Integer.parseInt(sellingprice);
                coll.put(hs);
            }
        
            Query query = coll.query("select *");
            RowSet<House> rs = query.fetch(false);
       
           String datastr = "@RELATION house\n\n@ATTRIBUTE houseSize NUMERIC\n@ATTRIBUTE lotSize NUMERIC\n@ATTRIBUTE bedrooms NUMERIC\n@ATTRIBUTE granite NUMERIC\n@ATTRIBUTE bathroom NUMERIC\n@ATTRIBUTE sellingPrice NUMERIC\n\n@DATA\n";
           
           while (rs.hasNext()) {
            House hs = rs.next();
            datastr = datastr + hs.housesize+","+hs.lotsize+","+hs.bedrooms+","+hs.granite+","+hs.bathroom+","+hs.sellingprice+"\n";
           }
           
           System.out.println("==== Before Normalization ===\n"+datastr);
           
           Reader dataString = new StringReader(datastr);
           Instances dataset = new Instances(dataString);
           dataset.setClassIndex(dataset.numAttributes()-1);
           
           Normalize normalize = new Normalize();
           normalize.setInputFormat(dataset);
           Instances newdata = Filter.useFilter(dataset, normalize);
           
           System.out.println("==== After Normalization ===\n"+newdata);
           
           LinearRegression lr = new LinearRegression();
           lr.buildClassifier(newdata);
           Evaluation lreval = new Evaluation(newdata);
           lreval.evaluateModel(lr, newdata);
           System.out.println(lreval.toSummaryString());
        
           query.close();
           coll.close();
           store.close();
           
        } catch(Exception e){
          System.out.println(e);
        }
        
    }
}
