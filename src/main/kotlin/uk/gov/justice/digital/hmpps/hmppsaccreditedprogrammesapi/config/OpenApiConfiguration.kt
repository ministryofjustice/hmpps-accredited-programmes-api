package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.config

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType
import io.swagger.v3.oas.annotations.security.SecurityScheme
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.media.Schema
import org.springdoc.core.utils.SpringDocUtils
import org.springframework.boot.info.BuildProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Configuration
@SecurityScheme(
  name = "bearerAuth",
  type = SecuritySchemeType.HTTP,
  bearerFormat = "JWT",
  scheme = "bearer",
)
class OpenApiConfiguration(buildProperties: BuildProperties) {
  private val version: String = buildProperties.version

  init {
    val schema: Schema<LocalDateTime> = Schema<LocalDateTime>()
    schema.example(LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")))
    SpringDocUtils.getConfig().replaceWithSchema(LocalDateTime::class.java, schema)
  }

  @Bean
  fun customOpenAPI(buildProperties: BuildProperties): OpenAPI? = OpenAPI()
    .info(
      Info().title("HMPPS Accredited Programmes API").version(version).description(
        "API for Accredited programmes",
      ),
    )
}
