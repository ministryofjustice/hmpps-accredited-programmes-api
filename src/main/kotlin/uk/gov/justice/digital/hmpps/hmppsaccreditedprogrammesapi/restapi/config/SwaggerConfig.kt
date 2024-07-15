package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.config

import org.springdoc.core.customizers.OpenApiCustomizer
import org.springdoc.core.models.GroupedOpenApi
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {
  @Bean
  fun api(): GroupedOpenApi {
    fun apiStatisticsOpenApiCustomizer(): OpenApiCustomizer {
      return OpenApiCustomizer { openApi ->
        openApi.info
          .title("Statistics API")
          .description(
            """
            This API provides API endpoints that return statistical data about accredited programmes. 
            Note that these endpoints are created manually rather than via open api.yaml.
            """.trimIndent(),
          )
          .version("1.0.0")
      }
    }
    return GroupedOpenApi.builder()
      .group("API-Statistics")
      .packagesToScan("uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.statistics")
      .addOpenApiCustomizer(apiStatisticsOpenApiCustomizer())
      .build()
  }
}
