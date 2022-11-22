# Base image on JDK 18
FROM openjdk:18
# Copy JAR built by maven
COPY target/cs-218-paxos-router-0.0.1-SNAPSHOT.jar app.jar
# Expose port 8053
EXPOSE 8053
# Launch the application
ENTRYPOINT ["java", "-jar", "app.jar"]