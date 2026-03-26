# Stage 1: Build
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

COPY ../pom.xml ../pom.xml
COPY ../iwos-common/pom.xml ../iwos-common/pom.xml
COPY pom.xml .

# Download dependencies (cached layer)
RUN --mount=type=cache,target=/root/.m2 \
    cd .. && mvn dependency:go-offline -pl iwos-common,$(basename $(pwd)) -am

COPY ../iwos-common/src ../iwos-common/src
COPY src ./src

RUN --mount=type=cache,target=/root/.m2 \
    cd .. && mvn clean package -pl iwos-common,$(basename $(pwd)) -am -DskipTests

# Stage 2: Run
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

RUN addgroup -S iwos && adduser -S iwos -G iwos
USER iwos

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]
