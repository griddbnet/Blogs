FROM rockylinux:9.3

ENV GRIDDB_NODE_API_VERSION=0.8.5
ENV NODE_PATH=/root/node-api-${GRIDDB_NODE_API_VERSION}

# Install griddb server
RUN set -eux \
    && dnf update -y \
    # Install nodejs version 16.x and c client for griddb nodejs_client
    && dnf install -y curl make python3 tar --allowerasing \
    && dnf groupinstall -y 'Development Tools'

RUN curl -o- https://raw.githubusercontent.com/nvm-sh/nvm/v0.40.1/install.sh | bash

RUN source ~/.nvm/nvm.sh && nvm install 20 && nvm use 20


COPY ./lib/griddb-c-client-5.5.0-linux.aarch64.rpm /
RUN rpm -Uvh /griddb-c-client-5.5.0-linux.aarch64.rpm

SHELL ["/bin/bash", "--login", "-c"]
# Copy entrypoint script and sample for fixlist
COPY run-griddb.sh sample1_fixlist.js /root/

WORKDIR /root

# Install nodejs client
RUN curl -L https://github.com/griddb/node-api/archive/refs/tags/${GRIDDB_NODE_API_VERSION}.tar.gz -o ${GRIDDB_NODE_API_VERSION}.tar.gz -sS \
    && tar -xzvf ${GRIDDB_NODE_API_VERSION}.tar.gz \
    && cd node-api-${GRIDDB_NODE_API_VERSION} 

WORKDIR /root/node-api-${GRIDDB_NODE_API_VERSION}
RUN  npm install 
RUN rm ../${GRIDDB_NODE_API_VERSION}.tar.gz

WORKDIR /root
# Set permission executable for script
RUN chmod a+x run-griddb.sh

# Run sample
CMD ["/bin/bash", "run-griddb.sh"]
