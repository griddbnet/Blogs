FROM alpine:3.20.1

RUN apk add --no-cache \
  wget \
  openjdk11 \
  build-base \
  python3 \
  py3-pip \  
  python3-dev \
  py3-virtualenv

WORKDIR /app/lib
RUN wget https://repo1.maven.org/maven2/com/github/griddb/gridstore-jdbc/5.6.0/gridstore-jdbc-5.6.0.jar

ENV ENV_DIR /app/venv
ENV CLASSPATH /app/lib/gridstore-jdbc-5.6.0.jar
ENV LD_LIBRARY_PATH /usr/lib/jvm/java-11-openjdk/lib/server/

WORKDIR /app
RUN python3.12 -m venv ${ENV_DIR}
RUN source ${ENV_DIR}/bin/activate 
RUN ${ENV_DIR}/bin/pip3.12 install \
  pandas \
  jpype1 \
  numpy \
  sqlalchemy-jdbc-generic

COPY griddb.py /app/griddb.py
# ENTRYPOINT ["tail"]
# CMD ["-f","/dev/null"]
CMD ["/app/venv/bin/python3.12", "griddb.py"]