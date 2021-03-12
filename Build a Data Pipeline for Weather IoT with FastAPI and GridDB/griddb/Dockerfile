FROM centos:7

RUN rpm -Uvh https://github.com/griddb/griddb/releases/download/v4.5.0/griddb-4.5.0-1.linux.x86_64.rpm
 RUN yum -y install iproute wget


ENV GS_HOME /var/lib/gridstore
ENV GS_LOG $GS_HOME/log
ENV HOME $GS_HOME

WORKDIR $HOME

COPY start_griddb.sh ./
RUN chown gsadm ./start_griddb.sh
RUN chmod u+x ./start_griddb.sh

RUN wget -O /usr/local/bin/jq https://github.com/stedolan/jq/releases/download/jq-1.6/jq-linux64 && \
    chown gsadm /usr/local/bin/jq && \
    chmod u+x /usr/local/bin/jq

USER gsadm
CMD ./start_griddb.sh
EXPOSE 10001 10010 10020 10030 10040 10050 10080 20001

