package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration.health

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration.IntegrationTestBase
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class InfoTest : IntegrationTestBase() {

  @Test
  fun `Requesting info endpoint should be accessible and contain correct build name`() {
    webTestClient
      .get()
      .uri("/info")
      .exchange()
      .expectStatus()
      .isOk
      .expectBody()
      .jsonPath("build.name").isEqualTo("hmpps-accredited-programmes-api")
  }

  @Test
  fun `Info endpoint should report build version with today's date`() {
    webTestClient
      .get()
      .uri("/info")
      .exchange()
      .expectStatus().isOk
      .expectBody()
      .jsonPath("build.version").value<String> {
        assertThat(it).startsWith(LocalDateTime.now().format(DateTimeFormatter.ISO_DATE))
      }
  }
}
