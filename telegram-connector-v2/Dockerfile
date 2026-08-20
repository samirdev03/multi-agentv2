FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY target/telegram-connector-*.jar app.jar
ENTRYPOINT ["java", "-jar", "app.jar"]
