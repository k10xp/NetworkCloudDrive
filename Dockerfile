FROM amazoncorretto:17-alpine

VOLUME /tmp
WORKDIR /app

RUN apk update
RUN apk add maven

ADD pom.xml /app/pom.xml
RUN mvn dependency:resolve

# Adding source, compile and package into a fat jar
ADD src /app/src
RUN mvn verify
RUN mvn package

RUN mv /app/target/*.jar /app/target/NetworkCloudDriveDocker.jar

ENTRYPOINT ["java","-Djava.security.egd=file:/dev/./urandom","-Dspring.profiles.active=container","-jar","/app/target/NetworkCloudDriveDocker.jar"]
EXPOSE 8080

