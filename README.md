AdaBoost, bagging, stacking, and voting are all ensemble learning methods. Ensemble learning is a machine learning technique in which multiple models, popularly referred to as *weak learners* or *base models*, are trained to solve a particular problem and combined to give better results. It works on the hypothesis that when weak models are combined correctly, we can end up with more accurate or robust models.

Let's discuss the ensemble learning methods that we will be using in this article:

1.  **Bagging**

Bagging stands for "Boostrap Aggregation". Bootstrapping is the technique of sampling different sets of data from a particular training set using replacement. Bagging is an ensemble learning technique where a single training algorithm is applied on different subsets of training data, and the subset sampling is done using replacement (bootstrap).

After the algorithm has been trained with all the subsets, bagging makes a prediction by aggregating the predictions made by the algorithm using the different subsets. To aggregate the outputs of base learners, the algorithm uses majority voting for classification and averaging for regression problems.

1.  **AdaBoost**

AdaBoost is a family of algorithms that convert weak learners into strong learners. This ensemble method improves the model predictions of any learning algorithm. In AdaBoost, the weak learners are trained sequentially, with each learner trying to correct its predecessor. So, the weak learners are corrected by their predecessors, converting them into strong learners. The corrections are made by reducing bias.

1.  **Stacking**

This ensemble learning method combines many machine learning algorithms through *meta-learning*. Base level algorithms are trained using a complete training dataset, and the meta-model is trained on the results of all base-level model as a feature. It improves the accuracy of predictions by using the predictions of not so good models as the input to a better model.

In this article, we will be implementing these ensemble learning methods using Java and GridDB.

## Data Description

The data to be used in this article shows different weather conditions and whether an individual can play or not. The dataset has 5 attributes namely outlook, temperature, humidity, windy, and play. The first 4 attributes are the independent variables while the last attribute is the dependent variable. The data has been stored in a CSV file named `weather.csv`.

## Store the Data in GridDB

GridDB offers a number of benefits over a CSV file. For example, queries offer a faster performance. Thus, it will be good for us to move the data from the CSV file and store it in GridDB.

Let's first import the libraries to help us achieve this in our Java code:

<div class="clipboard">
  <pre><code class="language-java">import java.io.File;
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
import com.toshiba.mwcloud.gs.GridStoreFactory;</code></pre>
</div>

GridDB stores data in containers. Let's create a static Java class to represent the GridDB container to be used for the storage of the data. We will give it the name `Weather`:

<div class="clipboard">
  <pre><code class="language-java">public static class Weather {
     @RowKey String outlook;
     String temperature; 
     String humidity;
     String windy;
     String play;
}  </code></pre>
</div>

Each of the above variables represents a column in the GridDB container. It is similar to a SQL table.

Next, we can connect to GridDB from Java. We will have to provide authentication and other details for this to be successful. The following code demonstrates this:

<div class="clipboard">
  <pre><code class="language-java">Properties props = new Properties();
        props.setProperty("notificationAddress", "239.0.0.1");
        props.setProperty("notificationPort", "31999");
        props.setProperty("clusterName", "defaultCluster");
        props.setProperty("user", "admin");
        props.setProperty("password", "admin");
        GridStore store = GridStoreFactory.getInstance().getGridStore(props);</code></pre>
</div>

See that we have specified the name of the cluster, the username as well as the password of the user who is connecting to GriDB. Now that we've established the connection, let's select the container we want to work on:

<div class="clipboard">
  <pre><code class="language-java">Collection&lt;String, Weather> coll = store.putCollection("col01", Weather.class);</code></pre>
</div>

We will be using the name `coll` to refer to the container.

Let's now write data from the CSV file into the GridDB container:

<div class="clipboard">
  <pre><code class="language-java"> File file1 = new File("weather.csv");
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
                 }</code></pre>
</div>

The code will insert the data into the GridDB container.

## Retrieve the Data

We want to use the data to build different machine learning models using ensemble learning methods. This means that we must pull it from the GridDB container. The following code will help us achieve this:

<div class="clipboard">
  <pre><code class="language-java">Query&lt;weather> query = coll.query("select *");
                RowSet&lt;/weather>&lt;weather> rs = query.fetch(false);
            RowSet res = query.fetch();&lt;/weather></code></pre>
</div>

We have used the `select *` statement to select all data stored in the GridDB container named `Weather`.

## Fit Machine Learning Models

We can now fit machine learning models using the data and ensemble learning methods. The methods to be used include AdaBoost, Bagging, Stacking, and Voting.

Let's first import libraries from the Weka API to help us implement these methods:

<div class="clipboard">
  <pre><code class="language-java">import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.meta.Bagging;
import weka.core.Instances;
import java.io.BufferedReader;
import java.io.FileReader;
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
import weka.classifiers.Classifier;</code></pre>
</div>

Next, we create a buffered reader for the dataset:

<div class="clipboard">
  <pre><code class="language-java">BufferedReader bufferedReader
                = new BufferedReader(
                    new FileReader(res));
 
            // Create dataset instances
            Instances datasetInstances
                = new Instances(bufferedReader);</code></pre>
</div>

Let's instantiate the`AdaBoost()` function of the Weka API to help us implement a machine learning model on the data using the AdaBoost algorithm:

<div class="clipboard">
  <pre><code class="language-java">datasetInstances.setClassIndex(datasetInstances.numAttributes()-1);

            /**
         * AdaBoost
         */
        AdaBoostM1 learner1 = new AdaBoostM1();
        learner1.setClassifier(new DecisionStump());
        learner1.setNumIterations(100);
        learner1.buildClassifier(datasetInstances);</code></pre>
</div>

Next, we will implement a machine learning model on the dataset using the Bagging algorithm:

<div class="clipboard">
  <pre><code class="language-java">/**
         * bagging
         */
        Bagging learner2 = new Bagging();
        learner2.setClassifier(new RandomTree());
        learner2.setNumIterations(25);
        learner2.buildClassifier(datasetInstances);</code></pre>
</div>

Let's use the `Stacking()` method of the Weka API to implement a machine learning model using the Stacking algorithm:

<div class="clipboard">
  <pre><code class="language-java">/**
         * stacking
         */
        Stacking learner3 = new Stacking();
        learner3.setMetaClassifier(new Logistic());
        Classifier[] classifiers = {new J48(),new NaiveBayes(),
                new RandomForest()
        };
        learner3.setClassifiers(classifiers);
        learner3.buildClassifier(datasetInstances);</code></pre>
</div>

In the above code, we are stacking three models built using the decision tree, Naive Bayes, and Random Forest algorithms.

And finally, let's demonstrate how voting works. We want to aggregate the outputs of the above base learners or classifiers, that is, the Decision Tree, Naive Bayes, and Random Forest learners. We will use the `Vote()` function of the Weka API as shown below:

<div class="clipboard">
  <pre><code class="language-java"> /**
         * voting
         */
        Vote vote = new Vote();
        vote.setClassifiers(classifiers);
        vote.buildClassifier(datasetInstances);</code></pre>
</div>

To evaluate any of the models, you can use the`Evaluation()` function provided by the Weka API. But first, import the function as shown below:

<div class="clipboard">
  <pre><code class="language-java">import weka.classifiers.Evaluation;</code></pre>
</div>

You can then instantiate the function and get the evaluation metrics:

<div class="clipboard">
  <pre><code class="language-java">Evaluation eval = new Evaluation(datasetInstances);
        eval.evaluateModel(classifiers, datasetInstances);</code></pre>
</div>

We can then print out a summary of the evaluation metrics:

<div class="clipboard">
  <pre><code class="language-java">System.out.println(eval.toSummaryString());</code></pre>
</div>

## Make a Prediction

We can make a prediction to know whether one will play or not. We can use the last instance of the dataset for this. We will use the `ClassifyInstance()` function of Weka as shown below:

<div class="clipboard">
  <pre><code class="language-java">Instance pred = datasetInstances.lastInstance();
        double answer = classifier.classifyInstance(pred);
        System.out.println(answer);</code></pre>
</div>

## Compile and Run the Model

To compile and run the model, you will need the Weka API. Download it from the following URL:

http://www.java2s.com/Code/Jar/w/weka.htm

Next, login as the `gsadm` user. Move your `.java` file to the `bin` folder of your GridDB located in the following path:

/griddb_4.6.0-1_amd64/usr/griddb-4.6.0/bin

Run the following command on your Linux terminal to set the path for the gridstore.jar file:

<div class="clipboard">
  <pre><code class="language-bash">export CLASSPATH=$CLASSPATH:/home/osboxes/Downloads/griddb_4.6.0-1_amd64/usr/griddb-4.6.0/bin/gridstore.jar</code></pre>
</div>

Next, use the following command to compile your `.java` file:

<div class="clipboard">
  <pre><code class="language-bash">javac -cp weka-3-7-0/weka.jar EnsembleLearningProject.java</code></pre>
</div>

Run the .class file that is generated by running the following command:

<div class="clipboard">
  <pre><code class="language-bash">java -cp .:weka-3-7-0/weka.jar EnsembleLearningProject</code></pre>
</div>

The project code should run successfully. The output of the prediction should be 1.0, which means that we can play.