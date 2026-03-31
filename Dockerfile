# syntax=docker/dockerfile:1
# Multi-stage: JDK build → JRE runtime with extracted layers (fast cold start, small cache-friendly image).
# Run: docker run --rm -p 8083:8083 -e SPRING_ELASTICSEARCH_URIS=http://es:9200 <image>

FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

RUN chmod +x gradlew && ./gradlew dependencies --no-daemon

COPY src src
RUN ./gradlew bootJar --no-daemon -x test

WORKDIR /app/layers
RUN JAR=$(ls /app/build/libs/*.jar | grep -v plain) && \
    java -Djarmode=tools -jar "$JAR" extract --layers --destination .

FROM eclipse-temurin:21-jre-jammy AS runtime

RUN groupadd --gid 1000 app && \
    useradd --uid 1000 --gid app --shell /bin/false --create-home app

WORKDIR /app

COPY --from=builder --chown=app:app /app/layers/dependencies/ ./
COPY --from=builder --chown=app:app /app/layers/spring-boot-loader/ ./
COPY --from=builder --chown=app:app /app/layers/snapshot-dependencies/ ./
COPY --from=builder --chown=app:app /app/layers/application/ ./

USER app

EXPOSE 8083

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=50.0 -XX:+ExitOnOutOfMemoryError -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
