FROM openjdk:8-jdk-alpine
VOLUME /tmp
COPY build/libs/shorturl-0.0.3-SNAPSHOT.jar app.jar
ENTRYPOINT [\
    "java", "-jar", "-XX:+UnlockExperimentalVMOptions", "-XX:+UseCGroupMemoryLimitForHeap", \
    "/app.jar", "--spring.profiles.active=docker"]
