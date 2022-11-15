/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package opinionmining;

/**
 *
 * @author user
 */
import java.io.*;
import weka.core.*;
import java.util.List;
import java.util.ArrayList;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.Instances;


import java.io.File;
import java.util.Scanner;
import java.io.IOException;
import java.util.Properties;

import com.toshiba.mwcloud.gs.Query;
import com.toshiba.mwcloud.gs.RowKey;
import com.toshiba.mwcloud.gs.RowSet;
import com.toshiba.mwcloud.gs.GridStore;
import com.toshiba.mwcloud.gs.Collection;
import com.toshiba.mwcloud.gs.GSException;
import com.toshiba.mwcloud.gs.GridStoreFactory;


public class OpinionMining {
    
    
    public static class OpinionData{
    
    @RowKey String sentence;
     String target;
    
    }

   
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        

        Properties props = new Properties();
        props.setProperty("notificationMember", "127.0.0.1:10001");
        props.setProperty("clusterName", "myCluster");
        props.setProperty("user", "admin");
        props.setProperty("password", "admin");
        GridStore store = GridStoreFactory.getInstance().getGridStore(props);       
 
        Collection<String, OpinionData> coll = store.putCollection("col01", OpinionData.class);
        
        File file1 = new File("Reviews.csv");
                Scanner sc = new Scanner(file1);
                String data = sc.next();
 
                while (sc.hasNext()){
                    
                        String scData = sc.next();
                        String dataList[] = scData.split(",");
                        String sentence = dataList[0];
                        String target = dataList[1];
                        
                        OpinionData od = new OpinionData();
                        od.sentence = sentence;
                        od.target = target;
                                                
                                               
                        coll.append(od);
                 }
Query<OpinionData> query = coll.query("select *");
   RowSet<OpinionData> rs = query.fetch(false);
   RowSet res = query.fetch();
   
   
   BufferedReader bufferedReader
                = new BufferedReader(
                    new FileReader(res));
 
            // Create dataset instances
            Instances datasetInstances
                = new Instances(bufferedReader);
   
    datasetInstances.setClassIndex(0);
    FilteredClassifier classifier = new FilteredClassifier();
    classifier.buildClassifier(datasetInstances);
    System.out.println(classifier);
    
    Instance pred = datasetInstances.lastInstance();
    double answer = classifier.classifyInstance(pred);
    System.out.println("Class predicted: " + pred.classAttribute().value((int) answer));

    }
    
}
