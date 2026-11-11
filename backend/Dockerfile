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
# A EC2 é uma t3.micro (1 GB de RAM, dividida com o nginx). O container roda com limite de memória (--memory no
# deploy) e a JVM usa no máximo 60% dele de heap; o GC serial gasta menos memória que o padrão nesse tamanho.
ENTRYPOINT ["java", "-XX:MaxRAMPercentage=60", "-XX:+UseSerialGC", "-XX:MaxMetaspaceSize=160m", "-jar", "app.jar"]
