FROM ubuntu:18.04

# You can download griddb V5.0.0 directly at https://github.com/griddb/griddb/releases/tag/v5.0.0
ENV GRIDDB_VERSION=5.5.0
ENV GS_HOME=/var/lib/gridstore
# Need declare $GS_LOG to start GridDB server
ENV GS_LOG=/var/lib/gridstore/log
ENV PORT=10001
ENV DEBIAN_FRONTEND=noninteractive

# Install griddb server
RUN set -eux \
    && apt-get update \
    # Install dependencies for griddb
    && apt-get install -y systemd dpkg python3 wget jq default-jre --no-install-recommends \
    && apt-get clean all \
    # Download package griddb server
    && wget -q https://github.com/griddb/griddb/releases/download/v${GRIDDB_VERSION}/griddb_${GRIDDB_VERSION}_amd64.deb --no-check-certificate \
    # Install package griddb server
    && dpkg -i griddb_${GRIDDB_VERSION}_amd64.deb \
    # Remove package
    && rm griddb_${GRIDDB_VERSION}_amd64.deb \
    # Delete the apt-get lists after installing something
    && rm -rf /var/lib/apt/lists/*

# Install GridDB c_client
RUN wget --no-check-certificate https://github.com/griddb/c_client/releases/download/v5.5.0/griddb-c-client_5.5.0_amd64.deb
RUN dpkg -i griddb-c-client_5.5.0_amd64.deb

RUN wget --no-check-certificate https://github.com/griddb/cli/releases/download/v5.3.1/griddb-ce-cli_5.3.1_amd64.deb
RUN dpkg -i griddb-ce-cli_5.3.1_amd64.deb

ADD start-griddb2.sh /
ADD .gsshrc /root
RUN chmod +x start-griddb2.sh
USER gsadm

ENTRYPOINT ["/bin/bash", "/start-griddb2.sh"]
EXPOSE $PORT
CMD ["griddb"]
