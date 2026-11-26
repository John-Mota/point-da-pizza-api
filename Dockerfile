# Stage 1: Build da aplicacao Quarkus (Maven 3.9 + Java 25)
FROM maven:3.9-eclipse-temurin-25 AS builder

WORKDIR /workspace

COPY pom.xml ./
RUN mvn dependency:go-offline -B

COPY src/ src/
RUN mvn package -DskipTests -B

# Stage 2: Runtime enxuto otimizado para Render (Red Hat UBI9 OpenJDK 25)
FROM registry.access.redhat.com/ubi9/openjdk-25-runtime:1.24

ENV LANGUAGE='pt_BR:pt' \
    TZ='America/Fortaleza' \
    PORT='8080' \
    JAVA_MAX_MEM_RATIO='60' \
    JAVA_INITIAL_MEM_RATIO='25' \
    JAVA_OPTS_APPEND="-Dquarkus.http.host=0.0.0.0 -Djava.util.logging.manager=org.jboss.logmanager.LogManager" \
    JAVA_APP_JAR="/deployments/quarkus-run.jar"

COPY --from=builder --chown=185 /workspace/target/quarkus-app/lib/ /deployments/lib/
COPY --from=builder --chown=185 /workspace/target/quarkus-app/*.jar /deployments/
COPY --from=builder --chown=185 /workspace/target/quarkus-app/app/ /deployments/app/
COPY --from=builder --chown=185 /workspace/target/quarkus-app/quarkus/ /deployments/quarkus/

EXPOSE 8080
USER 185

HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
  CMD sh -c 'curl -fsS http://localhost:${PORT:-8080}/q/health/live || exit 1'

ENTRYPOINT [ "/opt/jboss/container/java/run/run-java.sh" ]
