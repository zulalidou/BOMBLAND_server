# Use a base image that has Java
FROM eclipse-temurin:17-jdk-alpine

#Set the working directory
WORKDIR /app

# Copy the built .jar file into the container
COPY target/BOMBLAND_server-0.0.1-SNAPSHOT.jar /app/BOMBLAND_server-0.0.1-SNAPSHOT.jar

# Expose the port your application listens to
EXPOSE 9001

RUN mkdir -p /usr/local/newrelic
ADD ./newrelic/newrelic.jar /usr/local/newrelic/newrelic.jar
ADD ./newrelic/newrelic.yml /usr/local/newrelic/newrelic.yml

# Run the application
ENTRYPOINT ["java", "-javaagent:/usr/local/newrelic/newrelic.jar", "-jar", "/app/BOMBLAND_server-0.0.1-SNAPSHOT.jar"]