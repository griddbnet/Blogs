## Introduction

Opinion mining, also known as *sentiment analysis*, is a hot topic in the field of artificial intelligence today. Opinion mining identifies the emotional tone behind a specified text. Organizations use opinion mining to determine and categorize user opinions about a service, a product, or an idea. It uses artificial intelligence, data mining, and machine learning to mine text for subjective information. 

With sentiment analysis, organizations are able to gather insights from unstructured text obtained from online sources such as support tickets, blog posts, emails, comments, forums, social media channels, and web chats. 

In this article, we will discuss how to perform opinion mining using Java and GridDB. The goal will be to tell whether a customer review on a product is positive or negative. 

## Prerequisites 

You will need to download [GridDB](https://docs.griddb.net/gettingstarted/using-apt/), java, and [weka](https://www.weka.io/).

## Data Description

The dataset to be used in this article shows the reviews of products in different categories such as books, cars, phones, etc. 

The data has been stored in a CSV (Comma Separated Values) file named "Reviews.csv". There are two categories in the dataset, sentence and target. 

## Store the Data in GridDB

We want to store the data in GridDB. GridDB offers a number of benefits over a CSV file including data organization and improved query performance. 

Let's first import the libraries to help us store the data in GridDB:

<div class="clipboard">
<pre><code class="language-java">import java.io.File;
import java.util.Scanner;
import java.io.IOException;
import java.util.Properties;

import com.toshiba.mwcloud.gs.Query;
import com.toshiba.mwcloud.gs.RowKey;
import com.toshiba.mwcloud.gs.RowSet;
import com.toshiba.mwcloud.gs.GridStore;
import com.toshiba.mwcloud.gs.Collection;
import com.toshiba.mwcloud.gs.GSException;
import com.toshiba.mwcloud.gs.GridStoreFactory;</code></pre>
</div>

The data will be stored in a GridDB container. Let us create a static Java class to represent this container:

<div class="clipboard">
<pre><code class="language-java">public static class OpinionData{
    
    @RowKey String sentence;
     String target;
    
    }</code></pre>
</div>

We have defined two variables in the above class. They correspond to the two columns of the GridDB container where the data is to be stored. 

We now need to connect Java to GridDB. This requires us to specify the credentials of our GridDB database in our Java code. The following code demonstrates this:

<div class="clipboard">
<pre><code class="language-java">        Properties props = new Properties();
        props.setProperty("notificationMember", "127.0.0.1:10001");
        props.setProperty("clusterName", "myCluster");
        props.setProperty("user", "admin");
        props.setProperty("password", "admin");
        GridStore store = GridStoreFactory.getInstance().getGridStore(props);</code></pre>
</div>

Note that you must specify the correct credentials, otherwise, the connection will fail. 

Let's now select the GridDB container to be used and create a new instance:

<div class="clipboard">
<pre><code class="language-java">        Collection<String, OpinionData> coll = store.putCollection("col01", OpinionData.class);</code></pre>
</div>

We can now move the data from the CSV file into GriDB:

<div class="clipboard">
<pre><code class="language-java">File file1 = new File("Reviews.csv");
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
                 }</code></pre>
</div>
	
The data will be inserted into the GridDB container. 

## Retrieve the Data


We now want to retrieve the data and use it for opinion mining. We will use the `select *` statement for this as shown below:

<div class="clipboard">
<pre><code class="language-java">Query<OpinionData> query = coll.query("select *");
   RowSet<OpinionData> rs = query.fetch(false);
   RowSet res = query.fetch();</code></pre>
</div>

The `select *` statement selects all the data stored in the specified GridDB container. 

## Implement a Sentiment Classifier

It's now time to implement a text classifier using the loaded data and use it to perform opinion analysis. 

Let's first import the java libraries to help us implement the classifier:

<div class="clipboard">
<pre><code class="language-java">import java.io.*;
import weka.core.*;
import java.util.List;
import java.util.ArrayList;
import weka.classifiers.meta.FilteredClassifier;</code></pre>
</div>

We can now create a buffered reader for the dataset:

<div class="clipboard">
<pre><code class="language-java">BufferedReader bufferedReader
                = new BufferedReader(
                    new FileReader(res));
 
            // Create dataset instances
            Instances datasetInstances
                = new Instances(bufferedReader);</code></pre>
</div>

Let's now implement a filtered classifier using the dataset:

<div class="clipboard">
<pre><code class="language-java">    datasetInstances.setClassIndex(1);
    FilteredClassifier classifier = new FilteredClassifier();
    classifier.buildClassifier(datasetInstances);
    System.out.println(classifier);</code></pre>
</div>

We have set the class of the dataset to the second attribute. We have then created an instance of FilteredClassifier and used it to build a classifier using the dataset. 
The dataset should have the text as the first attribute and the class as the second attribute. 

## Classify Text

We can now use our ready model to make predictions. The purpose of making the predictions is to tell whether a review is positive or negative. 

The following code shows how to classify the last instance of the dataset:

<div class="clipboard">
<pre><code class="language-java">Instance pred = datasetInstances.lastInstance();
double answer = classifier.classifyInstance(pred);
System.out.println("Class predicted: " + pred.classAttribute().value((int) answer));</code></pre>
</div>

## Compile and Run the Model

To compile and run the model, you will need the Weka API. Download it from the following URL:

http://www.java2s.com/Code/Jar/w/weka.htm

Next, login as the `gsadm` user. Move your `.java` file to the `bin` folder of your GridDB located in the following path:

/griddb_4.6.0-1_amd64/usr/griddb-4.6.0/bin

Run the following command on your Linux terminal to set the path for the gridstore.jar file:

<div class="clipboard">
<pre><code class="language-sh">export CLASSPATH=$CLASSPATH:/home/osboxes/Downloads/griddb_4.6.0-1_amd64/usr/griddb-4.6.0/bin/gridstore.jar</code></pre>
</div>

Next, use the following command to compile your `.java` file:

<div class="clipboard">
<pre><code class="language-sh">javac -cp weka-3-7-0/weka.jar OpinionMining.java</code></pre>
</div>

Run the .class file that is generated by running the following command:

<div class="clipboard">
<pre><code class="language-sh">java -cp .:weka-3-7-0/weka.jar OpinionMining</code></pre>
</div>

The sentiment classifier correctly classified the review as positive.