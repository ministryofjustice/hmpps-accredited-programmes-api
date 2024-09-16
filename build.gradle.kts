import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "6.0.4"
  `jvm-test-suite`
  kotlin("plugin.spring") version "2.0.20"
  kotlin("plugin.jpa") version "2.0.20"
}

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
}

dependencies {
  val kotestVersion = "5.9.1"
  val springdocVersion = "2.6.0"
  val sentryVersion = "7.14.0"
  val jsonWebtokenVersion = "0.12.6"
  val springSecurityVersion = "6.3.3"

  implementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter:1.0.5")
  runtimeOnly("org.postgresql:postgresql:42.7.4")

  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-csv")
  implementation("com.google.guava:guava:33.3.0-jre")

  implementation("io.sentry:sentry-spring-boot-starter-jakarta:$sentryVersion")
  implementation("io.sentry:sentry-logback:$sentryVersion")

  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$springdocVersion")

  implementation("uk.gov.justice.service.hmpps:hmpps-sqs-spring-boot-starter:4.4.2")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.9.0")
  runtimeOnly("org.flywaydb:flyway-database-postgresql")
  runtimeOnly("org.postgresql:postgresql:42.7.4")

  testImplementation("com.h2database:h2")
  testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
  testImplementation("com.ninja-squad:springmockk:4.0.2")
  testImplementation("io.jsonwebtoken:jjwt-api:$jsonWebtokenVersion")
  testImplementation("io.jsonwebtoken:jjwt-impl:$jsonWebtokenVersion")
  testImplementation("io.jsonwebtoken:jjwt-orgjson:$jsonWebtokenVersion")
  testImplementation("au.com.dius.pact.provider:junit5spring:4.6.14")
  testImplementation("org.springframework.security:spring-security-test:$springSecurityVersion")
  testImplementation("com.github.tomakehurst:wiremock-standalone:3.0.1")
  testImplementation("org.awaitility:awaitility-kotlin")
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
    kotlinOptions {
      jvmTarget = "21"
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
