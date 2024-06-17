FROM alpine:3.14

WORKDIR /app
RUN apk add --no-cache wget
RUN apk add openjdk11

RUN wget https://repo1.maven.org/maven2/com/github/griddb/gridstore-jdbc/5.6.0/gridstore-jdbc-5.6.0.jar
ENV CLASSPATH /app/gridstore-jdbc-5.6.0.jar

COPY ./src ./src
WORKDIR /app/src/main/java/
RUN javac net/griddb/jdbc/Jdbc.java

CMD ["java",  "net/griddb/jdbc/Jdbc.java", "griddb-server:20001", "myCluster", "public"]