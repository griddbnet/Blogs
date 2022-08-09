/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ensemblelearningproject;

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

import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.meta.Bagging;
import java.io.BufferedReader;
import weka.core.Instances;
import java.io.FileReader;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.Logistic;
import weka.classifiers.meta.AdaBoostM1;
import weka.classifiers.meta.Stacking;
import weka.classifiers.trees.DecisionStump;
import weka.classifiers.trees.J48;
import weka.classifiers.meta.Vote;
import weka.classifiers.trees.RandomForest;
import weka.classifiers.trees.RandomTree;
import weka.core.converters.ConverterUtils.DataSource;
import weka.classifiers.functions.LinearRegression;
import weka.classifiers.Classifier;



public class EnsembleLearningProject {

    public static class Weather {
     @RowKey String outlook;
     String temperature; 
     String humidity;
     String windy;
     String play;
}  
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        Properties props = new Properties();
        props.setProperty("notificationAddress", "239.0.0.1");
        props.setProperty("notificationPort", "31999");
        props.setProperty("clusterName", "defaultCluster");
        props.setProperty("user", "admin");
        props.setProperty("password", "admin");
        GridStore store = GridStoreFactory.getInstance().getGridStore(props);
        
        Collection<String, Weather> coll = store.putCollection("col01", Weather.class);

        File file1 = new File("weather.csv");
                Scanner sc = new Scanner(file1);
                String data = sc.next();
 
                while (sc.hasNext()){
                        String scData = sc.next();
                        String dataList[] = scData.split(",");
                        String outlook = dataList[0];
                        String temperature = dataList[1];
                        String humidity = dataList[2];
                        String windy = dataList[3];
                        String play = dataList[4];
                                                
                        
                        Weather wc = new Weather();
                        wc.outlook = outlook;
                        wc.temperature = temperature;
                        wc.humidity = humidity;
                        wc.windy = windy;
                        wc.play = play;
    
                        
                        
                        coll.append(wc);
                 }
        
                
Query<Weather> query = coll.query("select *");
                RowSet<Weather> rs = query.fetch(false);
	        RowSet res = query.fetch();   
                
                BufferedReader bufferedReader
                = new BufferedReader(
                    new FileReader(res));
 
            // Create dataset instances
            Instances datasetInstances
                = new Instances(bufferedReader);
            
            datasetInstances.setClassIndex(datasetInstances.numAttributes()-1);

            /**
		 * AdaBoost
		 */
		AdaBoostM1 learner1 = new AdaBoostM1();
		learner1.setClassifier(new DecisionStump());
		learner1.setNumIterations(100);
		learner1.buildClassifier(datasetInstances);
                
                /**
		 * bagging
		 */
		Bagging learner2 = new Bagging();
		learner2.setClassifier(new RandomTree());
		learner2.setNumIterations(25);
		learner2.buildClassifier(datasetInstances);
                
                
                /**
		 * stacking
		 */
		Stacking learner3 = new Stacking();
		learner3.setMetaClassifier(new Logistic());
		Classifier[] classifiers = {new J48(),new NaiveBayes(),
				new RandomForest()
		};
		learner3.setClassifiers(classifiers);
		learner3.buildClassifier(datasetInstances);
                
                /**
		 * voting
		 */
		Vote vote = new Vote();
		vote.setClassifiers(classifiers);
		vote.buildClassifier(datasetInstances);
                
                Evaluation eval = new Evaluation(datasetInstances);
		eval.evaluateModel(classifiers, datasetInstances);

		System.out.println(eval.toSummaryString());

                Instance pred = datasetInstances.lastInstance();
		double answer = classifier.classifyInstance(pred);
		System.out.println(answer);

                       

    }
    
    
}
