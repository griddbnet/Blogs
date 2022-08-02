## Introduction

Text data can be found everywhere, including in emails, chat conversations, social media, and websites. Text data is full of information, but extracting this information can be difficult due to its unstructured nature. Extracting insights from unstructured data can  be expensive and time-consuming as much time will be spent sorting the data. 

Text classifiers that use Natural Language Processing (NLP) techniques provide an alternative way to structure data in a fast, scalable, and cost-effective way. 

The process of text classification involves organizing text into groups. It is also known as *Text Tagging* or *Text Categorization*. With NLP, it is possible to analyze text data and assign it to pre-defined categories based on the content. 

In this article, we will implement a text classifier using Java and GridDB. The purpose of the text classifier will be to classify an email as either spam or ham. 

## Data Description

The data to be used shows whether an email is spam or ham. The data has been stored in a comma separated values (CSV) file named `smsspam.csv`. There are two fields in the data namely `spamclass` and `text`. The first field shows whether the email is spam or ham and it takes only two values, `spam`, and `ham`. The second field shows the email text. 

## Store the Data in GridDB

It is possible for us to use the data directly from the CSV file. However, GridDB comes with a number of benefits including data organization and improved query performance. That's why we will move the data from the CVS file into GridDB. 

Let's start by importing a number of libraries to help us in storing the data in GridDB:

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
public static class EmailClassification{
    
    @RowKey String spamclass;
     String text;
      
    
    }
```

The above static class has two variables. These two variables correspond to the two columns of the GridDB container. 

To move the data to GridDB, we must connect Java to GridDB. The following code can help us to achieve this:

```java
        Properties props = new Properties();
        props.setProperty("notificationAddress", "239.0.0.1");
        props.setProperty("notificationPort", "31999");
        props.setProperty("clusterName", "defaultCluster");
        props.setProperty("user", "admin");
        props.setProperty("password", "admin");
        GridStore store = GridStoreFactory.getInstance().getGridStore(props);
```

Replace the above details with the specifics of your GridDB installation. 

We can now select the container into which we need to store the data:

```java
        Collection<String, EmailClassification> coll = store.putCollection("col01", EmailClassification.class);
```

We have created an instance of the container. This instance can now be used to refer to the container. 

Let's now pull the data from the CSV file and insert it into GridDB:

```java
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
```

The above code will pull the data from the CSV file and insert it into the GridDB container. 

## Retrieve the Data

We want to use the data for text classification. So, let us retrieve the data from the GridDB container:

```java
Query<EmailClassification> query = coll.query("select *");
   RowSet<EmailClassification> rs = query.fetch(false);
   RowSet res = query.fetch();
```

The `select *` statement helped us to select all the data stored in the GridDB container. 

## Implement the Text  Classify

Now that our data is ready, we can go ahead and implement the text classifier. Let's first import the libraries that we will use to implement the text classifier:

```java
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

We can now implement the text classifier. We will combine a `StringToWordVector` filter (to help us represent our text in the form of feature vectors) with a `NaiveBayes` classifier (to facilitate learning). 

```java
datasetInstances.setClassIndex(0);
   StringToWordVector filter = new StringToWordVector();
   filter.setAttributeIndices("last");
   FilteredClassifier classifier = new FilteredClassifier();
   classifier.setFilter(filter);
   classifier.setClassifier(new NaiveBayes());
   classifier.buildClassifier(datasetInstances);
   System.out.println(classifier);
```
 
The class of the dataset has been set to the first attribute. We have then created a filter and set the attribute which is to be transformed from text into a feature factor. We have then created an object of the `FilteredClassifier` class and added the previous filter as well as a new `NaiveBayes` classifier to it. 
The dataset should have the class as the first attribute and the text as the second attribute. 

## Evaluate the Model

We can now evaluate the model to see its performance statistics. The following code can help us to achieve this:

```java
Evaluation eval = new Evaluation(datasetInstances);
   eval.crossValidateModel(classifier, datasetInstances, 4, new Random(1));
   System.out.println(eval.toSummaryString());
```

The code will return the evaluation metrics for the text classifier. 

## Classify Text

Now that our model is ready, we can use it to make predictions. The purpose of making predictions should be to classify emails as either spam or ham. 

Let us classify the last instance of the dataset as either spam or ham. The following code demonstrates this:

```java
Instance pred = datasetInstances.lastInstance();
      double answer = classifier.classifyInstance(pred);
      System.out.println("Class predicted: " + pred.classAttribute().value((int) answer));
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
javac -cp weka-3-7-0/weka.jar TextClassification.java
```

Run the .class file that is generated by running the following command:

```
java -cp .:weka-3-7-0/weka.jar TextClassification
```

The text classifier correctly classified the email as `ham`.  
















