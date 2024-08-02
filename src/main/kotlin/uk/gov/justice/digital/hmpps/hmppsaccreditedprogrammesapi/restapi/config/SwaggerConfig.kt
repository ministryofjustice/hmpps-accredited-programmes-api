package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.config

import org.springdoc.core.customizers.OpenApiCustomizer
import org.springdoc.core.models.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {
  @Bean
  fun api(): GroupedOpenApi {
    fun openApiCustomizer(): OpenApiCustomizer {
      return OpenApiCustomizer { openApi ->
        openApi.info
          .title("APIs (Not generated via openApi)")
          .description(
            """
            Note that these endpoints are created manually rather than via open api.yaml.
            """.trimIndent(),
          )
          .version("1.0.0")
      }
    }

    return GroupedOpenApi.builder()
      .group("API (outside of openApi)")
      .packagesToScan(
        "uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.statistics",
        "uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.pni",
        "uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.controllers",
      )
      .addOpenApiCustomizer(openApiCustomizer())
      .build()
  }
}
