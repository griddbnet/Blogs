FROM maven

WORKDIR /app

COPY ./pom.xml ./

COPY ./src ./src

RUN mvn dependency:resolve
RUN mvn package
CMD ["java", "-jar", "target/Log-Viewer-1.jar"]
