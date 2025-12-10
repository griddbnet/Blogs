FROM mcr.microsoft.com/azure-functions/python:4-python3.12

ENV AzureWebJobsScriptRoot=/home/site/wwwroot \
    AzureFunctionsJobHost__Logging__Console__IsEnabled=true
ENV PYTHONBUFFERED=1
ENV GRIDDB_NOTIFICATION_PROVIDER="[notification_provider]"
ENV GRIDDB_CLUSTER_NAME="[clustername]"
ENV GRIDDB_USERNAME="[griddb-user]"
ENV GRIDDB_PASSWORD="[password]"
ENV GRIDDB_DATABASE="[database]"

WORKDIR /home/site/wwwroot

RUN apt-get update && \
    apt-get install -y default-jdk git maven && \
    rm -rf /var/lib/apt/lists/*

ENV JAVA_HOME=/usr/lib/jvm/default-java

WORKDIR /tmp
RUN git clone https://github.com/griddb/python_client.git && \
    cd python_client/java && \
    mvn install

RUN mkdir -p /home/site/wwwroot/lib && \
    mv /tmp/python_client/java/target/gridstore-arrow-5.8.0.jar /home/site/wwwroot/lib/gridstore-arrow.jar


WORKDIR /tmp/python_client/python
RUN python3.12 -m pip install .

WORKDIR /home/site/wwwroot

COPY ./lib/gridstore.jar /home/site/wwwroot/lib/
COPY ./lib/arrow-memory-netty.jar /home/site/wwwroot/lib/
COPY ./lib/gridstore-jdbc.jar /home/site/wwwroot/lib/
COPY *.py .
COPY requirements.txt .

RUN python3.12 -m pip install -r requirements.txt

ENV CLASSPATH=/home/site/wwwroot/lib/gridstore.jar:/home/site/wwwroot/lib/gridstore-jdbc.jar:/home/site/wwwroot/lib/gridstore-arrow.jar:/home/site/wwwroot/lib/arrow-memory-netty.jar