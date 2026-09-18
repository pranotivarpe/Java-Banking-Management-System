# Stage 1: build the jar with Maven + the full JDK.
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /build
COPY pom.xml .
RUN mvn -B dependency:go-offline
COPY src ./src
RUN mvn -B package -DskipTests

# Stage 2: run it on a slim JRE-only image — no build tools, no source, no Maven cache
# shipped to production. The final image only contains what's needed to execute the jar.
# (Not -alpine: that tag has no linux/arm64 manifest, which breaks builds on Apple Silicon.)
FROM eclipse-temurin:17-jre
WORKDIR /app
COPY --from=build /build/target/banking-management-system-*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
