FROM eclipse-temurin:21-jdk-jammy

WORKDIR /app


COPY .mvn .mvn
COPY mvnw pom.xml ./
COPY src ./src

RUN chmod +x mvnw

# Expose NOC service port
EXPOSE 8081

CMD ["./mvnw", "spring-boot:run"]
