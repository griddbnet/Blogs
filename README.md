Studying and Forecasting web traffic using Python and GridDB

In the last few years, the time-series database category has experienced the fastest growth. Both established and emerging technology sectors have been producing an increasing amount of time-series data.

The quantity of sessions in a given period of time is known as web traffic, and it varies greatly depending on the time of day, day of the week, and other factors. The amount of web traffic a platform can handle is determined by the size of the servers that host the platform.

Based on historical visitor volume data or historical web traffic data, you can dynamically allocate many servers. And that brings us to the data science challenge, which is basically analysing and forecasting the volume of sessions or web traffic based on past data.

The outline of the tutorial is as follows:

 1. Dataset overview
 2. Importing required libraries
 3. Loading the dataset
 4. Analysing with data visualization
 5. Forecasting
 6. Conclusion

# Prerequisites and Environment setup

This tutorial is carried out in Anaconda Navigator (Python version – 3.8.5) on Windows Operating System. The following packages need to be installed before you continue with the tutorial –

1. Pandas

2. NumPy

3. re

4. Matplotlib

5. Seaborn

6. griddb_python

7. fbprophet

You can install these packages in Conda’s virtual environment using `conda install package-name`. In case you are using Python directly via terminal/command prompt, `pip install package-name` will do the work.

### GridDB installation

While loading the dataset, this tutorial will cover two methods – Using GridDB as well as Using Pandas. To access GridDB using Python, the following packages also need to be installed beforehand:

1. [GridDB C-client](https://github.com/griddb/c_client)
2. SWIG (Simplified Wrapper and Interface Generator)
3. [GridDB Python Client](https://github.com/griddb/python_client)

# 1. Dataset Overview

The  dataset consists of approximately 145k time series. Each of these time series represent a number of daily views of a different Wikipedia article, starting from July, 1st, 2015 up until December 31st, 2016. 

https://www.kaggle.com/competitions/web-traffic-time-series-forecasting/data

# 2. Importing Required Libraries



<div class="clipboard">
<pre><code class="language-python">%matplotlib inline
import pandas as pd
import numpy as np
import re
import seaborn as sns
from fbprophet import Prophet
import griddb_python as griddb

import matplotlib.pyplot as plt
plt.style.use('fivethirtyeight')

import warnings
warnings.filterwarnings("ignore")</code></pre>
</div>

# 3. Loading the Dataset

Let’s proceed and load the dataset into our notebook.


## 3.a Using GridDB

Toshiba GridDB™ is a highly scalable NoSQL database best suited for IoT and Big Data. The foundation of GridDB’s principles is based upon offering a versatile data store that is optimized for IoT, provides high scalability, tuned for high performance, and ensures high reliability.


To store large amounts of data, a CSV file can be cumbersome. GridDB serves as a perfect alternative as it in open-source and a highly scalable database. GridDB is a scalable, in-memory, No SQL database which makes it easier for you to store large amounts of data. If you are new to GridDB, a tutorial on  [reading and writing to GridDB](https://griddb.net/en/blog/using-pandas-dataframes-with-griddb/)  can be useful.

Assuming that you have already set up your database, we will now write the SQL query in python to load our dataset.

The read_sql_query function offered by the pandas library converts the data fetched into a panda data frame to make it easy for the user to work.


<div class="clipboard">
<pre><code class="language-python">sql_statement = ('SELECT * FROM train_1.csv)
df1 = pd.read_sql_query(sql_statement, cont)</code></pre>
</div>

Note that the `cont` variable has the container information where our data is stored. Replace the `credit_card_dataset` with the name of your container. More info can be found in this tutorial [reading and writing to GridDB](https://griddb.net/en/blog/using-pandas-dataframes-with-griddb/).

When it comes to IoT and Big Data use cases, GridDB clearly stands out among other databases in the Relational and NoSQL space.
Overall, GridDB offers multiple reliability features for mission-critical applications that require high availability and data retention.

## 3.b  Using pandas read_csv

We can also use Pandas' `read_csv` function to load our data. Both of the above methods will lead to the same output as the data is loaded in the form of a pandas dataframe using either of the methods.


<div class="clipboard">
<pre><code class="language-python">df = pd.read_csv('train_1.csv', parse_dates=True) </code></pre>
</div>


<div class="clipboard">
<pre><code class="language-python">df.head()</code></pre>
</div>




<div  style="overflow-x: scroll;overflow-y: hidden;">
<style scoped>
    .dataframe tbody tr th:only-of-type {
        vertical-align: middle;
    }

    .dataframe tbody tr th {
        vertical-align: top;
    }

    .dataframe thead th {
        text-align: right;
    }
</style>
<table border="1" class="dataframe">
  <thead>
    <tr style="text-align: right;">
      <th></th>
      <th>Page</th>
      <th>2015-07-01</th>
      <th>2015-07-02</th>
      <th>2015-07-03</th>
      <th>2015-07-04</th>
      <th>2015-07-05</th>
      <th>2015-07-06</th>
      <th>2015-07-07</th>
      <th>2015-07-08</th>
      <th>2015-07-09</th>
      <th>...</th>
      <th>2016-12-22</th>
      <th>2016-12-23</th>
      <th>2016-12-24</th>
      <th>2016-12-25</th>
      <th>2016-12-26</th>
      <th>2016-12-27</th>
      <th>2016-12-28</th>
      <th>2016-12-29</th>
      <th>2016-12-30</th>
      <th>2016-12-31</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <th>0</th>
      <td>2NE1_zh.wikipedia.org_all-access_spider</td>
      <td>18.0</td>
      <td>11.0</td>
      <td>5.0</td>
      <td>13.0</td>
      <td>14.0</td>
      <td>9.0</td>
      <td>9.0</td>
      <td>22.0</td>
      <td>26.0</td>
      <td>...</td>
      <td>32.0</td>
      <td>63.0</td>
      <td>15.0</td>
      <td>26.0</td>
      <td>14.0</td>
      <td>20.0</td>
      <td>22.0</td>
      <td>19.0</td>
      <td>18.0</td>
      <td>20.0</td>
    </tr>
    <tr>
      <th>1</th>
      <td>2PM_zh.wikipedia.org_all-access_spider</td>
      <td>11.0</td>
      <td>14.0</td>
      <td>15.0</td>
      <td>18.0</td>
      <td>11.0</td>
      <td>13.0</td>
      <td>22.0</td>
      <td>11.0</td>
      <td>10.0</td>
      <td>...</td>
      <td>17.0</td>
      <td>42.0</td>
      <td>28.0</td>
      <td>15.0</td>
      <td>9.0</td>
      <td>30.0</td>
      <td>52.0</td>
      <td>45.0</td>
      <td>26.0</td>
      <td>20.0</td>
    </tr>
    <tr>
      <th>2</th>
      <td>3C_zh.wikipedia.org_all-access_spider</td>
      <td>1.0</td>
      <td>0.0</td>
      <td>1.0</td>
      <td>1.0</td>
      <td>0.0</td>
      <td>4.0</td>
      <td>0.0</td>
      <td>3.0</td>
      <td>4.0</td>
      <td>...</td>
      <td>3.0</td>
      <td>1.0</td>
      <td>1.0</td>
      <td>7.0</td>
      <td>4.0</td>
      <td>4.0</td>
      <td>6.0</td>
      <td>3.0</td>
      <td>4.0</td>
      <td>17.0</td>
    </tr>
    <tr>
      <th>3</th>
      <td>4minute_zh.wikipedia.org_all-access_spider</td>
      <td>35.0</td>
      <td>13.0</td>
      <td>10.0</td>
      <td>94.0</td>
      <td>4.0</td>
      <td>26.0</td>
      <td>14.0</td>
      <td>9.0</td>
      <td>11.0</td>
      <td>...</td>
      <td>32.0</td>
      <td>10.0</td>
      <td>26.0</td>
      <td>27.0</td>
      <td>16.0</td>
      <td>11.0</td>
      <td>17.0</td>
      <td>19.0</td>
      <td>10.0</td>
      <td>11.0</td>
    </tr>
    <tr>
      <th>4</th>
      <td>52_Hz_I_Love_You_zh.wikipedia.org_all-access_s...</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>...</td>
      <td>48.0</td>
      <td>9.0</td>
      <td>25.0</td>
      <td>13.0</td>
      <td>3.0</td>
      <td>11.0</td>
      <td>27.0</td>
      <td>13.0</td>
      <td>36.0</td>
      <td>10.0</td>
    </tr>
  </tbody>
</table>
<p>5 rows × 551 columns</p>
</div>



## 4. Analysing with data visualization

Is Traffic Influenced by Page Language?

How the various languages used in Wikipedia might affect the dataset is one thing that might be interesting to examine. I'll search for the language code in the wikipedia URL using a straightforward regular expression.


<div class="clipboard">
<pre><code class="language-python">train_1 = df</code></pre>
</div>


<div class="clipboard">
<pre><code class="language-python">def get_language(page):
    res = re.search('[a-z][a-z].wikipedia.org',page)
    if res:
        return res[0][0:2]
    return 'na'

train_1['lang'] = train_1.Page.map(get_language)

from collections import Counter

print(Counter(train_1.lang))</code></pre>
</div>

    Counter({'en': 24108, 'ja': 20431, 'de': 18547, 'na': 17855, 'fr': 17802, 'zh': 17229, 'ru': 15022, 'es': 14069})
    


<div class="clipboard">
<pre><code class="language-python">lang_sets = {}
lang_sets['en'] = train_1[train_1.lang=='en'].iloc[:,0:-1]
lang_sets['ja'] = train_1[train_1.lang=='ja'].iloc[:,0:-1]
lang_sets['de'] = train_1[train_1.lang=='de'].iloc[:,0:-1]
lang_sets['na'] = train_1[train_1.lang=='na'].iloc[:,0:-1]
lang_sets['fr'] = train_1[train_1.lang=='fr'].iloc[:,0:-1]
lang_sets['zh'] = train_1[train_1.lang=='zh'].iloc[:,0:-1]
lang_sets['ru'] = train_1[train_1.lang=='ru'].iloc[:,0:-1]
lang_sets['es'] = train_1[train_1.lang=='es'].iloc[:,0:-1]

sums = {}
for key in lang_sets:
    sums[key] = lang_sets[key].iloc[:,1:].sum(axis=0) / lang_sets[key].shape[0]</code></pre>
</div>

So then how does the total number of views change over time? I'll plot all the different sets on the same plot.


<div class="clipboard">
<pre><code class="language-python">days = [r for r in range(sums['en'].shape[0])]

fig = plt.figure(1,figsize=[13,8])
plt.ylabel('Views per Page')
plt.xlabel('Day')
plt.title('Pages in Different Languages')
labels={'en':'English','ja':'Japanese','de':'German',
        'na':'Media','fr':'French','zh':'Chinese',
        'ru':'Russian','es':'Spanish'
       }

for key in sums:
    plt.plot(days,sums[key],label = labels[key] )
    
plt.legend()
plt.show()</code></pre>
</div>


    
![png](output_27_0.png)
    


English shows a much higher number of views per page, as might be expected since Wikipedia is a US-based site. 


<div class="clipboard">
<pre><code class="language-python">df1 = df.T
df1 = df1.reset_index()</code></pre>
</div>


<div class="clipboard">
<pre><code class="language-python">df1.head()</code></pre>
</div>




<div style="overflow-x: scroll;overflow-y: hidden;">
<style scoped>
    .dataframe tbody tr th:only-of-type {
        vertical-align: middle;
    }

    .dataframe tbody tr th {
        vertical-align: top;
    }

    .dataframe thead th {
        text-align: right;
    }
</style>
<table border="1" class="dataframe">
  <thead>
    <tr style="text-align: right;">
      <th></th>
      <th>Date</th>
      <th>2NE1_zh.wikipedia.org_all-access_spider</th>
      <th>2PM_zh.wikipedia.org_all-access_spider</th>
      <th>3C_zh.wikipedia.org_all-access_spider</th>
      <th>4minute_zh.wikipedia.org_all-access_spider</th>
      <th>52_Hz_I_Love_You_zh.wikipedia.org_all-access_spider</th>
      <th>5566_zh.wikipedia.org_all-access_spider</th>
      <th>91Days_zh.wikipedia.org_all-access_spider</th>
      <th>A'N'D_zh.wikipedia.org_all-access_spider</th>
      <th>AKB48_zh.wikipedia.org_all-access_spider</th>
      <th>...</th>
      <th>Drake_(músico)_es.wikipedia.org_all-access_spider</th>
      <th>Skam_(serie_de_televisión)_es.wikipedia.org_all-access_spider</th>
      <th>Legión_(serie_de_televisión)_es.wikipedia.org_all-access_spider</th>
      <th>Doble_tentación_es.wikipedia.org_all-access_spider</th>
      <th>Mi_adorable_maldición_es.wikipedia.org_all-access_spider</th>
      <th>Underworld_(serie_de_películas)_es.wikipedia.org_all-access_spider</th>
      <th>Resident_Evil:_Capítulo_Final_es.wikipedia.org_all-access_spider</th>
      <th>Enamorándome_de_Ramón_es.wikipedia.org_all-access_spider</th>
      <th>Hasta_el_último_hombre_es.wikipedia.org_all-access_spider</th>
      <th>Francisco_el_matemático_(serie_de_televisión_de_2017)_es.wikipedia.org_all-access_spider</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <th>1</th>
      <td>2015-07-01</td>
      <td>18.0</td>
      <td>11.0</td>
      <td>1.0</td>
      <td>35.0</td>
      <td>NaN</td>
      <td>12.0</td>
      <td>NaN</td>
      <td>118.0</td>
      <td>5.0</td>
      <td>...</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
    </tr>
    <tr>
      <th>2</th>
      <td>2015-07-02</td>
      <td>11.0</td>
      <td>14.0</td>
      <td>0.0</td>
      <td>13.0</td>
      <td>NaN</td>
      <td>7.0</td>
      <td>NaN</td>
      <td>26.0</td>
      <td>23.0</td>
      <td>...</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
    </tr>
    <tr>
      <th>3</th>
      <td>2015-07-03</td>
      <td>5.0</td>
      <td>15.0</td>
      <td>1.0</td>
      <td>10.0</td>
      <td>NaN</td>
      <td>4.0</td>
      <td>NaN</td>
      <td>30.0</td>
      <td>14.0</td>
      <td>...</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
    </tr>
    <tr>
      <th>4</th>
      <td>2015-07-04</td>
      <td>13.0</td>
      <td>18.0</td>
      <td>1.0</td>
      <td>94.0</td>
      <td>NaN</td>
      <td>5.0</td>
      <td>NaN</td>
      <td>24.0</td>
      <td>12.0</td>
      <td>...</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
    </tr>
    <tr>
      <th>5</th>
      <td>2015-07-05</td>
      <td>14.0</td>
      <td>11.0</td>
      <td>0.0</td>
      <td>4.0</td>
      <td>NaN</td>
      <td>20.0</td>
      <td>NaN</td>
      <td>29.0</td>
      <td>9.0</td>
      <td>...</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
    </tr>
  </tbody>
</table>
<p>5 rows × 145064 columns</p>
</div>




<div class="clipboard">
<pre><code class="language-python">df1=df1[:550]</code></pre>
</div>


<div class="clipboard">
<pre><code class="language-python">column_header = df1.iloc[0,:].values

df1.columns = column_header
    
df1 = df1.drop(0, axis = 0)
df1 = df1.rename(columns = {"Page" : "Date"})</code></pre>
</div>


<div class="clipboard">
<pre><code class="language-python">df1["Date"] = pd.to_datetime(df1["Date"], format='%Y-%m-%d')
df1 = df1.set_index("Date")</code></pre>
</div>


<div class="clipboard">
<pre><code class="language-python"># Finding number of access types and agents
access_types = []
agents = []
for column in df1.columns:
    access_type = column.split("_")[-2]
    agent = column.split("_")[-1]
    access_types.append(access_type)
    agents.append(agent)</code></pre>
</div>


<div class="clipboard">
<pre><code class="language-python"># Counting access types
from collections import Counter
access_dict = Counter(access_types)
access_dict</code></pre>
</div>




    Counter({'all-access': 74315, 'desktop': 34809, 'mobile-web': 35939})




<div class="clipboard">
<pre><code class="language-python">access_df = pd.DataFrame({"Access type" : access_dict.keys(),
                          "Number of columns" : access_dict.values()})
access_df</code></pre>
</div>




<div  style="overflow-x: scroll;overflow-y: hidden;">
<style scoped>
    .dataframe tbody tr th:only-of-type {
        vertical-align: middle;
    }

    .dataframe tbody tr th {
        vertical-align: top;
    }

    .dataframe thead th {
        text-align: right;
    }
</style>
<table border="1" class="dataframe">
  <thead>
    <tr style="text-align: right;">
      <th></th>
      <th>Access type</th>
      <th>Number of columns</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <th>0</th>
      <td>all-access</td>
      <td>74315</td>
    </tr>
    <tr>
      <th>1</th>
      <td>desktop</td>
      <td>34809</td>
    </tr>
    <tr>
      <th>2</th>
      <td>mobile-web</td>
      <td>35939</td>
    </tr>
  </tbody>
</table>
</div>




<div class="clipboard">
<pre><code class="language-python">agents_dict = Counter(agents)
agents_dict</code></pre>
</div>




    Counter({'spider': 34913, 'all-agents': 110150})




<div class="clipboard">
<pre><code class="language-python">agents_df = pd.DataFrame({"Agent" : agents_dict.keys(),
                          "Number of columns" : agents_dict.values()})
agents_df</code></pre>
</div>




<div  style="overflow-x: scroll;overflow-y: hidden;">
<style scoped>
    .dataframe tbody tr th:only-of-type {
        vertical-align: middle;
    }

    .dataframe tbody tr th {
        vertical-align: top;
    }

    .dataframe thead th {
        text-align: right;
    }
</style>
<table border="1" class="dataframe">
  <thead>
    <tr style="text-align: right;">
      <th></th>
      <th>Agent</th>
      <th>Number of columns</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <th>0</th>
      <td>spider</td>
      <td>34913</td>
    </tr>
    <tr>
      <th>1</th>
      <td>all-agents</td>
      <td>110150</td>
    </tr>
  </tbody>
</table>
</div>




<div class="clipboard">
<pre><code class="language-python">df1.columns[86543].split("_")[-3:]
"_".join(df1.columns[86543].split("_")[-3:])

projects = []
for column in df1.columns:
    project = column.split("_")[-3] 
    projects.append(project)</code></pre>
</div>


<div class="clipboard">
<pre><code class="language-python">project_dict = Counter(projects)
project_df = pd.DataFrame({"Project" : project_dict.keys(),
                           "Number of columns" : project_dict.values()})

project_df</code></pre>
</div>




<div  style="overflow-x: scroll;overflow-y: hidden;">
<style scoped>
    .dataframe tbody tr th:only-of-type {
        vertical-align: middle;
    }

    .dataframe tbody tr th {
        vertical-align: top;
    }

    .dataframe thead th {
        text-align: right;
    }
</style>
<table border="1" class="dataframe">
  <thead>
    <tr style="text-align: right;">
      <th></th>
      <th>Project</th>
      <th>Number of columns</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <th>0</th>
      <td>zh.wikipedia.org</td>
      <td>17229</td>
    </tr>
    <tr>
      <th>1</th>
      <td>fr.wikipedia.org</td>
      <td>17802</td>
    </tr>
    <tr>
      <th>2</th>
      <td>en.wikipedia.org</td>
      <td>24108</td>
    </tr>
    <tr>
      <th>3</th>
      <td>commons.wikimedia.org</td>
      <td>10555</td>
    </tr>
    <tr>
      <th>4</th>
      <td>ru.wikipedia.org</td>
      <td>15022</td>
    </tr>
    <tr>
      <th>5</th>
      <td>www.mediawiki.org</td>
      <td>7300</td>
    </tr>
    <tr>
      <th>6</th>
      <td>de.wikipedia.org</td>
      <td>18547</td>
    </tr>
    <tr>
      <th>7</th>
      <td>ja.wikipedia.org</td>
      <td>20431</td>
    </tr>
    <tr>
      <th>8</th>
      <td>es.wikipedia.org</td>
      <td>14069</td>
    </tr>
  </tbody>
</table>
</div>




<div class="clipboard">
<pre><code class="language-python">def extract_average_views(project):
    required_column_names = [column for column in df1.columns if project in column]
    average_views = df1[required_column_names].sum().mean()
    return average_views

average_views = []
for project in project_df["Project"]:
    average_views.append(extract_average_views(project))</code></pre>
</div>


<div class="clipboard">
<pre><code class="language-python">project_df["Average views"] = average_views
project_df['Average views'] = project_df['Average views'].astype('int64')
</code></pre>
</div>


<div class="clipboard">
<pre><code class="language-python">project_df</code></pre>
</div>




<div  style="overflow-x: scroll;overflow-y: hidden;">
<style scoped>
    .dataframe tbody tr th:only-of-type {
        vertical-align: middle;
    }

    .dataframe tbody tr th {
        vertical-align: top;
    }

    .dataframe thead th {
        text-align: right;
    }
</style>
<table border="1" class="dataframe">
  <thead>
    <tr style="text-align: right;">
      <th></th>
      <th>Project</th>
      <th>Number of columns</th>
      <th>Average views</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <th>0</th>
      <td>zh.wikipedia.org</td>
      <td>17229</td>
      <td>184107</td>
    </tr>
    <tr>
      <th>1</th>
      <td>fr.wikipedia.org</td>
      <td>17802</td>
      <td>358264</td>
    </tr>
    <tr>
      <th>2</th>
      <td>en.wikipedia.org</td>
      <td>24108</td>
      <td>2436898</td>
    </tr>
    <tr>
      <th>3</th>
      <td>commons.wikimedia.org</td>
      <td>10555</td>
      <td>99429</td>
    </tr>
    <tr>
      <th>4</th>
      <td>ru.wikipedia.org</td>
      <td>15022</td>
      <td>532443</td>
    </tr>
    <tr>
      <th>5</th>
      <td>www.mediawiki.org</td>
      <td>7300</td>
      <td>31411</td>
    </tr>
    <tr>
      <th>6</th>
      <td>de.wikipedia.org</td>
      <td>18547</td>
      <td>477813</td>
    </tr>
    <tr>
      <th>7</th>
      <td>ja.wikipedia.org</td>
      <td>20431</td>
      <td>419523</td>
    </tr>
    <tr>
      <th>8</th>
      <td>es.wikipedia.org</td>
      <td>14069</td>
      <td>674546</td>
    </tr>
  </tbody>
</table>
</div>




<div class="clipboard">
<pre><code class="language-python">project_df_sorted = project_df.sort_values(by = "Average views", ascending = False)</code></pre>
</div>


<div class="clipboard">
<pre><code class="language-python">plt.figure(figsize = (10,6))
sns.barplot(x = project_df_sorted["Project"], y = project_df_sorted["Average views"])
plt.xticks(rotation = "vertical")
plt.title("Average views per each project")
plt.show()</code></pre>
</div>


    
![png](output_45_0.png)
    


Popular pages in "en.wikipedia.org"


<div class="clipboard">
<pre><code class="language-python">en_wikipedia_org_columns = [column for column in df1.columns if "en.wikipedia.org" in column]

top_pages_en = df1[en_wikipedia_org_columns].mean().sort_values(ascending = False)[0:5]
df1[top_pages_en.index].plot(figsize = (16,9))</code></pre>
</div>




    <AxesSubplot:xlabel='Date'>




    
![png](output_47_1.png)
    


# 5. Forecasting


<div class="clipboard">
<pre><code class="language-python">train = df</code></pre>
</div>


<div class="clipboard">
<pre><code class="language-python">train</code></pre>
</div>




<div  style="overflow-x: scroll;overflow-y: hidden;">
<style scoped>
    .dataframe tbody tr th:only-of-type {
        vertical-align: middle;
    }

    .dataframe tbody tr th {
        vertical-align: top;
    }

    .dataframe thead th {
        text-align: right;
    }
</style>
<table border="1" class="dataframe">
  <thead>
    <tr style="text-align: right;">
      <th></th>
      <th>Page</th>
      <th>2015-07-01</th>
      <th>2015-07-02</th>
      <th>2015-07-03</th>
      <th>2015-07-04</th>
      <th>2015-07-05</th>
      <th>2015-07-06</th>
      <th>2015-07-07</th>
      <th>2015-07-08</th>
      <th>2015-07-09</th>
      <th>...</th>
      <th>2016-12-23</th>
      <th>2016-12-24</th>
      <th>2016-12-25</th>
      <th>2016-12-26</th>
      <th>2016-12-27</th>
      <th>2016-12-28</th>
      <th>2016-12-29</th>
      <th>2016-12-30</th>
      <th>2016-12-31</th>
      <th>lang</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <th>0</th>
      <td>2NE1_zh.wikipedia.org_all-access_spider</td>
      <td>18.0</td>
      <td>11.0</td>
      <td>5.0</td>
      <td>13.0</td>
      <td>14.0</td>
      <td>9.0</td>
      <td>9.0</td>
      <td>22.0</td>
      <td>26.0</td>
      <td>...</td>
      <td>63.0</td>
      <td>15.0</td>
      <td>26.0</td>
      <td>14.0</td>
      <td>20.0</td>
      <td>22.0</td>
      <td>19.0</td>
      <td>18.0</td>
      <td>20.0</td>
      <td>zh</td>
    </tr>
    <tr>
      <th>1</th>
      <td>2PM_zh.wikipedia.org_all-access_spider</td>
      <td>11.0</td>
      <td>14.0</td>
      <td>15.0</td>
      <td>18.0</td>
      <td>11.0</td>
      <td>13.0</td>
      <td>22.0</td>
      <td>11.0</td>
      <td>10.0</td>
      <td>...</td>
      <td>42.0</td>
      <td>28.0</td>
      <td>15.0</td>
      <td>9.0</td>
      <td>30.0</td>
      <td>52.0</td>
      <td>45.0</td>
      <td>26.0</td>
      <td>20.0</td>
      <td>zh</td>
    </tr>
    <tr>
      <th>2</th>
      <td>3C_zh.wikipedia.org_all-access_spider</td>
      <td>1.0</td>
      <td>0.0</td>
      <td>1.0</td>
      <td>1.0</td>
      <td>0.0</td>
      <td>4.0</td>
      <td>0.0</td>
      <td>3.0</td>
      <td>4.0</td>
      <td>...</td>
      <td>1.0</td>
      <td>1.0</td>
      <td>7.0</td>
      <td>4.0</td>
      <td>4.0</td>
      <td>6.0</td>
      <td>3.0</td>
      <td>4.0</td>
      <td>17.0</td>
      <td>zh</td>
    </tr>
    <tr>
      <th>3</th>
      <td>4minute_zh.wikipedia.org_all-access_spider</td>
      <td>35.0</td>
      <td>13.0</td>
      <td>10.0</td>
      <td>94.0</td>
      <td>4.0</td>
      <td>26.0</td>
      <td>14.0</td>
      <td>9.0</td>
      <td>11.0</td>
      <td>...</td>
      <td>10.0</td>
      <td>26.0</td>
      <td>27.0</td>
      <td>16.0</td>
      <td>11.0</td>
      <td>17.0</td>
      <td>19.0</td>
      <td>10.0</td>
      <td>11.0</td>
      <td>zh</td>
    </tr>
    <tr>
      <th>4</th>
      <td>52_Hz_I_Love_You_zh.wikipedia.org_all-access_s...</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>...</td>
      <td>9.0</td>
      <td>25.0</td>
      <td>13.0</td>
      <td>3.0</td>
      <td>11.0</td>
      <td>27.0</td>
      <td>13.0</td>
      <td>36.0</td>
      <td>10.0</td>
      <td>zh</td>
    </tr>
    <tr>
      <th>...</th>
      <td>...</td>
      <td>...</td>
      <td>...</td>
      <td>...</td>
      <td>...</td>
      <td>...</td>
      <td>...</td>
      <td>...</td>
      <td>...</td>
      <td>...</td>
      <td>...</td>
      <td>...</td>
      <td>...</td>
      <td>...</td>
      <td>...</td>
      <td>...</td>
      <td>...</td>
      <td>...</td>
      <td>...</td>
      <td>...</td>
      <td>...</td>
    </tr>
    <tr>
      <th>145058</th>
      <td>Underworld_(serie_de_películas)_es.wikipedia.o...</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>...</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>13.0</td>
      <td>12.0</td>
      <td>13.0</td>
      <td>3.0</td>
      <td>5.0</td>
      <td>10.0</td>
      <td>es</td>
    </tr>
    <tr>
      <th>145059</th>
      <td>Resident_Evil:_Capítulo_Final_es.wikipedia.org...</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>...</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>es</td>
    </tr>
    <tr>
      <th>145060</th>
      <td>Enamorándome_de_Ramón_es.wikipedia.org_all-acc...</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>...</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>es</td>
    </tr>
    <tr>
      <th>145061</th>
      <td>Hasta_el_último_hombre_es.wikipedia.org_all-ac...</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>...</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>es</td>
    </tr>
    <tr>
      <th>145062</th>
      <td>Francisco_el_matemático_(serie_de_televisión_d...</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>...</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>NaN</td>
      <td>es</td>
    </tr>
  </tbody>
</table>
<p>145063 rows × 552 columns</p>
</div>




<div class="clipboard">
<pre><code class="language-python">train=pd.melt(df[list(df.columns[-50:])+['Page']], id_vars='Page', var_name='date', value_name='Visits')</code></pre>
</div>


<div class="clipboard">
<pre><code class="language-python">list1 = ['lang']
train = train[train.date.isin(list1) == False]</code></pre>
</div>


<div class="clipboard">
<pre><code class="language-python">train</code></pre>
</div>




<div  style="overflow-x: scroll;overflow-y: hidden;">
<style scoped>
    .dataframe tbody tr th:only-of-type {
        vertical-align: middle;
    }

    .dataframe tbody tr th {
        vertical-align: top;
    }

    .dataframe thead th {
        text-align: right;
    }
</style>
<table border="1" class="dataframe">
  <thead>
    <tr style="text-align: right;">
      <th></th>
      <th>Page</th>
      <th>date</th>
      <th>Visits</th>
    </tr>
  </thead>
  <tbody>
    <tr>
      <th>0</th>
      <td>2NE1_zh.wikipedia.org_all-access_spider</td>
      <td>2016-11-13</td>
      <td>8.0</td>
    </tr>
    <tr>
      <th>1</th>
      <td>2PM_zh.wikipedia.org_all-access_spider</td>
      <td>2016-11-13</td>
      <td>11.0</td>
    </tr>
    <tr>
      <th>2</th>
      <td>3C_zh.wikipedia.org_all-access_spider</td>
      <td>2016-11-13</td>
      <td>4.0</td>
    </tr>
    <tr>
      <th>3</th>
      <td>4minute_zh.wikipedia.org_all-access_spider</td>
      <td>2016-11-13</td>
      <td>13.0</td>
    </tr>
    <tr>
      <th>4</th>
      <td>52_Hz_I_Love_You_zh.wikipedia.org_all-access_s...</td>
      <td>2016-11-13</td>
      <td>11.0</td>
    </tr>
    <tr>
      <th>...</th>
      <td>...</td>
      <td>...</td>
      <td>...</td>
    </tr>
    <tr>
      <th>7108082</th>
      <td>Underworld_(serie_de_películas)_es.wikipedia.o...</td>
      <td>2016-12-31</td>
      <td>10.0</td>
    </tr>
    <tr>
      <th>7108083</th>
      <td>Resident_Evil:_Capítulo_Final_es.wikipedia.org...</td>
      <td>2016-12-31</td>
      <td>NaN</td>
    </tr>
    <tr>
      <th>7108084</th>
      <td>Enamorándome_de_Ramón_es.wikipedia.org_all-acc...</td>
      <td>2016-12-31</td>
      <td>NaN</td>
    </tr>
    <tr>
      <th>7108085</th>
      <td>Hasta_el_último_hombre_es.wikipedia.org_all-ac...</td>
      <td>2016-12-31</td>
      <td>NaN</td>
    </tr>
    <tr>
      <th>7108086</th>
      <td>Francisco_el_matemático_(serie_de_televisión_d...</td>
      <td>2016-12-31</td>
      <td>NaN</td>
    </tr>
  </tbody>
</table>
<p>7108087 rows × 3 columns</p>
</div>




<div class="clipboard">
<pre><code class="language-python">train['date'] = train['date'].astype('datetime64[ns]')
train['weekend'] = ((train.date.dt.dayofweek) // 5 == 1).astype(float)
median = pd.DataFrame(train.groupby(['Page'])['Visits'].median())
median.columns = ['median']
mean = pd.DataFrame(train.groupby(['Page'])['Visits'].mean())
mean.columns = ['mean']

train = train.set_index('Page').join(mean).join(median)
train.reset_index(drop=False,inplace=True)
train['weekday'] = train['date'].apply(lambda x: x.weekday())

train['year']=train.date.dt.year 
train['month']=train.date.dt.month 
train['day']=train.date.dt.day</code></pre>
</div>


<div class="clipboard">
<pre><code class="language-python">mean_g = train[['Page','date','Visits']].groupby(['date'])['Visits'].mean()

means =  pd.DataFrame(mean_g).reset_index(drop=False)
means['weekday'] =means['date'].apply(lambda x: x.weekday())

means['Date_str'] = means['date'].apply(lambda x: str(x))

#create new columns year,month,day in the dataframe bysplitting the date string on hyphen and converting them to a list of values and add them under the column names year,month and day
means[['year','month','day']] = pd.DataFrame(means['Date_str'].str.split('-',2).tolist(), columns = ['year','month','day'])

#creating a new dataframe date by splitting the day column into 2 in the means data frame on sapce, to understand these steps look at the subsequent cells to understand how the day column looked before this step
date = pd.DataFrame(means['day'].str.split(' ',2).tolist(), columns = ['day','other'])
means['day'] = date['day']*1</code></pre>
</div>


<div class="clipboard">
<pre><code class="language-python">means.drop('Date_str',axis = 1, inplace =True)</code></pre>
</div>


<div class="clipboard">
<pre><code class="language-python">import seaborn as sns
sns.set(font_scale=1) 

date_index = means[['date','Visits']]
date_index = date_index.set_index('date')

prophet = date_index.copy()
prophet.reset_index(drop=False,inplace=True)
prophet.columns = ['ds','y']

m = Prophet()
m.fit(prophet)

future = m.make_future_dataframe(periods=30,freq='D')
forecast = m.predict(future)

fig = m.plot(forecast)</code></pre>
</div>

    INFO:fbprophet:Disabling yearly seasonality. Run prophet with yearly_seasonality=True to override this.
    INFO:fbprophet:Disabling daily seasonality. Run prophet with daily_seasonality=True to override this.
    


    
![png](output_57_1.png)
    


# 6. Conclusion

In this tutorial we analysed and forecasted web traffic using Python and GridDB. We examined two ways to import our data, using (1) GridDB and (2) Pandas. For large datasets, GridDB provides an excellent alternative to import data in your notebook as it is open-source and highly scalable.
