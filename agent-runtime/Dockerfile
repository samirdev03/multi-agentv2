# syntax=docker/dockerfile:1

##### Stufe 1: Build #####
FROM maven:3.9-eclipse-temurin-21 AS build

WORKDIR /build

# Build-Context ist das Root des multi-agent-Repositories
COPY . .

WORKDIR /build/agent-runtime

RUN mvn -B clean package -DskipTests


##### Stufe 2: Spring-Boot-Layer extrahieren #####
FROM eclipse-temurin:21-jre-alpine AS extract

WORKDIR /extract

COPY --from=build /build/agent-runtime/target/agent-runtime-*.jar application.jar

RUN java -Djarmode=layertools -jar application.jar extract


##### Stufe 3: Runtime #####
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Spring-Benutzer erstellen
RUN addgroup -S spring && adduser -S spring -G spring

# curl für HEALTHCHECK installieren
RUN apk add --no-cache curl

# SQLite-Datenverzeichnis erstellen und dem Spring-Benutzer Schreibrechte geben
RUN mkdir -p /app/data && chown -R spring:spring /app/data

# Spring-Boot-Layer kopieren
COPY --from=extract --chown=spring:spring /extract/dependencies/ ./
COPY --from=extract --chown=spring:spring /extract/spring-boot-loader/ ./
COPY --from=extract --chown=spring:spring /extract/snapshot-dependencies/ ./
COPY --from=extract --chown=spring:spring /extract/application/ ./

# Anwendung nicht als root ausführen
USER spring:spring

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=20s --retries=3 \
    CMD curl -fsS http://localhost:8080/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]