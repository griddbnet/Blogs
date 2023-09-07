from griddb/griddb

USER root

RUN set -eux \
    && apt-get update \
    # Install dependencies for griddb
    && apt-get install -y default-jre --no-install-recommends \
    && apt-get clean all \
    # Delete the apt-get lists after installing something
    && rm -rf /var/lib/apt/lists/*

# Install GridDB c_client
WORKDIR /
RUN wget --no-check-certificate https://github.com/griddb/c_client/releases/download/v5.0.0/griddb-c-client_5.0.0_amd64.deb
RUN dpkg -i griddb-c-client_5.0.0_amd64.deb

RUN wget --no-check-certificate https://github.com/griddb/cli/releases/download/v5.0.0/griddb-ce-cli_5.0.0_amd64.deb
RUN dpkg -i griddb-ce-cli_5.0.0_amd64.deb


ADD start-griddb2.sh /
USER gsadm

ENTRYPOINT ["/bin/bash", "/start-griddb2.sh"]
CMD ["griddb"]
