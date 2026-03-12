# Stage: Runtime [cite: 2026-03-13]
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

COPY target/barangay-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080

# Patakbuhin ang app [cite: 2026-03-13]
ENTRYPOINT ["java", "-jar", "app.jar"]