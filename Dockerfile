FROM maven

WORKDIR /app

COPY ./pom.xml ./

COPY ./src ./src

RUN mvn install
CMD ["java", "-jar", "target/SqlBatch.jar"]
