package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.ReferralStatusReasonEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.ReferralStatusReasonRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ReferralStatusUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.CourseEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.OfferingEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.OrganisationEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.PersonEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.ReferralEntityFactory
import java.util.*

class CaseNotesApiServiceTest {

  private val organisationService: OrganisationService = mockk()
  private val referralStatusReasonRepository: ReferralStatusReasonRepository = mockk()
  private val caseNotesApiService: CaseNotesApiService = CaseNotesApiService(
    mockk(),
    mockk(),
    mockk(),
    organisationService,
    mockk(),
    referralStatusReasonRepository,
    mockk(),
  )

  @BeforeEach
  fun setup() {
    every { caseNotesApiService.getFullName() } returns "Test User"
  }

  @Test
  fun `should build case note message correctly`() {
    val person = PersonEntityFactory().withForename("John").withSurname("Doe").withPrisonNumber("A1234BC").produce()

    val course =
      CourseEntityFactory().withId(UUID.randomUUID()).withName("Thinking Skills")
        .withAudience("General violence offence").produce()

    val offering = OfferingEntityFactory().withOrganisationId("ORG123").produce()
    offering.course = course

    val referral = ReferralEntityFactory().produce()
    referral.offering = offering

    val referralStatusUpdate = ReferralStatusUpdate("REFERRAL_SUBMITTED", "")

    every { organisationService.findOrganisationEntityByCode("ORG123") } returns OrganisationEntityFactory().withName("Test Org")
      .produce()

    every { referralStatusReasonRepository.findByCode("REFERRAL_SUBMITTED") } returns ReferralStatusReasonEntity(
      code = "REFERRAL_SUBMITTED",
      "Referral has been submitted",
      "",
      true,
      true,
    )

    // Expected output
    val expectedMessage = """
Referral to Thinking Skills: General violence offence strand at Test Org 

John Doe has been referred to Thinking Skills: General violence offence.

Updated by: Test User

    """.trimIndent()

    // Call function
    val result = caseNotesApiService.buildCaseNoteMessage(
      person,
      referral,
      referralStatusUpdate,
      "PRISONER_NAME has been referred to PGM_NAME_STRAND.",
    )

    // Assert
    assertEquals(expectedMessage, result)
  }

  @Test
  fun `should handle missing reason for closing referral`() {
    val person = PersonEntityFactory().withForename("John").withSurname("Doe").withPrisonNumber("A1234BC").produce()

    val course =
      CourseEntityFactory().withId(UUID.randomUUID()).withName("Referral to Anger Management")
        .withAudience("Emotional Regulation strand at Another Org").produce()

    val offering = OfferingEntityFactory().withOrganisationId("ORG123").produce()
    offering.course = course

    val referral = ReferralEntityFactory().withStatus("WITHDRAWN").produce()
    referral.offering = offering

    val referralStatusUpdate = ReferralStatusUpdate("WITHDRAWN", "D_WITHDRAWN")

    every { organisationService.findOrganisationEntityByCode("ORG123") } returns OrganisationEntityFactory().withName("Test Org")
      .produce()
    val expectedMessage = """
Referral to Referral to Anger Management: Emotional Regulation strand at Another Org strand at Test Org 

John Doe cannot continue the programme. The referral will be closed.

Reason for closing referral: Other

Updated by: Test User

    """.trimIndent()

    val result = caseNotesApiService.buildCaseNoteMessage(
      person,
      referral,
      referralStatusUpdate,
      "PRISONER_NAME cannot continue the programme. The referral will be closed.",
    )

    assertEquals(expectedMessage, result)
  }
}
