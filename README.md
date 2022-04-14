## Introduction

A new version of the GridDB Python client has been released which adds some new time series functions. These functions are new to the python client but have been available for use in the native GridDB language (java). These new functions are `aggregate_time_series`, `query_by_time_series_range`, and `query_by_time_series_sampling`. 

In this blog, we will walkthrough the uses for these functions, how to use them, and some examples using a freely available data set from [kaggle](https://www.kaggle.com/datasets/census/population-time-series-data). To add to that, we will also share a `Dockerfile` which will contain all instructions to building and running the new python client.

## Installing Python Client

You will of course need to have GridDB installed on to your machine. Instructions for that can be found here: [docs](https://docs.griddb.net/gettingstarted/introduction/).

According to the [github](https://github.com/griddb/python_client) page, these are the environment requirements: 

    OS: CentOS 7.6(x64) (GCC 4.8.5)
    SWIG: 3.0.12
    Python: 3.6
    GridDB C client: V4.5 CE(Community Edition)
    GridDB server: V4.5 CE, CentOS 7.6(x64) (GCC 4.8.5)

    OS: Ubuntu 18.04(x64) (gcc 7.3.0)
    SWIG: 3.0.12
    Python: 3.6
    GridDB C client: V4.5 CE (Note: If you build from source code, please use GCC 4.8.5.)
    GridDB server: V4.5 CE, Ubuntu 18.04(x64) (Note: If you build from source code, please use GCC 4.8.5.)
    
    OS: Windows 10(x64) (VS2017)
    SWIG: 3.0.12
    Python: 3.6
    GridDB C client: V4.5 CE
    GridDB server: V4.5 CE, CentOS 7.6(x64) (GCC 4.8.5)

    OS: MacOS Catalina (x86_64)
    SWIG: 3.0.12
    Python: 3.6.9
    GridDB C client: V4.5 CE
    GridDB server: V4.5 CE, Centos 7.6(x64) (GCC 4.8.5)

### Dockerfile

For the `Dockerfile` which we have prepared, it will build all prereqs and then run whichever Python script you feed into it at the bottom of the file. Here is the file in its entirety: 

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
RUN ls
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

If you want to install the python client onto your machine without using containers, you can of course simpyl follow the procedure laid out in the files instructions. 

When using this container, you can either run a [second container which will host a GridDB Server](https://griddb.net/en/blog/improve-your-devops-with-griddb-server-and-client-docker-containers/), or you can use your currently running GridDB instance. This can be accomplished by using the `network` flag while running your docker image:

`docker run -it --network host --name python_client <image id>`

By setting this network flag, you tell your container to use your host machine's network; you can read more about that here: [Docker Docs](https://docs.docker.com/network/host/).

## Ingesting Data 

## Time Series Functionality

The three functions which have been added to this GridDB connector ( `aggregate_time_series`, `query_by_time_series_range`, `query_by_time_series_sampling`) are useful in their own unique ways. In general though, these sorts of functions really help developers/engineers to do meaningful analysis through gaining statistical insights into large datasets.

### Aggregate Time Series

### Query Time Series Range

### Query By Time Series Sampling
