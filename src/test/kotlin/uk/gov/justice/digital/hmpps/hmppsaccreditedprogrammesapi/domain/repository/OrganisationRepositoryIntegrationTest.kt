package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration.IntegrationTestBase
import kotlin.test.Test

/**
 * Integration coverage for the batch [OrganisationRepository.findAllByCodeIn]
 * derived query. Guards the Spring Data method-name derivation against silent
 * breakage from a future rename of `OrganisationEntity.code`, and pins the
 * "unmatched codes are silently dropped" semantic the SAR content builder
 * relies on.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class OrganisationRepositoryIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var organisationRepository: OrganisationRepository

  private fun seedOrganisations() {
    persistenceHelper.clearAllTableContent()
    persistenceHelper.createOrganisation(code = "BXI", name = "Brixton (HMP)")
    persistenceHelper.createOrganisation(code = "BMI", name = "Belmarsh (HMP)")
    persistenceHelper.createOrganisation(code = "WSI", name = "Wormwood Scrubs (HMP)")
  }

  @Test
  fun `findAllByCodeIn returns one row per matched code`() {
    // Given
    seedOrganisations()

    // When
    val result = organisationRepository.findAllByCodeIn(listOf("BXI", "BMI"))

    // Then
    val byCode = result.associate { it.code to it.name }
    byCode shouldBe mapOf(
      "BXI" to "Brixton (HMP)",
      "BMI" to "Belmarsh (HMP)",
    )
  }

  @Test
  fun `findAllByCodeIn silently drops unmatched codes`() {
    // Given
    seedOrganisations()

    // When – NOT_A_PRISON has no matching organisation row.
    val result = organisationRepository.findAllByCodeIn(listOf("BXI", "NOT_A_PRISON"))

    // Then – only the matched code is returned; no exception, no null row.
    result.map { it.code } shouldBe listOf("BXI")
  }

  @Test
  fun `findAllByCodeIn returns empty list when no codes match`() {
    // Given
    seedOrganisations()

    // When
    val result = organisationRepository.findAllByCodeIn(listOf("UNKNOWN_1", "UNKNOWN_2"))

    // Then
    result.shouldBeEmpty()
  }
}
