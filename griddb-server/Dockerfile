FROM ubuntu:20.04

ENV GRIDDB_VERSION=5.5.0
ENV GS_HOME=/var/lib/gridstore
# Need declare $GS_LOG to start GridDB server
ENV GS_LOG=/var/lib/gridstore/log
ENV PORT=20001
ENV DEBIAN_FRONTEND=noninteractive

RUN set -eux \
    && apt -y update \
    && apt install -y wget gnupg gnupg2 gnupg1

RUN echo "deb https://www.griddb.net/apt griddb/5.5 multiverse" >>  /etc/apt/sources.list.d/griddb.list
RUN wget -qO - https://www.griddb.net/apt/griddb.asc | apt-key add -



RUN set -eux \
    && apt-get -y update \
    # Install dependencies for griddb
    && apt-get install -y python3 wget dpkg systemd jq griddb-meta default-jre --no-install-recommends \
    # Delete the apt-get lists after installing something
    && rm -rf /var/lib/apt/lists/*

VOLUME $GS_HOME
WORKDIR $GS_HOME


COPY start-griddb.sh /
USER gsadm
ENTRYPOINT ["/bin/bash", "/start-griddb.sh"]
EXPOSE $PORT
CMD ["griddb"]
