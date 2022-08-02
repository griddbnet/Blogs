/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package textclassification;

/**
 *
 * @author user
 */
import java.io.File;
import java.util.Scanner;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;

import com.toshiba.mwcloud.gs.Query;
import com.toshiba.mwcloud.gs.RowKey;
import com.toshiba.mwcloud.gs.RowSet;
import com.toshiba.mwcloud.gs.GridStore;
import com.toshiba.mwcloud.gs.Collection;
import com.toshiba.mwcloud.gs.GSException;
import com.toshiba.mwcloud.gs.GridStoreFactory;

import java.io.*;
import weka.core.*;
import java.util.List;
import java.util.Random;
import weka.filters.Filter;
import java.util.ArrayList;
import weka.core.Instances;
import weka.core.FastVector;
import weka.classifiers.Evaluation;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.meta.FilteredClassifier;
import weka.core.converters.ArffLoader.ArffReader;
import weka.filters.unsupervised.attribute.StringToWordVector;



public class TextClassification {
    
    public static class EmailClassification{
    
    @RowKey String spamclass;
     String text;
      
    
    }


    public static void main(String[] args) {
        // TODO code application logic here
         Properties props = new Properties();
        props.setProperty("notificationAddress", "239.0.0.1");
        props.setProperty("notificationPort", "31999");
        props.setProperty("clusterName", "defaultCluster");
        props.setProperty("user", "admin");
        props.setProperty("password", "admin");
        GridStore store = GridStoreFactory.getInstance().getGridStore(props);
        
        Collection<String, EmailClassification> coll = store.putCollection("col01", EmailClassification.class);

        File file1 = new File("smsspam.csv");
                Scanner sc = new Scanner(file1);
                String data = sc.next();
 
                while (sc.hasNext()){
                    
                        String scData = sc.next();
                        String dataList[] = scData.split(",");
                        String spamclass = dataList[0];
                        String text = dataList[1];
                        
                        EmailClassification ec = new EmailClassification();
                        ec.spamclass = spamclass;
                        ec.text = text;
                                                
                                               
                        coll.append(ec);
    }
                
                Query<EmailClassification> query = coll.query("select *");
   RowSet<EmailClassification> rs = query.fetch(false);
   RowSet res = query.fetch();
   
   BufferedReader bufferedReader
                = new BufferedReader(
                    new FileReader(res));
 
            // Create dataset instances
            Instances datasetInstances
                = new Instances(bufferedReader);
   
   datasetInstances.setClassIndex(0);
   StringToWordVector filter = new StringToWordVector();
   filter.setAttributeIndices("last");
   FilteredClassifier classifier = new FilteredClassifier();
   classifier.setFilter(filter);
   classifier.setClassifier(new NaiveBayes());
   classifier.buildClassifier(datasetInstances);
   System.out.println(classifier);
   
   Evaluation eval = new Evaluation(datasetInstances);
   eval.crossValidateModel(classifier, datasetInstances, 4, new Random(1));
   System.out.println(eval.toSummaryString());
                        
      Instance pred = datasetInstances.lastInstance();
      double answer = classifier.classifyInstance(pred);
      System.out.println("Class predicted: " + pred.classAttribute().value((int) answer));

                
			
    }
    
}
