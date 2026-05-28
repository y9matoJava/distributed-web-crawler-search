# Этап 1: Сборка
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Копируем родительский pom и исходники всего проекта
COPY pom.xml .
COPY common ./common
COPY crawler-core ./crawler-core
COPY crawler-worker ./crawler-worker
COPY search-api ./search-api

# Собираем только search-api (и то, от чего он зависит)
RUN mvn clean package -pl search-api -am -DskipTests

# Этап 2: Выполнение (Runtime)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Копируем собранный JAR
COPY --from=builder /app/search-api/target/search-api-*.jar app.jar

# Порт, на котором работает API
EXPOSE 8081

# Настройки JVM
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
