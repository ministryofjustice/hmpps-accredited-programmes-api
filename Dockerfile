# Pinned to a digest that includes apt 2.4.15ubuntu0.1, which fixes CVE-2026-109518
# (SNYK-UBUNTU2204-APT-22204274). Digest covers linux/amd64 and linux/arm64.
ARG BASE_IMAGE=ghcr.io/ministryofjustice/hmpps-eclipse-temurin:25-jre-jammy@sha256:d0e3f9c1997376b15b66ab601ca88375063f003e0f8add6fa8b264e531a957ef
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
# temp fix for CVE-2026-114938, remove once the base image has been updated to a version that includes the fix 25.0.5.1 or later.
ENTRYPOINT ["java", "-XX:-UseBASE64Intrinsics", "-XX:+ExitOnOutOfMemoryError", "-XX:+AlwaysActAsServerClassMachine", "-javaagent:agent.jar", "-jar", "app.jar"]

