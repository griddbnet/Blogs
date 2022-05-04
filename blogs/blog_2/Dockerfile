FROM centos:7

RUN yum -y groupinstall "Development Tools"
RUN yum -y install epel-release wget
RUN yum -y install pcre2-devel.x86_64
RUN yum -y install openssl-devel libffi-devel bzip2-devel -y
RUN yum -y install libffi-devel bzip2-devel nodejs-devel -y
RUN yum -y install xz-devel  perl-core zlib-devel -y

RUN curl -sL https://rpm.nodesource.com/setup_12.x | bash -
RUN yum install nodejs -y

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

# Make Swig
WORKDIR /
RUN git clone https://github.com/swig/swig.git
WORKDIR /swig
RUN git checkout 113d78a083aa897ffdca4ff6bd9b42e630d16d27
RUN chmod +x autogen.sh
RUN ./autogen.sh
RUN ./configure
RUN make 
RUN make install
WORKDIR /

# Download and Make the Node.js Client
RUN wget https://github.com/griddb/nodejs_client/archive/refs/tags/0.8.6.tar.gz
RUN tar xvf 0.8.6.tar.gz
WORKDIR /nodejs_client-0.8.6
RUN npm install nan
RUN make
ENV NODE_PATH /nodejs_client-0.8.6

WORKDIR /app

# COPY time_series_example.py /app
# ENTRYPOINT ["python3", "-u", "time_series_example.py"]

