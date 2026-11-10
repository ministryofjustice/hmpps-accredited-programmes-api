# Pinned to a digest that includes apt 2.4.15ubuntu0.1, which fixes CVE-2026-109518
# (SNYK-UBUNTU2204-APT-22204274). Digest covers linux/amd64 and linux/arm64.
ARG BASE_IMAGE=ghcr.io/ministryofjustice/hmpps-eclipse-temurin:25-jre-jammy@sha256:f8b6979d25b9cadd78d72b45b41cd9cbe9ff0f8b750c657dd3cdb5650ba6f888
FROM --platform=$BUILDPLATFORM ${BASE_IMAGE} AS builder

ARG BUILD_NUMBER
ENV BUILD_NUMBER=${BUILD_NUMBER:-1_0_0}

WORKDIR /builder
COPY hmpps-accredited-programmes-api-${BUILD_NUMBER}.jar app.jar
RUN java -Djarmode=tools -jar app.jar extract --layers --destination extracted

FROM ${BASE_IMAGE}

WORKDIR /app
COPY --chown=appuser:appgroup applicationinsights.json ./
COPY --chown=appuser:appgroup applicationinsights.dev.json ./
COPY --chown=appuser:appgroup applicationinsights-agent*.jar ./agent.jar
COPY --from=builder --chown=appuser:appgroup /builder/extracted/dependencies/ ./
COPY --from=builder --chown=appuser:appgroup /builder/extracted/spring-boot-loader/ ./
COPY --from=builder --chown=appuser:appgroup /builder/extracted/snapshot-dependencies/ ./
COPY --from=builder --chown=appuser:appgroup /builder/extracted/application/ ./

ENTRYPOINT ["java", "-XX:+ExitOnOutOfMemoryError", "-XX:+AlwaysActAsServerClassMachine", "-javaagent:agent.jar", "-jar", "app.jar"]

