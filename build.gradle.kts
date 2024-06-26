import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "6.0.1"
  `jvm-test-suite`
  kotlin("plugin.spring") version "2.0.0"
  kotlin("plugin.jpa") version "2.0.0"
  id("org.openapi.generator") version "7.6.0"
}

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
}

dependencies {
  val kotestVersion = "5.9.1"
  val springdocVersion = "2.6.0"
  val sentryVersion = "7.10.0"
  val jsonWebtokenVersion = "0.12.6"
  val springSecurityVersion = "6.3.1"

  implementation("uk.gov.justice.service.hmpps:hmpps-kotlin-spring-boot-starter:1.0.1")
  runtimeOnly("org.postgresql:postgresql:42.7.3")

  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-csv")
  implementation("com.google.guava:guava:33.2.1-jre")

  implementation("io.sentry:sentry-spring-boot-starter-jakarta:$sentryVersion")
  implementation("io.sentry:sentry-logback:$sentryVersion")

  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$springdocVersion")
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-api:$springdocVersion")

  implementation("uk.gov.justice.service.hmpps:hmpps-sqs-spring-boot-starter:4.0.1")
  implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.1")
  runtimeOnly("org.flywaydb:flyway-database-postgresql")
  runtimeOnly("org.postgresql:postgresql:42.7.3")

  testImplementation("com.h2database:h2")
  testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
  testImplementation("com.ninja-squad:springmockk:4.0.2")
  testImplementation("io.jsonwebtoken:jjwt-api:$jsonWebtokenVersion")
  testImplementation("io.jsonwebtoken:jjwt-impl:$jsonWebtokenVersion")
  testImplementation("io.jsonwebtoken:jjwt-orgjson:$jsonWebtokenVersion")
  testImplementation("au.com.dius.pact.provider:junit5spring:4.6.10")
  testImplementation("io.github.bluegroundltd:kfactory:1.0.0")
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

    dependsOn("openApiGenerate")
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

  runKtlintCheckOverMainSourceSet {
    dependsOn("openApiGenerate")
  }
}

openApiGenerate {
  generatorName.set("kotlin-spring")
  inputSpec.set("$rootDir/src/main/resources/static/api.yml")
  outputDir.set(layout.buildDirectory.dir("generated").map { it.asFile.path })
  apiPackage.set("uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api")
  modelPackage.set("uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model")
  configOptions.apply {
    put("useSpringBoot3", "true")
    put("basePackage", "uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi")
    put("delegatePattern", "true")
    put("gradleBuildFile", "false")
    put("exceptionHandler", "false")
    put("useBeanValidation", "false")
    put("dateLibrary", "custom")
  }
  typeMappings.put("DateTime", "Instant")
  importMappings.put("Instant", "java.time.Instant")
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
