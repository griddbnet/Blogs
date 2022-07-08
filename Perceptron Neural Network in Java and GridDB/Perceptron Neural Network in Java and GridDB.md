
## Introduction

A multilayer perceptron refers to a feed forward artificial neural network model that maps a set of input data to a set of appropriate outputs. 

A multilayer perceptron is made up of many layers of nodes in a directed graph, with every layer being fully connected to the next layer. 

In this article, we will be implementing a perceptron neural network model in Java and GridDB. The purpose of the model will be to predict whether house prices are above or below the median house price value. 

## Data Description

The dataset to be used has 11 features. The first 10 attributes of the dataset will be the input features. Each of these inputs describe a feature of a house. 

The last attribute of the dataset is the feature that we will be predicting, which describes whether a house price is above the median price or not. A value of 1 means that the house price is above the median value while a value of 0 means that the house price is below the median value. 

The data has been stored in a CSV file named `housepricedata.csv`. 

## Store the Data in GridDB

Although we can still use the data from the file, GridDB offers a number of benefits over the CSV file. For instance, GridDB can return query results faster than a CSV file. 

That is why we have opted to store the data in GridDB. 

Let's first import the libraries to help us store the data in GridDB:

```java
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
```

GridDB groups data into containers. Let us create a static Java class to represent the GridDB container where the data will be stored:

```java
public static class Perceptron {
     @RowKey String lotArea;
     String overallQual;
     String overallCond; 
     String totalBsmtSF;
     String fullBath;
     String halfBath;
     String bedroomAbvGr;
     String totRmsAbvGrd;
     String fireplaces;
     String garageArea;
     String aboveMedianPrice;
                  
}    
```

See the above static class as a SQL table. Every variable represents a single column in the GridDB container. 

We can now connect to GridDB from Java. This is possible provided the authentication process runs successfully. The connection can be done using the following code:

```java
 Properties props = new Properties();
        props.setProperty("notificationAddress", "239.0.0.1");
        props.setProperty("notificationPort", "31999");
        props.setProperty("clusterName", "defaultCluster");
        props.setProperty("user", "admin");
        props.setProperty("password", "admin");
        GridStore store = GridStoreFactory.getInstance().getGridStore(props);
```

Ensure that you use the correct authentication details depending on your GridDB settings. 

Let us now select the container into which the data is to be inserted:

```java
        Collection<String, Perceptron> coll = store.putCollection("col01", Perceptron.class);
```

We have created an instance of the container. This instance can now be used to refer to the container. 

Let's now pull the data from the CSV file and insert it into GridDB:

```java
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
```

The above code will pull the data from the CSV file and insert it into the GridDB container. 

## Retrieve the Data

We want to use the data to implement a Perceptron neural network model. So, let's pull the data from the GridDB container:

```java
Query<Perceptron> query = coll.query("select *");
   RowSet<Perceptron> rs = query.fetch(false);
   RowSet res = query.fetch();
```

The `select *` statement helped us to select all the data stored in the GridDB container. 

## Fit the Perceptron Neural Network Model

We can now create a machine learning model using the dataset. Let's first import the libraries to be used for this:

```java
import java.io.BufferedReader;
import java.io.FileReader;
import weka.core.Instance;
import weka.core.Instances;
import java.math.BigDecimal;
import weka.classifiers.Evaluation;
import weka.classifiers.functions.MultilayerPerceptron;
```

Create a buffered reader for the dataset:

```java
BufferedReader bufferedReader
                = new BufferedReader(
                    new FileReader(res));
 
            // Create dataset instances
            Instances datasetInstances
                = new Instances(bufferedReader);
 ```

Next, we create an instance of the `PerceptronNeuralNetwork` class:

```java
datasetInstances.setClassIndex(datasetInstances.numAttributes()-1);
           //Instance of NN
            PerceptronNeuralNetwork mlp = new PerceptronNeuralNetwork();
```
		   
Let's now set the parameters for the neural network:

```
  //Setting Parameters
           mlp.setLearningRate(0.1);
           mlp.setMomentum(0.2);
           mlp.setTrainingTime(2000);
           mlp.setHiddenLayers("3");
 ```

The above code has helped us to set parameters such as the learning rate, momentum, training time, and the number of hidden layers for the model.

Let's now build the classifier:

```
           mlp.buildClassifier(datasetInstances);
```

## Evaluate the Model

Let's now evaluate the model to see how it performs. We will use the `Evaluation()` function for this:

```java
 Evaluation eval = new Evaluation(datasetInstances);
           eval.evaluateModel(mlp, datasetInstances);
           System.out.println(eval.toSummaryString()); //Summary of Training
```
	
We can now display the evaluation metrics:

```java
//display metrics
			System.out.println("Correlation: "+eval.correlationCoefficient());
			System.out.println("Mean Absolute Error: "+new BigDecimal(eval.meanAbsoluteError()));
			System.out.println("Root Mean Squared Error: "+eval.rootMeanSquaredError());
			System.out.println("Relative Absolute Error: "+eval.relativeAbsoluteError()+"%");
			System.out.println("Root Relative Squared Error: "+eval.rootRelativeSquaredError()+"%");
			System.out.println("Instances: "+eval.numInstances());
```

## Make a Prediction

Let's use the last instance of the dataset to make a prediction. The goal is to know whether the house price is above or below the median price. A result of 1 means the house price is above the median price. A value of 0 means the house price is below the median price. 

```java
Instance pred = datasetInstances.lastInstance();
		double answer = mlp.classifyInstance(pred);
		System.out.println(answer);
```

## Compile and Run the Model

To compile and run the model, you will need the Weka API. Download it from the following URL:

http://www.java2s.com/Code/Jar/w/weka.htm

Next, login as the `gsadm` user. Move your `.java` file to the `bin` folder of your GridDB located in the following path:

/griddb_4.6.0-1_amd64/usr/griddb-4.6.0/bin

Run the following command on your Linux terminal to set the path for the gridstore.jar file:

```
export CLASSPATH=$CLASSPATH:/home/osboxes/Downloads/griddb_4.6.0-1_amd64/usr/griddb-4.6.0/bin/gridstore.jar
```

Next, use the following command to compile your `.java` file:

```
javac -cp weka-3-7-0/weka.jar PerceptronNeuralNetwork.java
```

Run the .class file that is generated by running the following command:

```
java -cp .:weka-3-7-0/weka.jar PerceptronNeuralNetwork
```

The model returned `0` for the prediction. This means that the house price is below the median price. 
 




		   












































