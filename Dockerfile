FROM openjdk:8-jdk-alpine
ENV SERVER_PORT=8070
EXPOSE $SERVER_PORT
COPY user-microservice/target/user-microservice-0.1.0.jar app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]
