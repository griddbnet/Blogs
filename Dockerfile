FROM centos:7

RUN yum -y groupinstall "Development Tools"
RUN yum -y install epel-release wget
RUN yum -y install pcre2-devel.x86_64
RUN yum -y install openssl-devel libffi-devel bzip2-devel -y
RUN yum -y install xz-devel  perl-core zlib-devel -y
RUN yum -y install numpy scipy

#COPY griddb.repo /etc/yum.repos.d/
#RUN yum -y update
#RUN yum -y install griddb-c-client

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

RUN wget https://github.com/griddb/python_client/archive/refs/tags/0.8.5.tar.gz
RUN tar xvf 0.8.5.tar.gz
WORKDIR /python_client-0.8.5

#RUN yum -y install python36 python36-devel
RUN make
ENV PYTHONPATH /python_client-0.8.5

WORKDIR /app

COPY time_series_example.py /app
ENTRYPOINT ["python3", "-u", "time_series_example.py"]
