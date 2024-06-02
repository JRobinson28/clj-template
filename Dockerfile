FROM clojure:temurin-22-tools-deps-1.11.3.1456-alpine AS build

WORKDIR /app

# Build uberjar
COPY . /app
RUN clojure -T:build build


FROM eclipse-temurin:22-jre-alpine

WORKDIR /app
COPY --from=build /app/target/standalone.jar /app/standalone.jar
RUN apk add --no-cache curl

EXPOSE 80
CMD ["java", "-Xmx256m", "-jar", "standalone.jar"]
