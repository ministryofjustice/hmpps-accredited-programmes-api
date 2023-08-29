import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "5.4.0"
  kotlin("plugin.spring") version "1.9.10"
  kotlin("plugin.jpa") version "1.9.10"
  id("org.openapi.generator") version "7.0.0"
}

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
}

dependencies {
  val kotestVersion = "5.6.2"
  val springdocVersion = "2.2.0"
  val sentryVersion = "6.28.0"

  runtimeOnly("org.postgresql:postgresql:42.6.0")

  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-client")
  implementation("org.springframework.boot:spring-boot-starter-data-jpa")
  implementation("org.flywaydb:flyway-core")
  implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-csv:2.15.2")
  implementation("com.google.guava:guava:32.1.2-jre")

  implementation("io.sentry:sentry-spring-boot-starter-jakarta:$sentryVersion")
  implementation("io.sentry:sentry-logback:$sentryVersion")

  implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:$springdocVersion")
  implementation("org.springdoc:springdoc-openapi-starter-webmvc-api:$springdocVersion")

  testImplementation("com.h2database:h2")
  testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
  testImplementation("com.ninja-squad:springmockk:4.0.2")
  testImplementation("io.jsonwebtoken:jjwt-api:0.11.5")
  testImplementation("io.jsonwebtoken:jjwt-impl:0.11.5")
  testImplementation("io.jsonwebtoken:jjwt-orgjson:0.11.5")
  testImplementation("au.com.dius.pact.provider:junit5spring:4.6.2")
}

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(19))
}

kotlin {
  kotlinDaemonJvmArgs = listOf("-Xmx1024m")
}

tasks {
  withType<KotlinCompile> {
    kotlinOptions {
      jvmTarget = "19"
    }

    kotlin.sourceSets["main"].kotlin.srcDir(layout.buildDirectory.dir("generated/src/main/kotlin"))
    kotlin.sourceSets["main"].kotlin.srcDir(layout.buildDirectory.dir("generated/src/main/resources"))

    dependsOn("openApiGenerate")
  }

  test {
    useJUnitPlatform()

    maxParallelForks = Runtime.getRuntime().availableProcessors()

    environment["pact_do_not_track"] = "true"
    environment["pact.provider.tag"] = environment["PACT_PROVIDER_TAG"]
    environment["pact.provider.version"] = environment["PACT_PROVIDER_VERSION"]
    environment["pact.verifier.publishResults"] = environment["PACT_PUBLISH_RESULTS"] ?: "false"
  }

  register("bootRunLocal") {
    group = "application"
    description = "Runs this project as a Spring Boot application with the local profile"
    doFirst {
      bootRun.configure {
        systemProperty("spring.profiles.active", "local")
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
    "jakarta.persistence.Embeddable"
  )
}
