## Introduction

A new version of the GridDB Python client has been released which adds some new time series functions. Though these functions are new to the python client, they have been available for use in the native GridDB language (java) and through `TQL` strings/queries prior to this release. 

Generally, staying away from TQL query strings will be safer as it allows us to not worry about injection attacks; it is also much simpler to use these functions compared to their TQL equivalents. You can read more about the TQL queries [here](http://www.toshiba-sol.co.jp/en/pro/griddb/docs-en/v4_3/GridDB_TQL_Reference.html).  

Now, let's move on to what we will discuss in this blog: we will walk through the uses for these functions, how to use them, and display some examples using a freely available data set from [kaggle](https://www.kaggle.com/datasets/census/population-time-series-data). These are `aggregate_time_series`, `query_by_time_series_range`, and `query_by_time_series_sampling`. 

We will also have a brief section showing how to ingest the data from the `csv` file using Java. To add to that, we will also share a `Dockerfile` which will contain all instructions to building and running the new python client. 

## Installing Python Client

You will of course need to have GridDB installed on to your machine. Instructions for that can be found here: [docs](https://docs.griddb.net/gettingstarted/introduction/).
 
According to the [GridDB Python Client github](https://github.com/griddb/python_client) page, these are the environment requirements for CentOS: 

    OS: CentOS 7.6(x64) (GCC 4.8.5)
    SWIG: 3.0.12
    Python: 3.6
    GridDB C client: V4.5 CE(Community Edition)
    GridDB server: V4.5 CE, CentOS 7.6(x64) (GCC 4.8.5)

### Dockerfile

The `Dockerfile` which we have prepared will build/make all prereqs and then run whichever Python script you feed into it at the bottom of the file. 

To make things easy, you can simply pull from Dockerhub: 

`docker pull griddbnet/python-client-v0.8.5:latest`

Here is the file in its entirety: 

```bash
FROM centos:7

RUN yum -y groupinstall "Development Tools"
RUN yum -y install epel-release wget
RUN yum -y install pcre2-devel.x86_64
RUN yum -y install openssl-devel libffi-devel bzip2-devel -y
RUN yum -y install xz-devel  perl-core zlib-devel -y
RUN yum -y install numpy scipy

# Make c_client
WORKDIR /
RUN wget --no-check-certificate https://github.com/griddb/c_client/archive/refs/tags/v4.6.0.tar.gz
RUN tar -xzvf v4.6.0.tar.gz
WORKDIR /c_client-4.6.0/client/c
RUN  ./bootstrap.sh
RUN ./configure
RUN make
WORKDIR /c_client-4.6.0/bin
ENV LIBRARY_PATH ${LIBRARY_PATH}:/c_client-4.6.0/bin
ENV LD_LIBRARY_PATH ${LD_LIBRARY_PATH}:/c_client-4.6.0/bin

# Make SSL for Python3.10
WORKDIR /
RUN wget  --no-check-certificate https://www.openssl.org/source/openssl-1.1.1c.tar.gz
RUN tar -xzvf openssl-1.1.1c.tar.gz
WORKDIR /openssl-1.1.1c
RUN ./config --prefix=/usr --openssldir=/etc/ssl --libdir=lib no-shared zlib-dynamic
RUN make
RUN make test
RUN make install

# Build Python3.10
WORKDIR /
RUN wget https://www.python.org/ftp/python/3.10.4/Python-3.10.4.tgz
RUN tar xvf Python-3.10.4.tgz
WORKDIR /Python-3.10.4
RUN ./configure --enable-optimizations  -C --with-openssl=/usr --with-openssl-rpath=auto --prefix=/usr/local/python-3.version
RUN make install
ENV PATH ${PATH}:/usr/local/python-3.version/bin

RUN python3 -m pip install pandas

# Make Swig
WORKDIR /
RUN wget https://github.com/swig/swig/archive/refs/tags/v4.0.2.tar.gz
RUN tar xvfz v4.0.2.tar.gz
WORKDIR /swig-4.0.2
RUN chmod +x autogen.sh
RUN ./autogen.sh
RUN ./configure
RUN make
RUN make install
WORKDIR /

# Make Python Client
RUN wget https://github.com/griddb/python_client/archive/refs/tags/0.8.5.tar.gz
RUN tar xvf 0.8.5.tar.gz
WORKDIR /python_client-0.8.5
RUN make
ENV PYTHONPATH /python_client-0.8.5

WORKDIR /app

COPY time_series_example.py /app
ENTRYPOINT ["python3", "-u", "time_series_example.py"]
```

If you want to install the python client onto your machine without using containers, you can of course simply follow the procedure laid out in the file's instructions. 

When using this container, you can either run a [second container which will host a GridDB Server](https://griddb.net/en/blog/improve-your-devops-with-griddb-server-and-client-docker-containers/), or you can use your currently running GridDB instance. This can be accomplished by using the `network` flag while running your docker image:

`docker run -it --network host --name python_client <image id>`

## Ingesting Data 

The dataset we're using is downloadable on the [kaggle](https://www.kaggle.com/datasets/census/population-time-series-data) website for free and is presented to us in `csv` form. To ingest this into our GridDB server, we will be using java as it is the native connector, but of course ingesting via python is also feasible. 

You can download the java code to ingest the data yourself from our [Github Repo]()


## Time Series Functionality

The three functions which have been added to this GridDB connector ( `aggregate_time_series`, `query_by_time_series_range`, `query_by_time_series_sampling`) are useful in their own unique ways. In general though, these sorts of functions really help developers/engineers to do meaningful analysis through gaining statistical insights into large datasets.

For the remainder of this blog, we will walk through each function one-by-one and showcase running it against our dataset and hopefully illuminate why it is needed.

To start, we will connect to our GridDB server with Python; it is not that dissimilar to connecting using java.

```python
#!/usr/bin/python

import griddb_python as griddb
import sys
import calendar
import datetime

factory = griddb.StoreFactory.get_instance()

#Get GridStore object
store = factory.get_store(
    host="239.0.0.1",
    port=31999,
    cluster_name="defaultCluster",
    username="admin",
    password="admin"
)
```

We will also need to fetch our newly made dataset to run our queries: 

```python
ts = store.get_container("population")
query = ts.query("select * from population where value > 327000")
rs = query.fetch()
```

To avoid querying too many rows, as well as avoiding the missing data from before 1970, we will start our query with population over 280000 (these values are in thousands), which will put us at around 1999 (20+ years ago).

This query will grab 20 years of data, but for simplicity sake, I will simply start from the first date which is over our query parameter and use the time series analysis since then.

```python
data = rs.next() #grabs just the first row from our entire query
timestamp = calendar.timegm(data[0].timetuple()) #data[0] is the timestamp
gsTS = (griddb.TimestampUtils.get_time_millis(timestamp)) #converts the data to millis
time = datetime.datetime.fromtimestamp(gsTS/1000.0) # converts back to a usable java datetime obj for the time series functions
```


### Aggregate Time Series

The `aggregation` functionality is a bit unqiue as it will return an `AggregationResult` instead of a set of rows as the other queries do. These results can grab values of `min, max, total, average, variance, standard deviation, count, and weighted average`. 

Let's walk through these examples. First, some preliminary variables need to be set:

```python
data = rs.next()
year_in_mili = 31536000000 # this is one year in miliseconds
added = gsTS + (year_in_mili * 7) # 7 years after our start time (this is end time)
addedTime = datetime.datetime.fromtimestamp(added/1000.0) # converting to datetime obj as this is what the function expects
```

Here you can see we use the start time as the first row returned from our query, and then the end time as 7 years later. 

So, if we set the aggregation type to min, the function will return the minimum value from the dataset (the smallest number). The max will do the opposite -- the largest integer from the result. Total will take the sum of all values and return that to you. 

`AggregationResult` is the return type, and the parameters expected look like this: `aggregate_time_series(object start, object end, Aggregation type, string column_name=None)`. This is what it looks like fully formed: 

```python
total = ts.aggregate_time_series(time, addedTime, griddb.Aggregation.TOTAL, "value")
print("TOTAL: ", total.get(griddb.Type.LONG))
```

`TOTAL:  48714984`

The average is also the mean, it simply takes the total sum of all values divided by the count.

```python
avg = ts.aggregate_time_series(time, addedTime, griddb.Aggregation.AVERAGE, "value")
print("AVERAGE: ", avg.get(griddb.Type.LONG))
```

`AVERAGE:  289970`
 
The average between 1999 and 2006 was about 290 million.

Variance is mathematically defined as: the average of the squared differences from the mean. This essentially means how different each number is different from the mean/average. 

Standard deviation is similar to variance, it "is a statistical measurement that looks at how far a group of numbers is from the mean. Put simply, standard deviation measures how far apart numbers are in a data set." It is usually used to analyze how closely related the numbers are to the mean.

The last one worth discussing here is the weighted average. A weighted average attempts to quantify the importance of some values over others in the average. In the case of time series data, it generally measures the time space between two data points and tries to weigh that. For this specific dataset, each data point is exactly 1 month apart, so unfortunately the number that is produced by our query is the same as our average: 

```python
weightedAvg = ts.aggregate_time_series(time, addedTime, griddb.Aggregation.WEIGHTED_AVERAGE, "value")
print("WEIGHTED AVERAGE: ", weightedAvg.get(griddb.Type.LONG))
```

`WEIGHTED AVERAGE:  289970`

But if the time series datas was irregularly spaced, the number produced would have been different from our average. You can read more about this from a previous [blog](https://griddb.net/en/blog/aggregation-with-griddb/)

### Query Time Series Range

The query time series range will return a set of Rows type same as most other queries. The function looks like this: `query_by_time_series_range(object start, object end, QueryOrder order=QueryOrder.ASCENDING)`. It returns the row from the start time to the end time -- essentially giving the developer an easy way to grab an explicit time range. 

Here's the concrete example: 

```python
rangeQuery = ts.query_by_time_series_range(time, addedTime, griddb.QueryOrder.ASCENDING)
rangeRs = rangeQuery.fetch()
while rangeRs.has_next():
    d = rangeRs.next()
    print("d: ", d)
```

The results are simply the range: 

```bash
d:  [datetime.datetime(1999, 10, 1, 0, 0), 280203]
d:  [datetime.datetime(1999, 10, 1, 7, 0), 280203]
d:  [datetime.datetime(1999, 11, 1, 0, 0), 280471]
d:  [datetime.datetime(1999, 11, 1, 8, 0), 280471]
d:  [datetime.datetime(1999, 12, 1, 0, 0), 280716]
d:  [datetime.datetime(1999, 12, 1, 8, 0), 280716]
d:  [datetime.datetime(2000, 1, 1, 0, 0), 280976]
d:  [datetime.datetime(2000, 1, 1, 8, 0), 280976]
d:  [datetime.datetime(2000, 2, 1, 0, 0), 281190]
d:  [datetime.datetime(2000, 2, 1, 8, 0), 281190]
d:  [datetime.datetime(2000, 3, 1, 0, 0), 281409]
d:  [datetime.datetime(2000, 3, 1, 8, 0), 281409]
d:  [datetime.datetime(2000, 4, 1, 0, 0), 281653]
d:  [datetime.datetime(2000, 4, 1, 8, 0), 281653]
d:  [datetime.datetime(2000, 5, 1, 0, 0), 281877]
d:  [datetime.datetime(2000, 5, 1, 7, 0), 281877]
d:  [datetime.datetime(2000, 6, 1, 0, 0), 282126]
d:  [datetime.datetime(2000, 6, 1, 7, 0), 282126]
d:  [datetime.datetime(2000, 7, 1, 0, 0), 282385]
d:  [datetime.datetime(2000, 7, 1, 7, 0), 282385]
d:  [datetime.datetime(2000, 8, 1, 0, 0), 282653]
d:  [datetime.datetime(2000, 8, 1, 7, 0), 282653]
d:  [datetime.datetime(2000, 9, 1, 0, 0), 282932]
d:  [datetime.datetime(2000, 9, 1, 7, 0), 282932]
```

### Query By Time Series Sampling

The last of the new functions is, in my opinion, the most interesting. Thie sampling returns a uniform sample of rows which have a set time between each point.

The time series sampling function also returns a set of Rows. It takes the most parameters of all the functions: `query_by_time_series_sampling(object start, object end, list[string] column_name_list, InterpolationMode mode, int interval, TimeUnit interval_unit)`. It also takes the start and end, but this time it takes a list of column names, and then an interpolation mode, and intervals and time units. 

For interpolation mode, you can choose either: `LINEAR_OR_PREVIOUS` or `EMPTY`. For the interval unit, the following choices are available: `YEAR,MONTH,DAY,HOUR,MINUTE,SECOND,MILLISECOND` but the year and month are too large and not allowed as intervals, so essentially you would choose an interval from day or below. 

Here is the concrete example ran for the blog: 

```python
try:   
    samplingQuery = ts.query_by_time_series_sampling(time, addedTime, ["value"], griddb.InterpolationMode.LINEAR_OR_PREVIOUS, 1, griddb.TimeUnit.DAY) # the columns need to be a list, hence the [ ]
    samplingRs = samplingQuery.fetch()
    while samplingRs.has_next(): 
        d = samplingRs.next()
        print("sampling: ", d)
except griddb.GSException as e:
    for i in range(e.get_error_stack_size()):
        print("[", i, "]")
        print(e.get_error_code(i))
        print(e.get_message(i))
```

The addedtime variable is 7 years after the initial, but here is just a small slice of the queries which were printed out:

```bash
sampling:  [datetime.datetime(2006, 8, 1, 0, 0), 299263]
sampling:  [datetime.datetime(2006, 8, 2, 0, 0), 299269]
sampling:  [datetime.datetime(2006, 8, 3, 0, 0), 299279]
sampling:  [datetime.datetime(2006, 8, 4, 0, 0), 299288]
sampling:  [datetime.datetime(2006, 8, 5, 0, 0), 299298]
sampling:  [datetime.datetime(2006, 8, 6, 0, 0), 299307]
sampling:  [datetime.datetime(2006, 8, 7, 0, 0), 299317]
sampling:  [datetime.datetime(2006, 8, 8, 0, 0), 299326]
sampling:  [datetime.datetime(2006, 8, 9, 0, 0), 299336]
sampling:  [datetime.datetime(2006, 8, 10, 0, 0), 299345]
sampling:  [datetime.datetime(2006, 8, 11, 0, 0), 299354]
sampling:  [datetime.datetime(2006, 8, 12, 0, 0), 299364]
sampling:  [datetime.datetime(2006, 8, 13, 0, 0), 299373]
sampling:  [datetime.datetime(2006, 8, 14, 0, 0), 299383]
sampling:  [datetime.datetime(2006, 8, 15, 0, 0), 299392]
sampling:  [datetime.datetime(2006, 8, 16, 0, 0), 299402]
sampling:  [datetime.datetime(2006, 8, 17, 0, 0), 299411]
sampling:  [datetime.datetime(2006, 8, 18, 0, 0), 299421]
sampling:  [datetime.datetime(2006, 8, 19, 0, 0), 299430]
sampling:  [datetime.datetime(2006, 8, 20, 0, 0), 299440]
sampling:  [datetime.datetime(2006, 8, 21, 0, 0), 299449]
sampling:  [datetime.datetime(2006, 8, 22, 0, 0), 299459]
sampling:  [datetime.datetime(2006, 8, 23, 0, 0), 299468]
sampling:  [datetime.datetime(2006, 8, 24, 0, 0), 299478]
sampling:  [datetime.datetime(2006, 8, 25, 0, 0), 299487]
sampling:  [datetime.datetime(2006, 8, 26, 0, 0), 299497]
sampling:  [datetime.datetime(2006, 8, 27, 0, 0), 299506]
sampling:  [datetime.datetime(2006, 8, 28, 0, 0), 299516]
sampling:  [datetime.datetime(2006, 8, 29, 0, 0), 299525]
sampling:  [datetime.datetime(2006, 8, 30, 0, 0), 299535]
sampling:  [datetime.datetime(2006, 8, 31, 0, 0), 299544]
sampling:  [datetime.datetime(2006, 9, 1, 0, 0), 299554]
sampling:  [datetime.datetime(2006, 9, 2, 0, 0), 299560]
sampling:  [datetime.datetime(2006, 9, 3, 0, 0), 299570]
sampling:  [datetime.datetime(2006, 9, 4, 0, 0), 299579]
sampling:  [datetime.datetime(2006, 9, 5, 0, 0), 299589]
sampling:  [datetime.datetime(2006, 9, 6, 0, 0), 299598]
sampling:  [datetime.datetime(2006, 9, 7, 0, 0), 299607]
sampling:  [datetime.datetime(2006, 9, 8, 0, 0), 299617]
sampling:  [datetime.datetime(2006, 9, 9, 0, 0), 299626]
sampling:  [datetime.datetime(2006, 9, 10, 0, 0), 299636]
```

The original dataset provides us with the population numbers for the first of every month. And with the sampling, we can extrapolate the population values on a per-day basis. We can see, based on the data, the values we do have from kaggle, are correct, and the population leading up to those days are reasonable. For example, 09/01/2006 has a population value of 299554, which matches our kaggle data, and the day before has a value of 299544.    

## Conclusion

This blog has demonstrated how to build a new python client, either through docker, or through following the step by step instructions in the Dockerfile. A very simple data ingestion from a `csv` file was also demonstrated (via java). And lastly, we have shown how useful the new time series functions can be in your time series analysis. 

Full code for this blog can be found here: [download]()