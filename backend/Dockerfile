# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline -DskipTests

COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring \
    && mkdir -p /app/uploads \
    && chown spring:spring /app/uploads

COPY --from=build /app/target/vendor-onboarding-0.0.1-SNAPSHOT.jar app.jar

ENV PORT=8080
ENV APP_UPLOAD_DIR=/app/uploads

USER spring:spring
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]
