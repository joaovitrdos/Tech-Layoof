FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B clean package -DskipTests

FROM eclipse-temurin:21-jre-alpine
RUN addgroup -S layoof && adduser -S layoof -G layoof
WORKDIR /app
COPY --from=build /build/target/layoof-0.0.1-SNAPSHOT.jar app.jar
RUN mkdir -p /app/uploads && chown -R layoof:layoof /app
USER layoof
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
