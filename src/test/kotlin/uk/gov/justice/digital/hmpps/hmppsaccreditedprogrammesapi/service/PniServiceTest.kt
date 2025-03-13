package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import com.fasterxml.jackson.databind.ObjectMapper
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.PNIResultEntityRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.PniRuleRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.PniResultEntityFactory

class PniServiceTest {

  private val oasysService = mockk<OasysService>()
  private val auditService = mockk<AuditService>()
  private val pniNeedsEngine = mockk<PniNeedsEngine>()
  private val pniRiskEngine = mockk<PniRiskEngine>()
  private val pniRuleRepository = mockk<PniRuleRepository>()
  private val pniResultEntityRepository = mockk<PNIResultEntityRepository>()
  private val personService = mockk<PersonService>()
  private val objectMapper = mockk<ObjectMapper>()

  val pniService = PniService(
    oasysService = oasysService,
    auditService = auditService,
    pniNeedsEngine = pniNeedsEngine,
    pniRiskEngine = pniRiskEngine,
    pniRuleRepository = pniRuleRepository,
    pniResultEntityRepository = pniResultEntityRepository,
    personService = personService,
    objectMapper = objectMapper,
  )

  @Test
  fun `fetchAndStoreOasysPni should return mismatch message when results differ`() {
    // Given
    val prisonId = "A1234BC"
    val acpPniResult = "HIGH_INTENSITY_BC"
    val oasysPniResult = "MODERATE_INTENSITY_BC"

    every { pniResultEntityRepository.findAllByPrisonNumber(prisonId) } returns listOf(
      PniResultEntityFactory().withPrisonNumber(prisonId).withProgrammePathway(acpPniResult).produce(),
    )
    every { oasysService.getOasysPniProgrammePathway(prisonId) } returns oasysPniResult

    // When
    val result = pniService.fetchAndStoreOasysPni(prisonId)

    // Then
    result shouldBe "Pni calculation mismatch for prisonNumber $prisonId. ACP: $acpPniResult, OASYS: $oasysPniResult \n"
  }

  @Test
  fun `fetchAndStoreOasysPni should return empty string when results match`() {
    // Given
    val prisonId = "A1234BC"
    val acpPniResult = "HIGH_INTENSITY_BC"

    every { pniResultEntityRepository.findAllByPrisonNumber(prisonId) } returns listOf(
      PniResultEntityFactory().withPrisonNumber(prisonId).withProgrammePathway(acpPniResult).produce(),
    )
    every { oasysService.getOasysPniProgrammePathway(prisonId) } returns acpPniResult

    // When
    val result = pniService.fetchAndStoreOasysPni(prisonId)

    // Then
    result shouldBe ""
  }

  @Test
  fun `fetchAndStoreOasysPni should return error message when exception occurs`() {
    // Given
    val prisonId = "A1234BC"

    every { pniResultEntityRepository.findAllByPrisonNumber(prisonId) } throws RuntimeException("Database error")

    // When
    val result = pniService.fetchAndStoreOasysPni(prisonId)

    // Then
    result shouldContain "Error while fetching PNI for $prisonId"
  }
}
