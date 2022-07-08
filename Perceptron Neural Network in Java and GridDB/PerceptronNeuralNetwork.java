/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package perceptronneuralnetwork;

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

import java.io.BufferedReader;
import java.io.FileReader;
import weka.core.Instance;
import weka.core.Instances;
import java.math.BigDecimal;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.MultilayerPerceptron;


/**
 *
 * @author user
 */
public class PerceptronNeuralNetwork {

    public static class Perceptron {
     @RowKey int lotArea;
     int overallQual;
     int overallCond; 
     int totalBsmtSF;
     int fullBath;
     int halfBath;
     int bedroomAbvGr;
     int totRmsAbvGrd;
     int fireplaces;
     int garageArea;
     int aboveMedianPrice;
                  
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
        
        Collection<String, Perceptron> coll = store.putCollection("col01", Perceptron.class);

        File file1 = new File("housepricedata.csv");
                Scanner sc = new Scanner(file1);
                String data = sc.next();
 
                while (sc.hasNext()){
                    
                        String scData = sc.next();
                        String dataList[] = scData.split(",");
                        String lotArea = dataList[0];
                        String overallQual = dataList[1];
                        String overallCond = dataList[2];
                        String totalBsmtSF = dataList[3];
                        String fullBath = dataList[4];
                        String halfBath = dataList[5];
                        String bedroomAbvGr = dataList[6];
                        String totRmsAbvGrd = dataList[7];
                        String fireplaces = dataList[8];
                        String garageArea = dataList[9];
                        String aboveMedianPrice = dataList[10];
                        
                                                              
                        
                        Perceptron pc = new Perceptron();
                        pc.lotArea = Integer.parseInt(lotArea);
                        pc.overallQual = Integer.parseInt(overallQual);
                        pc.overallCond = Integer.parseInt(overallCond);
                        pc.totalBsmtSF = Integer.parseInt(totalBsmtSF);
                        pc.fullBath = Integer.parseInt(fullBath);
                        pc.halfBath = Integer.parseInt(halfBath);
                        pc.bedroomAbvGr = Integer.parseInt(bedroomAbvGr);
                        pc.totRmsAbvGrd = Integer.parseInt(totRmsAbvGrd);
                        pc.fireplaces = Integer.parseInt(fireplaces);
                        pc.garageArea = Integer.parseInt(garageArea);
                        pc.aboveMedianPrice = Integer.parseInt(aboveMedianPrice);
                        
                                               
                        coll.append(pc);
                 }
                Query<Perceptron> query = coll.query("select *");
                RowSet<Perceptron> rs = query.fetch(false);
	        RowSet res = query.fetch();
                
                BufferedReader bufferedReader
                = new BufferedReader(
                    new FileReader(res));
 
            // Create dataset instances
            Instances datasetInstances
                = new Instances(bufferedReader);
        
            datasetInstances.setClassIndex(datasetInstances.numAttributes()-1);
           //Instance of NN
           PerceptronNeuralNetwork mlp = new PerceptronNeuralNetwork();
           
           //Setting Parameters
           mlp.setLearningRate(0.1);
           mlp.setMomentum(0.2);
           mlp.setTrainingTime(2000);
           mlp.setHiddenLayers("3");
           
           mlp.buildClassifier(datasetInstances);
           
           Evaluation eval = new Evaluation(datasetInstances);
           eval.evaluateModel(mlp, datasetInstances);
           System.out.println(eval.toSummaryString()); //Summary of Training
           
           //display metrics
			System.out.println("Correlation: "+eval.correlationCoefficient());
			System.out.println("Mean Absolute Error: "+new BigDecimal(eval.meanAbsoluteError()));
			System.out.println("Root Mean Squared Error: "+eval.rootMeanSquaredError());
			System.out.println("Relative Absolute Error: "+eval.relativeAbsoluteError()+"%");
			System.out.println("Root Relative Squared Error: "+eval.rootRelativeSquaredError()+"%");
			System.out.println("Instances: "+eval.numInstances());
           
           Instance pred = datasetInstances.lastInstance();
		double answer = mlp.classifyInstance(pred);
		System.out.println(answer);
           


  }
    
}
