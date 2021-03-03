FROM jupyter/scipy-notebook

USER root

RUN apt-get update \
    && apt-get install -o Dpkg::Options::="--force-confdef" -o Dpkg::Options::="--force-confold" \
    default-jre telnet -y

RUN wget https://repo1.maven.org/maven2/com/github/griddb/gridstore-jdbc/4.5.0/gridstore-jdbc-4.5.0.jar && \
    mv gridstore-jdbc-4.5.0.jar /usr/share/java/

RUN chown 777 /usr/share/java/gridstore-jdbc-4.5.0.jar

COPY utils utils
COPY run_init.sh \
    supervised-anomaly-detection.ipynb \
    unsupervised-anomaly-detection.ipynb \
    ./

RUN chmod ugo+x run_init.sh

USER $NB_UID

RUN pip install jaydebeapi
