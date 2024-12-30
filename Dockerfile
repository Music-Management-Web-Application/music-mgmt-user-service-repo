# Use an official OpenJDK runtime as a parent image
FROM openjdk:17-jdk-slim

# Set the working directory inside the container
WORKDIR /app

# Copy the jar file into the container
COPY build/libs/user-service-0.0.2-SNAPSHOT.jar user-service.jar

# Expose the application's port
EXPOSE 8081

# Command to run the application
ENTRYPOINT ["java", "-jar", "user-service.jar"]