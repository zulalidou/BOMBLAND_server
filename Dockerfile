# Use a base image that has Java
FROM eclipse-temurin:17-jdk-alpine

#Set the working directory
WORKDIR /app

# Copy the built .jar file into the container
COPY target/BOMBLAND_server-0.0.1-SNAPSHOT.jar /app/BOMBLAND_server-0.0.1-SNAPSHOT.jar

# Expose the port your application listens to
EXPOSE 9001

# Run the application
CMD ["java", "-jar", "/app/BOMBLAND_server-0.0.1-SNAPSHOT.jar"]