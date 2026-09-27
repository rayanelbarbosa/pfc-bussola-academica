# ---------- Etapa 1: build do .jar com Maven ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn -B -q dependency:go-offline
COPY src ./src
RUN mvn -B -q package -DskipTests

# ---------- Etapa 2: imagem final, so com o Java necessario pra rodar ----------
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S bussola && adduser -S bussola -G bussola
COPY --from=build /app/target/backend-*.jar app.jar
USER bussola
ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
