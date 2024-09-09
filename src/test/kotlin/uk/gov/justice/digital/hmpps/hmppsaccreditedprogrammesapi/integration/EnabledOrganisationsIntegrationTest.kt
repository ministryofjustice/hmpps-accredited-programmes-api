package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration

import io.kotest.matchers.equals.shouldBeEqual
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.config.JwtAuthHelper
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.EnabledOrganisationRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.EnabledOrganisationService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.EnabledOrganisationEntityFactory

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(JwtAuthHelper::class)
class EnabledOrganisationsIntegrationTest : IntegrationTestBase() {

  @Autowired
  lateinit var enabledOrganisationRepository: EnabledOrganisationRepository

  @Autowired
  lateinit var enabledOrganisationService: EnabledOrganisationService

  @BeforeEach
  fun setUp() {
    enabledOrganisationRepository.deleteAll()
  }

  @Test
  fun `should return list of enabled organisations`() {
    val code = "ONI"
    val desc = "Onley"

    val enabledOrganisation = EnabledOrganisationEntityFactory().withCode(code).withDescription(desc).produce()
    enabledOrganisationRepository.save(enabledOrganisation)

    val enabledOrganisations = enabledOrganisationService.getEnabledOrganisations()

    enabledOrganisations.isNotEmpty()
    enabledOrganisations[0].code shouldBeEqual code
    enabledOrganisations[0].description shouldBeEqual desc
  }
}
