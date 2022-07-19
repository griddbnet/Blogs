FROM centos:7
RUN yum clean all 
RUN yum install -y python3
RUN rpm -Uvh https://github.com/griddb/griddb/releases/download/v5.0.0/griddb-5.0.0-linux.x86_64.rpm
RUN rpm -Uvh https://github.com/griddb/cli/releases/download/v4.6.0/griddb-ce-cli-4.6.0-linux.x86_64.rpm
RUN yum -y install iproute
RUN yum install -y \
       java-1.8.0-openjdk \
       java-1.8.0-openjdk-devel

ENV GS_HOME /var/lib/gridstore
ENV GS_LOG $GS_HOME/log
ENV HOME $GS_HOME
WORKDIR $HOME


USER gsadm
WORKDIR $HOME

EXPOSE 10001 10010 10020 10030 10040 10050 10080 20001 41999
