# Stage 1: Build the Spring Boot application
# Use a Maven image that includes JDK for building
FROM maven:3.9.6-eclipse-temurin-17 AS builder

# Set the working directory inside the container
WORKDIR /app

# Copy the Maven project files (pom.xml and .mvn folder)
COPY pom.xml .
COPY .mvn .mvn

# Download project dependencies
RUN mvn dependency:go-offline -B

# Copy the rest of the application source code
COPY src src

# Build the Spring Boot application into a JAR file
RUN mvn package -DskipTests

# Stage 2: Create the final, smaller runtime image
# Use a JRE-only image for a smaller footprint
FROM eclipse-temurin:17-jre-focal

# Set the working directory for the application
WORKDIR /app

# Copy the built JAR file from the 'builder' stage
COPY --from=builder /app/target/spring-boot-jpa-paging-sorting-0.0.1-SNAPSHOT.jar app.jar

# Expose the port your Spring Boot application runs on (default is 8080)
EXPOSE 8080

# Define the command to run the application when the container starts
ENTRYPOINT ["java", "-jar", "app.jar"]