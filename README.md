# Diabetes Prediction using Machine Learning, Java, and GridDB

This article will cover the health care concern of diabetes that is driving the lifestyle of many people worldwide. This article will cover the usage of machine learning models to create a predictive system. This model will use random-forest to predict if patients have diabetes or not. The article will outline the requirements needed to set up our database **GridDB**. Following that, we will briefly describe our dataset and model. To finish off, we will interpret the results and come up with our conclusion.

## Requirements

The **GridDB** database storage system will be a backup memory resource while building our random forest module. **GridDB** must be downloaded and configured in your operating system to be fully functional.

Make sure to run the following command to update the needed environment variables:

```
export GS_HOME=$PWD
export GS_LOG=$PWD/log
export PATH=${PATH}:$GS_HOME/bin
export CLASSPATH=$CLASSPATH:/usr/share/java/gridstore.jar
export CLASSPATH=${CLASSPATH}:/usr/share/java/weka.jar
```

In your Java class, you should ensure a running **GridDB** cluster. This task should begin with the creation of a **GridDB** container. This task should include the container schema, represented by our dataset columns.

The code below is used to achieve the above tasks:

```java
// Manage connection to GridDB
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
columnList.add(new ColumnInfo("Pregnancies", GSType.INTEGER));
columnList.add(new ColumnInfo("Glucose", GSType.INTEGER));
columnList.add(new ColumnInfo("BloodPressure", GSType.INTEGER));
columnList.add(new ColumnInfo("SkinThickness", GSType.INTEGER))
columnList.add(new ColumnInfo("Insulin", GSType.INTEGER));
columnList.add(new ColumnInfo("BMI", GSType.FLOAT));
columnList.add(new ColumnInfo("DiabetesPedigreeFunction", GSType.FLOAT));
columnList.add(new ColumnInfo("Age", GSType.INTEGER));
columnList.add(new ColumnInfo("Outcome", GSType.INTEGER));
containerInfo.setColumnInfoList(columnList);
containerInfo.setRowKeyAssigned(true);
Collection<Void, Row> collection = store.putCollection(containerName, containerInfo, false);
List<Row> rowList = new ArrayList<Row>();
```

In our random-forest Java program, we need to import classes from four library groups:

-  `java.util`: used to read our dataset and write it in our database.
- `java.io`: used for input and output operations that will be used in our program.
-  `com.toshiba.mwcloud.gs`: used to set up and operate the **GridDB** database.
-  `weka.classifier.trees`: used to implement and configure our random forest module. 


Find below the Java code used to implement the task explained above:

```java
// ---------- Java Util ---------
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

// ----------- Weka ---------
import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.classifiers.trees.RandomForest;
import weka.classifiers.Evaluation;
```

## The Dataset

To implement the random-forest algorithms, we will use the **diabetes** dataset to predict whether an individual has diabetes based on diagnostic measurements. The dataset is composed of **9** attributes and **768** instances.

The attributes covered in this dataset are as follows:

- **Pregnancies**: Numerical value used to identify the historical data of pregnancy.
- **Glucose**: Numerical value used to quantify the glucose level.
- **Blood Pressure**: Numerical value used to quantify blood pressure.
- **Skin Thickness**: Numerical value used to quantify skin thickness.
- **Insulin**: Numerical value used to quantify the insulin level.
- **BMI**: Numerical value used to quantify the Body Mass Index.
- **Diabetes Pedigree Function**: Numerical value used to quantify the diabetes pedigree function.
- **Age**:  Numerical value used to quantify a patient's age in years.
- **Outcome**: A binary value used to determine whether an individual has diabetes (**1**) or not (**0**).

Find below an extract of the dataset:

```
Pregnancies,Glucose,BloodPressure,SkinThickness,Insulin,BMI,DiabetesPedigreeFunction,Age,Outcome
6,148,72,35,0,33.6,0.627,50,1
1,85,66,29,0,26.6,0.351,31,0
8,183,64,0,0,23.3,0.672,32,1
1,89,66,23,94,28.1,0.167,21,0
0,137,40,35,168,43.1,2.288,33,1
5,116,74,0,0,25.6,0.201,30,0
3,78,50,32,88,31,0.248,26,1
10,115,0,0,0,35.3,0.134,29,0
2,197,70,45,543,30.5,0.158,53,1
8,125,96,0,0,0,0.232,54,1
```

We must use Java to read the **diabetes** dataset file to confirm we are ready for the next step. Once this task is completed, we will use the **GridDB** database to store this dataset in our long-term storage.

In the following code, we implement the steps described above:

```java
while (sc.hasNext()){  
    int i = 0;
    Row row = collection.createRow();

    String line = sc.next();
    String columns[] = line.split(",");

    int pregnancies = Integer.parseInt(columns[0]);
    int glucose = Integer.parseInt(columns[1]);
    int bloodpressure = Integer.parseInt(columns[2]);
    int skinthickness = Integer.parseInt(columns[3]);
    int insulin = Integer.parseInt(columns[4]);
    float bmi = Float.parseFloat(columns[5]);
    float diabetespedigreefunction = Float.parseFloat(columns[6]);
    int age = Integer.parseInt(columns[7]);
    int outcome = Integer.parseInt(columns[8]);

    row.setInteger(0, i);
    row.setInteger(1, pregnancies);
    row.setInteger(2, glucose);
    row.setInteger(3, bloodpressure);
    row.setInteger(4, skinthickness);
    row.setInteger(5, insulin);
    row.setFloat(6, bmi);
    row.setFloat(7, diabetespedigreefunction);
    row.setInteger(8, age);
    row.setInteger(9, outcome);

    rowList.add(row);

    i++;
}
```

##  Implementing a Random Forest Algorithm in Java 

This article will employ a classification model that outperforms a decision tree. The random forest algorithm is the name given to this algorithm. To understand why this model was chosen, we must first explain the advantages of using it over a decision tree. First, the random forest algorithm takes advantage of the fact that many trees are created and averages the results to provide the best possible accuracy. This improves the module's suitability for numerical datasets. Second, our problem statement is diabetes prediction, which relies heavily on diagnostic measurements. In other words, because this is a numerical dataset, the random forest is the ideal solution. The `weka.classifier.trees` package will be used in our Java implementation because it contains the random-forest source code. This library will be used to implement our model and run the evaluation process to ensure that the data is valid and accurate.

## Write Data into GridDB 

A long-term storage database is critical for model reusability and accessibility. This set can be accomplished by using the `List<Row>` datatypes, which begin by storing our values in a list and then adding them to our **GridDB** database later on.

The following code was used to conduct the task explained in this section:

```java
row.setInteger(0, i);
row.setInteger(1, pregnancies);
row.setInteger(2, glucose);
row.setInteger(3, bloodpressure);
row.setInteger(4, skinthickness);
row.setInteger(5, insulin);
row.setFloat(6, bmi);
row.setFloat(7, diabetespedigreefunction);
row.setInteger(8, age);
row.setInteger(9, outcome);

rowList.add(row);
```

## Store the Data in GridDB 

We must use the correct column names and datatypes to store our data in the **GridDB** database. First, we should complete this task by inspecting the data types of our attributes and mapping them in our database. After examining our dataset, we discovered that all our features are numerical values. In other words, in our mapping process, no strings or characters must be determined. Following that, we must distinguish between integer and float values. This step is simple because we can see that the BMI and diabetes pedigree function scores are all floats and the other values are integers.

The following code was used to conduct the task explained in this section:

```java
// Define container schema and columns
ContainerInfo containerInfo = new ContainerInfo();
List<ColumnInfo> columnList = new ArrayList<ColumnInfo>();

columnList.add(new ColumnInfo("key", GSType.INTEGER));
columnList.add(new ColumnInfo("Pregnancies", GSType.INTEGER));
columnList.add(new ColumnInfo("Glucose", GSType.INTEGER));
columnList.add(new ColumnInfo("BloodPressure", GSType.INTEGER));
columnList.add(new ColumnInfo("SkinThickness", GSType.INTEGER))
columnList.add(new ColumnInfo("Insulin", GSType.INTEGER));

columnList.add(new ColumnInfo("BMI", GSType.FLOAT));
columnList.add(new ColumnInfo("DiabetesPedigreeFunction", GSType.FLOAT));

columnList.add(new ColumnInfo("Age", GSType.INTEGER));
columnList.add(new ColumnInfo("Outcome", GSType.INTEGER));
```

## Retrieve the Data from GridDB 

In this section, we will verify the code validity. This section will ensure that the data was correctly stored in our database and that there were no errors. We will use the `SELECT` query to complete this task, which returns all database values.

The following code was used to conduct the task explained in this section:

```java
Container<?, Row> container = store.getContainer(containerName);

if (container == null) {
    throw new Exception("Container not found.");
}

Query<Row> query = container.query("SELECT * ");
RowSet<Row> rowset = query.fetch();
```

Once the data was retrieved from our database, the next step would be to print our data. This task will require using a loop and a `System.out.println()` function. 

The following code was used to display our **GridDB** data:

```java
// Print GridDB data
while ( rowset.hasNext() ) {
    Row row = rowset.next();

    int pregnancies = row.getInt(0);
    int glucose = row.getInt(1);
    int bloodpressure = row.getInt(2);
    int skinthickness = row.getInt(3);
    int insulin = row.getInt(4);
    float bmi = row.getFloat(5);
    float diabetespedigreefunction = row.getFloat(6);
    int age = row.getFgetIntloat(7);
    int outcome = row.getInt(8);

    System.out.println(pregnancies);
    System.out.println(glucose);
    System.out.println(bloodpressure);
    System.out.println(skinthickness);
    System.out.println(insulin);
    System.out.println(bmi);
    System.out.println(diabetespedigreefunction);
    System.out.println(age);
    System.out.println(outcome);
}
```

## Build the Random Forest 

In our Java program, the random forest model will forecast the possibility of a patient's diabetes diagnosis. Our code will first initialise the random forest object. The random forest algorithm will then be set up using the standard **WEKA** parameters.

The following code was used to conduct the task explained in this section:

```java
RandomForest randomForest = new RandomForest();

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
   
randomForest.setOptions(parameters);
```

Once the random forest arguments have been defined, they must be entered into our model. The next step in our program is to train our model using our training dataset. Finally, we complete our code by evaluating the results of our model after it has been prepared using our diabetes dataset.

The following code was used to conduct the task explained in this section:

```java
randomForest.setOptions(parameters);

randomForest.buildClassifier(datasetInstances);

Evaluation evaluation = new Evaluation(datasetInstances);

evaluation.crossValidateModel(randomForest, datasetInstances, numFolds, new Random(1));

System.out.println(evaluation.toSummaryString("\nResults\n======\n", true));
```

## Compile and Run the Code

To compile our Java program, we will navigate to our **GridDB** folder and run it from the command line. The `javac` command should be used to run the compiler to construct our code. After compiling our program, we will run it with the `java` command.

The following commands are a representation of our explanation:

```
javac gsSample/randomForest.java

java gsSample/randomForest.java
```

## Conclusion & Results

The final section of our article is the understanding of our results. To easily digest how well our random forest performed in predicting if a patient has diabetes or not, we will have to filter out the random-forest output. The main number we shall focus on in this section is the model accuracy. This number represents how well our model can perform in a real-world scenario. Our random forest diabetes predictor reached an accuracy of **90.05%**. To explain, this is considered a very high accuracy as it is above the 90% limit. For future development, our model will fully use the **GridDB** database as it provides an easy input-output data interface and an incredible speed of data retrieval. 

The following summary output is a representation of our results and evaluation information:

```
=== Run information ===

Relation:     diabetes
Instances:    768
Attributes:   9
              Pregnancies
              Glucose
              BloodPressure
              SkinThickness
              Insulin
              BMI
              DiabetesPedigreeFunction
              Age
              Outcome
Test mode:    10-fold cross-validation

=== Classifier model (full training set) ===

RandomForest

Bagging with 100 iterations and base learner

Time taken to build model: 0.13 seconds

=== Summary ===

Correctly Classified Instances          	90.05%
Incorrectly Classified Instances        	9.95%  
Mean absolute error                     	0.0995
Relative absolute error                 	9.51%
Total Number of Instances              		768
```

