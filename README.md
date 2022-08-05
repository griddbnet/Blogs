Random forest is a powerful module that can be used for classification predictions and performs better than a decision tree. In this article, we will focus on using the random forest module combined with a clean dataset to predict the admission likelihood of a college student to a given university. This problem is a well know prediction set in the industry. To implement our machine learning algorithm, we will be using Java programming. This article will follow the Cross-industry standard process for the data mining process. Initially, we will retrieve the data, prepare it, create our module, run it and finally test it by evaluating the results.

Full source code and data: [https://github.com/griddbnet/Blogs/tree/admissions](https://github.com/griddbnet/Blogs/tree/admissions)

## Requirements

The creation of our random forest module will use the **GridDB** database storage mechanism as a secondary memory instrument. It is essential to ensure that **GridDB** is up and running in your system by downloading and setting up **GridDB** variables. 

Make sure to run the following command to update the needed environment variables:

<div class="clipboard">
<pre><code class="language-sh">export GS_HOME=$PWD
export GS_LOG=$PWD/log
export PATH=${PATH}:$GS_HOME/bin
export CLASSPATH=$CLASSPATH:/usr/share/java/gridstore.jar
export CLASSPATH=${CLASSPATH}:/usr/share/java/weka.jar</code></pre>
</div>

Making sure you have a running **GridDB** cluster should be implemented in your Java class. This task should start by creating a **GridDB** container. Next, define the container schema that is presented in the form of our dataset columns. 

The code below is used to achieve the above tasks:

<div class="clipboard">
<pre><code class="language-java">// Manage connection to GridDB
Properties properties = new Properties();
properties.setProperty("notificationAddress", "239.0.0.1");
properties.setProperty("notificationPort", "31999");
properties.setProperty("clusterName", "cluster");
properties.setProperty("database", "public");
properties.setProperty("user", "admin");
properties.setProperty("password", "admin");

// Get Store and Container
GridStore store = GridStoreFactory.getInstance().getGridStore(properties);

store.getContainer("newContainer");

String containerName = "mContainer";

// Define container schema and columns
ContainerInfo containerInfo = new ContainerInfo();
List<ColumnInfo> columnList = new ArrayList<ColumnInfo>();

columnList.add(new ColumnInfo("key", GSType.INTEGER));
columnList.add(new ColumnInfo("Serial No.", GSType.INTEGER));
columnList.add(new ColumnInfo("GRE Score", GSType.INTEGER));
columnList.add(new ColumnInfo("TOEFL Score", GSType.INTEGER));
columnList.add(new ColumnInfo("University Rating", GSType.INTEGER));

columnList.add(new ColumnInfo("SOP", GSType.FLOAT));
columnList.add(new ColumnInfo("LOR", GSType.FLOAT));
columnList.add(new ColumnInfo("CGPA", GSType.FLOAT));

columnList.add(new ColumnInfo("Research", GSType.INTEGER));

columnList.add(new ColumnInfo("Chance of Admit", GSType.FLOAT));

containerInfo.setColumnInfoList(columnList);
containerInfo.setRowKeyAssigned(true);

Collection<Void, Row> collection = store.putCollection(containerName, containerInfo, false);
List<Row> rowList = new ArrayList<Row>();</code></pre>
</div>

In our random forest Java program, we need to import classes from four library groups:

-  `java.util`: used for reading and writing our dataset.
- `java.io`: used for input and output tasks.
-  `com.toshiba.mwcloud.gs`: used to set up and operate the **GridDB** database.
-  `weka.classifier.trees`: used to implement the random forest module. 


Find below the Java code used to implement the task explained above:

<div class="clipboard">
<pre><code class="language-java">// ---------- Java Util ---------
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;
import java.util.Scanner;
 
// ---------- Java IO ---------
import java.io.IOException;
import java.io.File;
import java.io.BufferedReader;
import java.io.FileReader;
 
// ---------- GridDB ---------
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
 
 
//----------- Weka ---------
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.classifiers.trees.RandomForest;
import weka.classifiers.Evaluation;
</code></pre>
</div>

## The Dataset

To implement the random-forest algorithms, we will use the admission dataset to predict the admission of students to a given university. The dataset is composed of **9** attributes and **400** instances.

The attributes that make up this dataset are as follows:

- **Serial No**: Numerical value used to identify the student.
- **GRE Score**: Numerical value used to quantify the GRE score of the student.
- **TOEFL Score**: Numerical value used to quantify the TOEFL score of the student.
- **University Rating**: Numerical value used to quantify the university ranking.
- **SOP**: Numerical value used to quantify the SOP score of the student.
- **LOR**: Numerical value used to quantify the LOR score of the student.
- **CGPA**: Numerical value used to quantify the cumulative GPA score of the student.
- **Research**: Binary value used to quantify the research score of the student.
- **Chance of Admit**: A binary value used to quantify the chances of admission.

Find below an extract of the dataset:

<div class="clipboard">
<pre><code class="language-sh">Serial No.,GRE Score,TOEFL Score,University Rating,SOP,LOR ,CGPA,Research,Chance of Admit 
1,337,118,4,4.5,4.5,9.65,1,0.92
2,324,107,4,4,4.5,8.87,1,0.76
3,316,104,3,3,3.5,8,1,0.72
4,322,110,3,3.5,2.5,8.67,1,0.8
5,314,103,2,2,3,8.21,0,0.65
6,330,115,5,4.5,3,9.34,1,0.9</code></pre>
</div>

To ensure we are ready for the next step, we must read the dataset file using Java. Once this task is done, the next step would be to write this dataset in our long-term storage using the **GridDB** database.

In the following code, we implement the steps described above:

<div class="clipboard">
<pre><code class="language-java">File data = new File("/home/ubuntu/griddb/gsSample/Admission_Predict.csv");
Scanner sc = new Scanner(data);
sc.useDelimiter("\n");

while (sc.hasNext())  // Returns a boolean value
{
    int i = 0;
    Row row = collection.createRow();

    String line = sc.next();
    String columns[] = line.split(",");

    int serial = Integer.parseInt(columns[0]);
    int gre = Integer.parseInt(columns[1]);
    int toefl = Integer.parseInt(columns[2]);
    int rating = Integer.parseInt(columns[3]);

    float sop = Float.parseFloat(columns[4]);
    float lor = Float.parseFloat(columns[5]);
    float cgpa = Float.parseFloat(columns[6]);

    int research = Integer.parseInt(columns[7]);

    float admitclass = Float.parseFloat(columns[8]);

    row.setInteger(0, i);
    row.setInteger(1, serial);
    row.setInteger(2, gre);
    row.setInteger(3, toefl);
    row.setInteger(4, rating);

    row.setFloat(5, sop);
    row.setFloat(6, lor);
    row.setFloat(7, cgpa);

    row.setInteger(8, research);

    row.setFloat(9, admitclass);

    rowList.add(row);

    i++;
}</code></pre>
</div>

##  Implementing a Random Forest Algorithm in Java 

This article will use a classification model that performs better than a decision tree. This algorithm is known as the random forest. To understand the choice behind this model, we need to explain the benefits of using it compared to a decision tree. First, the random forest algorithm takes advantage of creating many trees and averages out the results to give us the best possible accuracy. This makes this module a better module for numerical datasets. Second, our problem statement deals with admission prediction that relies heavily on university scores and grades. In other words, the random forest is the perfect solution for such a dataset as it is a numerical dataset. In terms of Java implementation, the `weka.classifier.trees` package will be used as it contains the random forest source code. This library will be used to implement our model and perform the evaluation process to check for validity data and accuracy.

## Write Data into GridDB 

Writing data in a long-term storage database is critical to reusability and model accessibility. This set can be done using the `List<Row>` datatypes that start by storing our values in a list and then adding them later on to our **GridDB** database. 

The following code was used to conduct the task explained in this section:

<div class="clipboard">
<pre><code class="language-java">row.setInteger(0, i);
row.setInteger(1, serial);
row.setInteger(2, gre);
row.setInteger(3, toefl);
row.setInteger(4, rating);

row.setFloat(5, sop);
row.setFloat(6, lor);
row.setFloat(7, cgpa);

row.setInteger(8, research);

row.setFloat(9, admitclass);

rowList.add(row);</code></pre>
</div>

## Store the Data in GridDB 

To store our data in the **GridDB** database, we must use the proper column names and datatypes. First, this task should be achieved by inspecting the data types of our attributes and mapping that type in our database. After examining our dataset, we found that all our attributes are numerical values. In other words, no strings or characters must be determined in our mapping process. Next, we will have to differentiate between integer values and floats. This step is very straightforward as we can observe that the university scores are all floats and other values are integers. 

The following code was used to conduct the task explained in this section:

<div class="clipboard">
<pre><code class="language-java">ContainerInfo containerInfo = new ContainerInfo();
List<ColumnInfo> columnList = new ArrayList<ColumnInfo>();

columnList.add(new ColumnInfo("key", GSType.INTEGER));
columnList.add(new ColumnInfo("Serial No.", GSType.INTEGER));
columnList.add(new ColumnInfo("GRE Score", GSType.INTEGER));
columnList.add(new ColumnInfo("TOEFL Score", GSType.INTEGER));
columnList.add(new ColumnInfo("University Rating", GSType.INTEGER));

columnList.add(new ColumnInfo("SOP", GSType.FLOAT));
columnList.add(new ColumnInfo("LOR", GSType.FLOAT));
columnList.add(new ColumnInfo("CGPA", GSType.FLOAT));

columnList.add(new ColumnInfo("Research", GSType.INTEGER));

columnList.add(new ColumnInfo("Chance of Admit", GSType.FLOAT));</code></pre>
</div>

## Retrieve the Data from GridDB 

Testing code validity should be one of our main priorities. This section will verify that the data was stored correctly in our database with no issues. To conduct this task, we will use the `SELECT` query that will return all the database values. 

The following code was used to conduct the task explained in this section:

<div class="clipboard">
<pre><code class="language-java">Container<?, Row> container = store.getContainer(containerName);

if (container == null) {
    throw new Exception("Container not found.");
}

Query<Row> query = container.query("SELECT * ");
RowSet<Row> rowset = query.fetch();</code></pre>
</div>

Once the data was retrieved from our database, the next step would be to display our data. This task will require using a Java loop and a print statement. 

The following code was used to display our **GridDB** data:

<div class="clipboard">
<pre><code class="language-java">while (rowset.hasNext()) {

    Row row = rowset.next();

    int serial = row.getInt(0);

    float gre = row.getFloat(1);
    float toefl = row.getFloat(2);
    float rating = row.getFloat(3);

    int sop = row.getInt(4);
    int lor = row.getInt(5);
    int cgpa = row.getInt(6);
    int research = row.getInt(7);

    float admitclass =row.getFloat(8);

    System.out.println(admitclass);
}</code></pre>
</div>

## Build the Random Forest 

The random forest model will be called in our Java program to determine the admission prediction of a student to a given university. Our code will start by initialising the random forest object. Next, the random forest algorithm will be configured using the default `WEKA` parameters. 

The following code was used to conduct the task explained in this section:

<div class="clipboard">
<pre><code class="language-java">RandomForest randomForest = new RandomForest();

String[] parameters = new String[14];
     
parameters[0] = "-P";
parameters[1] = "100";
parameters[2] = "-I";
parameters[3] = "100";
parameters[4] = "-num-slots";
parameters[5] = "1";
parameters[6] = "-K";
parameters[7] = "0";
parameters[8] = "-M";
parameters[9] = "1.0";
parameters[10] = "-V";
parameters[11] = "0.001";
parameters[12] = "-S";
parameters[13] = "1";
   
randomForest.setOptions(parameters);</code></pre>
</div>

Once the parameters are defined, we will have to set them up in our model. The next step of our program is to use our training data to train our model. Finally, we finish our code by evaluating the results once our model is trained using our admission dataset. 

The following code was used to conduct the task explained in this section:

<div class="clipboard">
<pre><code class="language-java">randomForest.setOptions(parameters);

randomForest.buildClassifier(datasetInstances);

Evaluation evaluation = new Evaluation(datasetInstances);

evaluation.crossValidateModel(randomForest, datasetInstances, numFolds, new Random(1));

System.out.println(evaluation.toSummaryString("\nResults\n======\n", true));</code></pre>
</div>

## Compile and Run the Code

To compile our Java program, we will locate our GridDB folder and use our command line to run our program. To compile our Java program, we will use the `javac` command. Once our program compiles, the next step is to run our code using the `java` command. 

The following commands are a representation of our explanation:

<div class="clipboard">
<pre><code class="language-sh">javac gsSample/randomForest.java

java gsSample/randomForest.java</code></pre>
</div>

## Conclusion & Results

After compiling our random forest Java program, we have to examine our results to understand the performance of our model. The summary of our results contains the accuracy of our model. In addition, our summary results include the number of attributes, instances, and the accuracy of the information in our model. Our random forest reached an accuracy of **91.05%** using only **400** instances. To explain, the number of instances of our dataset is not a representation of an extensive dataset. In other words, a bigger dataset will be more extensively helpful in increasing our accuracy. In addition, the **GridDB** database is dynamic and can easily store any additional instances we run in our model. 

The following summary output is a representation of our results and evaluation information:

<div class="clipboard">
<pre><code class="language-sh">=== Run information ===

Relation:     Admission_Predict
Instances:    400
Attributes:   9
              Serial No.
              GRE Score
              TOEFL Score
              University Rating
              SOP
              LOR 
              CGPA
              Research
              Chance of Admit 

=== Summary ===

Correctly Classified Instances          91.05 %
Incorrectly Classified Instances        8.96 %  
Mean absolute error                     0.0896
Relative absolute error                 9.19   %
Root relative squared error             34.3846 %
Total Number of Instances               400   </code></pre>
</div>

