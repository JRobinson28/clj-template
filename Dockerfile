FROM clojure:temurin-21-tools-deps-1.12.6.1673-alpine AS build

WORKDIR /app

# Cache dependencies
COPY deps.edn build.clj /app/
RUN clojure -P && clojure -P -T:build

COPY . /app
RUN clojure -T:build build


FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S app && adduser -S app -G app
USER app

WORKDIR /app
COPY --from=build /app/target/standalone.jar /app/standalone.jar

ENV PORT=2800
EXPOSE 2800
CMD ["java", "-XX:MaxRAMPercentage=75", "-jar", "standalone.jar"]
