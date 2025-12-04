# Use JDK 25 for main app
FROM eclipse-temurin:25-jdk-jammy

WORKDIR /app

# Expose main app port
EXPOSE 8080

# Run Spring Boot using Maven wrapper
CMD ["./mvnw", "spring-boot:run"]
