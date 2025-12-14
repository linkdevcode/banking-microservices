# Use the OpenJDK JRE based on Alpine Linux for a small image size
# eclipse-temurin provides reliable, production-ready OpenJDK builds.
FROM eclipse-temurin:21-jre-alpine

# Set time zone data for accurate logging (Alpine images often lack this by default)
RUN apk add --no-cache tzdata

ARG APP_JAR_NAME=app.jar

# Define the name of the JAR file. This file will be created by the Maven 'install' command.
# The value is passed during the build process defined in docker-compose.yml.
ARG JAR_FILE=target/app.jar

# Copy the built JAR file into the container's root directory and rename it to app.jar
# COPY ${JAR_FILE} app.jar
COPY target/${APP_JAR_NAME} app.jar

# Define the user to run the container for security best practices (if supported by base image)
# USER 1000

# Specify the command to run the application when the container starts
ENTRYPOINT ["java", "-jar", "app.jar"]