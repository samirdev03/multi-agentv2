# syntax=docker/dockerfile:1
#
# Diese Datei liegt in telegram-connector-v2/Dockerfile.
#
# Der Docker-Build-Context ist bewusst das Repo-ROOT, nicht nur dieser
# Modul-Ordner (siehe docker-compose.yml: "context: .",
# "dockerfile: telegram-connector-v2/Dockerfile"). Grund: telegram-connector-v2/
# pom.xml könnte eine Parent-POM aus dem multi-agent-Root referenzieren
# (<relativePath>../pom.xml</relativePath>) - mit einem auf dieses Modul
# beschränkten Kontext wäre die beim Build nicht sichtbar und Maven würde
# fehlschlagen. Kehrseite: Layer-Caching ist etwas grober (Änderungen an
# einem anderen Modul im selben Repo invalidieren auch diesen Cache-Layer) -
# für den aktuellen Ein-Modul-Stand kein Problem, bei Bedarf später über
# Maven-Reactor-Flags (-pl/-am) und BuildKit-Cache-Mounts optimierbar.

##### Stufe 1: Build #####
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /build
COPY . .
WORKDIR /build/telegram-connector-v2
RUN mvn -B clean package -DskipTests

##### Stufe 2: Layer-Extraktion #####
FROM eclipse-temurin:21-jre-alpine AS extract
WORKDIR /extract
COPY --from=build /build/telegram-connector-v2/target/telegram-connector-*.jar application.jar
RUN java -Djarmode=layertools -jar application.jar extract

##### Stufe 3: Laufzeit-Image #####
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Nicht als root laufen lassen (verbreitetes Anti-Pattern bei Container-Images)
RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

COPY --from=extract --chown=spring:spring /extract/dependencies/ ./
COPY --from=extract --chown=spring:spring /extract/spring-boot-loader/ ./
COPY --from=extract --chown=spring:spring /extract/snapshot-dependencies/ ./
COPY --from=extract --chown=spring:spring /extract/application/ ./

EXPOSE 8080

# Setzt spring-boot-starter-actuator voraus (Health-Endpoint) - siehe
# telegram-connector-v2/pom.xml. Ohne diese Abhaengigkeit liefert /actuator/health
# ein generisches 404 vom normalen Spring-MVC-Dispatching statt eines Health-Status,
# und der Container wird trotz funktionierender Anwendung dauerhaft als unhealthy
# markiert (siehe ActuatorHealthIntegrationTest fuer den Regressionstest).
HEALTHCHECK --interval=30s --timeout=3s --start-period=20s --retries=3 \
    CMD wget -qO- http://localhost:8080/actuator/health | grep -q '"status":"UP"' || exit 1

ENTRYPOINT ["java", "org.springframework.boot.loader.launch.JarLauncher"]