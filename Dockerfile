# Étape 1 : compilation avec Gradle + JDK 21
FROM gradle:8.12-jdk21 AS build
WORKDIR /app
COPY . .
RUN gradle installDist --no-daemon

# Étape 2 : image finale légère (JRE seul)
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/install/cinemastre/ ./

# La base SQLite vit dans /app/data (monté en volume par docker-compose)
ENV DB_PATH=/app/data/cinemastre.db

EXPOSE 8080
CMD ["./bin/cinemastre"]
