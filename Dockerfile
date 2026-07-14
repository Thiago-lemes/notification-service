# ---- Build stage ----
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copia primeiro só o pom.xml para aproveitar cache de dependências
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copia o resto do código e builda
COPY src ./src
RUN mvn clean package -Dmaven.test.skip=true -B

# ---- Runtime stage ----
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copia o jar gerado no stage anterior
COPY --from=build /app/target/*.jar app.jar

# Porta que o Spring Boot usa (conforme seu application.yml)
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]