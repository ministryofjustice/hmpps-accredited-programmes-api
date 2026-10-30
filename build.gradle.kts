@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "11.0.14"
  `jvm-test-suite`
  kotlin("plugin.spring") version "2.4.21"
  kotlin("plugin.jpa") version "2.4.21"
}

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
}

ext["netty.version"] = "4.2.18.Final"
// CVE-2026-47884 / CVE-2026-47890
ext["spring-framework.version"] = "7.0.10"
// CVE-2026-59270
ext["spring-security.version"] = "7.0.8"
// CVE-2026-68525
ext["tomcat.version"] = "11.0.27"

dependencies {
  val kotestVersion = "6.2.6"
  val springdocVersion = "3.2.0"
  val sentryVersion = "8.62.0"
  val jsonWebtokenVersion = "0.13.0"
  val springSecurityVersion = "7.1.2"
  val flywayVersion = "13.12.0"

  runtimeOnly("org.postgresql:postgresql:42.7.14")
  implementation("org.flywaydb:flyway-core:$flywayVersion")
  implementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter:3.0.3")
  implementation("uk.gov.justice.service.hmpps:hmpps-sqs-spring-boot-starter:7.4.1")

  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-web")
  implementation("org.springframework.boot:spring-boot-starter-cache")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.springframework.boot:spring-boot-starter-flyway")
  implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-csv")
  implementation("com.google.guava:guava:33.7.2-jre")

  implementation("io.sentry:sentry-spring-boot-4:$sentryVersion")
  implementation("io.sentry:sentry-logback:$sentryVersion")

  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$springdocVersion")

  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.11.0")
  implementation("org.openfolder:kotlin-asyncapi-spring-web:3.2.4")
  implementation("org.apache.tomcat.embed:tomcat-embed-core:11.0.27")
  implementation("org.apache.tomcat.embed:tomcat-embed-websocket:11.0.27")

  testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
  testImplementation("com.ninja-squad:springmockk:5.0.1")
  testImplementation("io.jsonwebtoken:jjwt-api:$jsonWebtokenVersion")
  testImplementation("io.jsonwebtoken:jjwt-impl:$jsonWebtokenVersion")
  testImplementation("io.jsonwebtoken:jjwt-orgjson:$jsonWebtokenVersion")
  testImplementation("au.com.dius.pact.provider:junit5spring:4.7.6")
  testImplementation("org.springframework.security:spring-security-test:$springSecurityVersion")
  testImplementation("org.wiremock:wiremock-standalone:3.13.2")
  testImplementation("org.springframework.boot:spring-boot-starter-test")
  testImplementation("org.springframework.boot:spring-boot-starter-webclient-test")
  testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
  testImplementation("org.springframework.boot:spring-boot-starter-webflux-test")
  testImplementation("org.awaitility:awaitility-kotlin")

  testImplementation("org.testcontainers:testcontainers:2.0.5")
  testImplementation("org.testcontainers:testcontainers-postgresql:2.0.5")
  testImplementation("org.testcontainers:testcontainers-localstack:2.0.5")
  testImplementation("org.testcontainers:testcontainers-junit-jupiter:2.0.5")
  testImplementation("org.jetbrains.kotlin:kotlin-test")
  testImplementation("uk.gov.justice.service.hmpps:hmpps-subject-access-request-test-support:2.8.6")
  testImplementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter-test:3.0.3")

  runtimeOnly("org.flywaydb:flyway-database-postgresql:$flywayVersion")
}

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(25))
}

kotlin {
  kotlinDaemonJvmArgs = listOf("-Xmx2g")
  jvmToolchain(25)
}

testing {
  suites {
    val test by getting(JvmTestSuite::class) {
      useJUnitJupiter()

      targets {
        all {
          testTask.configure {
            // Uncomment this next line if you need to debug tests in CI
            // testLogging.showStandardStreams = true
            maxParallelForks = 1
            environment["pact_do_not_track"] = "true"
            val pactProviderTag = System.getenv("PACT_PROVIDER_TAG")
            val pactProviderVersion = System.getenv("PACT_PROVIDER_VERSION")

            if (pactProviderTag != null && pactProviderVersion != null) {
              environment["pact.provider.tag"] = pactProviderTag
              environment["pact.provider.version"] = pactProviderVersion
              environment["pact.verifier.publishResults"] =
                System.getenv("PACT_PUBLISH_RESULTS") ?: "false"
            }
          }
        }
      }
    }
  }
}

tasks {
  withType<KotlinCompile> {
    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_25)
    }

    kotlin.sourceSets["main"].kotlin.srcDir(layout.buildDirectory.dir("generated/src/main/kotlin"))
    kotlin.sourceSets["main"].kotlin.srcDir(layout.buildDirectory.dir("generated/src/main/resources"))
  }

  register("bootRunLocal") {
    group = "application"
    description = "Runs this project as a Spring Boot application with the local profile"
    doFirst {
      bootRun.configure {
        systemProperty("spring.profiles.active", "local,dev,seed")
      }
    }
    finalizedBy("bootRun")
  }
}

ktlint {
  filter {
    val openApiGeneratedSrcPath = layout.buildDirectory.dir("generated").get().asFile.path
    exclude { it.file.startsWith(openApiGeneratedSrcPath) }
  }
}

allOpen {
  annotations(
    "jakarta.persistence.Entity",
    "jakarta.persistence.MappedSuperclass",
    "jakarta.persistence.Embeddable",
  )
}
