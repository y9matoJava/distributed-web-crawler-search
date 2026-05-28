# Этап 1: Сборка
FROM maven:3.9.6-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Копируем родительский pom и исходники всего проекта
COPY pom.xml .
COPY common ./common
COPY crawler-core ./crawler-core
COPY crawler-worker ./crawler-worker
COPY search-api ./search-api

# Собираем только crawler-worker (и то, от чего он зависит: common, crawler-core)
RUN mvn clean package -pl crawler-worker -am -DskipTests

# Этап 2: Выполнение (Runtime)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Копируем собранный JAR из этапа сборки
COPY --from=builder /app/crawler-worker/target/crawler-worker-*.jar app.jar

# Настройки для оптимизации JVM внутри контейнера
ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
