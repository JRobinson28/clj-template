FROM clojure:temurin-22-tools-deps-1.11.3.1456-alpine AS build

WORKDIR /app

COPY . /app
RUN clojure -T:build build


FROM eclipse-temurin:22-jre-alpine

WORKDIR /app
COPY --from=build /app/target/standalone.jar /app/standalone.jar

EXPOSE 2800
CMD ["java", "-Xmx256m", "-jar", "standalone.jar"]
