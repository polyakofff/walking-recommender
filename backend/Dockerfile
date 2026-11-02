FROM eclipse-temurin:21-jdk AS build

WORKDIR /app

COPY . .

RUN chmod +x gradlew

RUN --mount=type=cache,target=/root/.gradle \
    ./gradlew bootJar --no-daemon

RUN ./gradlew bootJar --no-daemon

RUN JAR=$(find build/libs -maxdepth 1 -type f \
    -name "*.jar" ! -name "*-plain.jar" | head -n 1) \
    && cp "$JAR" /app/app.jar


#FROM eclipse-temurin:21-jre
#
#WORKDIR /app
#
#COPY --from=build /app/app.jar app.jar
#
#EXPOSE 8080
#
#ENTRYPOINT ["java", "-jar", "app.jar"]

FROM eclipse-temurin:21-jre

WORKDIR /app

RUN groupadd --gid 10001 app \
    && useradd \
        --uid 10001 \
        --gid 10001 \
        --no-create-home \
        app \
    && mkdir /logs \
    && chown 10001:10001 /logs

COPY --from=build --chown=10001:10001 /app/app.jar app.jar

USER 10001:10001

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]