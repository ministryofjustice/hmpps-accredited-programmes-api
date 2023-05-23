plugins {
  id("uk.gov.justice.hmpps.gradle-spring-boot") version "4.8.7"
  kotlin("plugin.spring") version "1.8.21"
  id("org.openapi.generator") version "6.6.0"
}

configurations {
  testImplementation { exclude(group = "org.junit.vintage") }
}

dependencies {
  val kotestVersion = "5.6.2"
  val springdocVersion = "1.7.0"

  implementation("org.springframework.boot:spring-boot-starter-webflux")
  implementation("org.springframework.boot:spring-boot-starter-security")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")
  implementation("org.springframework.boot:spring-boot-starter-oauth2-client")

  implementation("org.springdoc:springdoc-openapi-data-rest:$springdocVersion")
  implementation("org.springdoc:springdoc-openapi-ui:$springdocVersion")
  implementation("org.springdoc:springdoc-openapi-kotlin:$springdocVersion")

  testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
  testImplementation("io.jsonwebtoken:jjwt-api:0.11.5")
  testImplementation("io.jsonwebtoken:jjwt-impl:0.11.5")
  testImplementation("io.jsonwebtoken:jjwt-orgjson:0.11.5")
}

java {
  toolchain.languageVersion.set(JavaLanguageVersion.of(19))
}

tasks {
  withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
      jvmTarget = "19"
    }

    kotlin.sourceSets["main"].kotlin.srcDir("$buildDir/generated/src/main/kotlin")
    dependsOn("openApiGenerate")
  }
}

tasks.register("bootRunLocal") {
  group = "application"
  description = "Runs this project as a Spring Boot application with the local profile"
  doFirst {
    tasks.bootRun.configure {
      systemProperty("spring.profiles.active", "local")
    }
  }
  finalizedBy("bootRun")
}

openApiGenerate {
  generatorName.set("kotlin-spring")
  inputSpec.set("$rootDir/src/main/resources/static/api.yml")
  outputDir.set("$buildDir/generated")
  apiPackage.set("uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api")
  modelPackage.set("uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model")
  configOptions.apply {
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
    exclude { it.file.path.contains("$buildDir${File.separator}generated${File.separator}") }
  }
}

tasks.runKtlintCheckOverMainSourceSet {
  dependsOn("openApiGenerate")
}
