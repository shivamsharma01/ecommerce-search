# syntax=docker/dockerfile:1
# Multi-stage: JDK build → JRE runtime.
# This intentionally runs the fat jar with `java -jar` to avoid classpath/layout issues
# that can cause `org.springframework.boot.loader.launch.JarLauncher` ClassNotFound at runtime.

FROM eclipse-temurin:21-jdk-jammy AS builder

WORKDIR /app

COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

RUN chmod +x gradlew && ./gradlew dependencies --no-daemon

COPY src src
RUN ./gradlew bootJar --no-daemon -x test && \
    JAR=$(ls /app/build/libs/*.jar | grep -v plain | head -n1) && \
    cp "$JAR" /app/app.jar

FROM eclipse-temurin:21-jre-jammy AS runtime

RUN groupadd --gid 1000 app && \
    useradd --uid 1000 --gid app --shell /bin/false --create-home app

WORKDIR /app
COPY --from=builder --chown=app:app /app/app.jar /app/app.jar

USER app

EXPOSE 8083

ENV JAVA_OPTS="-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0 -XX:InitialRAMPercentage=50.0 -XX:+ExitOnOutOfMemoryError -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar /app/app.jar"]
