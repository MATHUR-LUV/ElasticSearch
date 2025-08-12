# Stage 1: Build the Spring Boot application
# Use a Maven image that includes JDK for building
FROM maven:3.9.6-eclipse-temurin-17 AS builder

# Set the working directory inside the container
WORKDIR /app

# Copy the Maven project files (pom.xml and .mvn folder)
# This step is optimized for Docker caching: if pom.xml doesn't change,
# Maven dependencies won't be re-downloaded in subsequent builds.
COPY pom.xml .
COPY .mvn .mvn

# Download project dependencies. This helps cache dependencies.
# The 'dependency:go-offline' goal downloads all project dependencies
# without building the actual project.
RUN mvn dependency:go-offline -B

# Copy the rest of the application source code
COPY src src

# Build the Spring Boot application into a JAR file
# The 'package' goal compiles the code and packages it into a JAR
# '-DskipTests' skips running tests, which speeds up the build for production images
RUN mvn package -DskipTests

# Stage 2: Create the final, smaller runtime image
# Use a JRE-only image for a smaller footprint, as we only need to run the JAR
FROM eclipse-temurin:17-jre-focal

# Set the working directory for the application
WORKDIR /app

# Copy the built JAR file from the 'builder' stage into the current stage
# The JAR is typically found in target/ and named after your artifactId and version
# In your case, it will be spring-boot-jpa-paging-sorting-0.0.1-SNAPSHOT.jar
COPY --from=builder /app/target/spring-boot-jpa-paging-sorting-0.0.1-SNAPSHOT.jar app.jar

# Expose the port your Spring Boot application runs on (default is 8080)
EXPOSE 8080

# Define the command to run the application when the container starts
# 'java -jar app.jar' executes the Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]