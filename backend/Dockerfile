# 1) Build stage: compile your JAR
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /workspace
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# 2) Run stage: slim runtime image
FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
# Copy the fat JAR from the build stage
COPY --from=build /workspace/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","/app/app.jar"]
