@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "8.3.6"
  `jvm-test-suite`
  kotlin("plugin.spring") version "2.2.10"
  kotlin("plugin.jpa") version "2.2.10"
}

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
}

ext["hibernate.version"] = "6.6.11.Final"

dependencies {
  val kotestVersion = "6.0.0"
  val springdocVersion = "2.8.9"
  val sentryVersion = "8.19.1"
  val jsonWebtokenVersion = "0.12.7"
  val springSecurityVersion = "6.5.2"

  implementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter:1.5.0")
  runtimeOnly("org.postgresql:postgresql:42.7.7")

  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-csv")
  implementation("com.google.guava:guava:33.4.8-jre")

  implementation("io.sentry:sentry-spring-boot-starter-jakarta:$sentryVersion")
  implementation("io.sentry:sentry-logback:$sentryVersion")

  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$springdocVersion")

  implementation("uk.gov.justice.service.hmpps:hmpps-sqs-spring-boot-starter:5.4.10")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.10.2")
  implementation("org.openfolder:kotlin-asyncapi-spring-web:3.1.1")
  implementation("org.apache.tomcat.embed:tomcat-embed-core:11.0.10")

  runtimeOnly("org.flywaydb:flyway-database-postgresql")

  testImplementation("com.h2database:h2")
  testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
  testImplementation("com.ninja-squad:springmockk:4.0.2")
  testImplementation("io.jsonwebtoken:jjwt-api:$jsonWebtokenVersion")
  testImplementation("io.jsonwebtoken:jjwt-impl:$jsonWebtokenVersion")
  testImplementation("io.jsonwebtoken:jjwt-orgjson:$jsonWebtokenVersion")
  testImplementation("au.com.dius.pact.provider:junit5spring:4.6.17")
  testImplementation("org.springframework.security:spring-security-test:$springSecurityVersion")
  testImplementation("org.wiremock:wiremock-standalone:3.13.1")

  testImplementation("org.awaitility:awaitility-kotlin")

  testImplementation("org.testcontainers:testcontainers:1.21.3")
  testImplementation("org.testcontainers:postgresql:1.21.3")
  testImplementation("org.testcontainers:junit-jupiter:1.21.3")
  testImplementation("org.jetbrains.kotlin:kotlin-test")
}

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

kotlin {
  kotlinDaemonJvmArgs = listOf("-Xmx2g")
  jvmToolchain(21)
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
            environment["pact.provider.tag"] = environment["PACT_PROVIDER_TAG"]
            environment["pact.provider.version"] = environment["PACT_PROVIDER_VERSION"]
            environment["pact.verifier.publishResults"] = environment["PACT_PUBLISH_RESULTS"] ?: "false"
          }
        }
      }
    }
  }
}

tasks {
  withType<KotlinCompile> {
    compilerOptions {
      jvmTarget.set(JvmTarget.JVM_21)
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
