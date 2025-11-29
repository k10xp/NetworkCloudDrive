FROM openjdk:8-jdk-alpine
ENV DOCKER_BUILDKIT 0
WORKDIR /app
ARG JAR_FILE=target/*.jar
COPY ${JAR_FILE} /app/NetworkCloudDrive_docker.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/NetworkCloudDrive_docker.jar"]