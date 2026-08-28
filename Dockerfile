# syntax=docker/dockerfile:1

# --- Build stage: compiles the Quarkus jar (mvn package also runs the
# Quinoa-driven `npm run build` for src/main/webui as part of the build). ---
FROM eclipse-temurin:21-jdk-noble AS build

# Vite 8 / rolldown-vite (src/main/webui) needs a modern Node; Quinoa is
# configured with quarkus.quinoa.package-manager-install=false so it expects
# one already on PATH instead of downloading its own.
RUN apt-get update && \
    curl -fsSL https://deb.nodesource.com/setup_22.x | bash - && \
    apt-get install -y --no-install-recommends nodejs && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /build
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
COPY src/ src/

RUN ./mvnw -B clean package -DskipTests

# --- Runtime stage: just the fast-jar layout + a JRE. ---
FROM eclipse-temurin:21-jre-noble

# Runs as root, matching the tomcat:9-jdk21-corretto image this replaces —
# the /data volume (VAPID/JWT keys) is an existing external volume already
# owned by root from prior Tomcat-container runs.
WORKDIR /deployments
COPY --from=build /build/target/quarkus-app/ ./

# VEKN card CSVs read at runtime by CardRegistry (jol.card.dir, default
# csv/core relative to the working dir). Unlike CardService — which falls
# back to a CloudFront-signed cards.json — the registry only reads these
# local files, so they must be in the image for card search / deck
# analytics / import to work.
COPY csv/ csv/

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "quarkus-run.jar"]
