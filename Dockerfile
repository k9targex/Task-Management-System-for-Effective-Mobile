FROM openjdk:17

WORKDIR /app

COPY target/TaskManagement-0.0.1-SNAPSHOT.jar tasks.jar
COPY .env .env
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "tasks.jar"]