package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import io.mockk.mockkStatic
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.ReferralStatusReasonEntity
import org.mockito.Mock
import org.mockito.Mockito.mock
import org.mockito.MockitoAnnotations
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.caseNotesApi.CaseNotesApiClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.COURSE_AUDIENCE
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.COURSE_INTENSITY
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.COURSE_NAME
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.REFERRER_USERNAME
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.ReferralStatusReasonRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.ReferralStatusRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ReferralStatusUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.CourseEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.OfferingEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.OrganisationEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.PersonEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.ReferrerUserEntityFactory
import java.util.UUID

class CaseNotesApiServiceTest {

    @Mock
    private lateinit var organisationService: OrganisationService

    @Mock
    private lateinit var referralStatusReasonRepository: ReferralStatusReasonRepository

    @Mock
    private lateinit var referralStatusRepository: ReferralStatusRepository

    @Mock
    private lateinit var manageUsersService: ManageUsersService

    private lateinit var caseNotesApiService: CaseNotesApiService

    @BeforeEach
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        caseNotesApiService = CaseNotesApiService(
            caseNotesApiClient = mock(CaseNotesApiClient::class.java),
            featureSwitchService = mock(FeatureSwitchService::class.java),
            personService = mock(PersonService::class.java),
            organisationService = organisationService,
            referralStatusRepository = referralStatusRepository,
            referralStatusReasonRepository = referralStatusReasonRepository,
            manageUsersService = manageUsersService,
        )
        every { caseNotesApiService.getFullName() } returns "Test User"
    }

    @Test
    fun `should build case note message successfully`() {
        // Given
        mockSecurityContext()

        val person = PersonEntityFactory()
            .withForename("John")
            .withSurname("Doe")
            .withPrisonNumber("A1234BC").produce()

        val referralEntity = ReferralEntity(
            id = UUID.randomUUID(),
            prisonNumber = "A1234BC",
            offering = OfferingEntityFactory()
                .withCourse(CourseEntityFactory()
                    .withName(COURSE_NAME)
                    .withAudience(COURSE_AUDIENCE)
                    .withIntensity(COURSE_INTENSITY)
                    .produce())
                .produce(),
            referrer = (ReferrerUserEntityFactory()).produce(),
        )

        whenever(organisationService.findOrganisationEntityByCode(any())).thenReturn(OrganisationEntityFactory().withName("Org name").produce())

        val referralStatusUpdate = ReferralStatusUpdate(
            status = "REFERRAL_SUBMITTED",
            notes = "Some additional notes",
        )
        val statusMessageTemplate = "The referral for PRISONER_NAME has been moved from PGM_NAME_STRAND to BC_STRAND."

        // When
        val caseNoteMessage = caseNotesApiService.buildCaseNoteMessage(
            person = person,
            referral = referralEntity,
            referralStatusUpdate = referralStatusUpdate,
            message = statusMessageTemplate,
        )

        // Then
        assertThat(caseNoteMessage).contains("Referral to Super course: Super audience strand at Org name")
        assertThat(caseNoteMessage).contains("The referral for John Doe has been moved from Super course: Super audience to Building Choices: high intensity.")
        assertThat(caseNoteMessage).contains("Details: Some additional notes")
        assertThat(caseNoteMessage).contains("Updated by: TEST_REFERRER_USER_1")
    }

    private fun mockSecurityContext() {
        val authentication = mockk<Authentication>()
        every { authentication.name } returns REFERRER_USERNAME

        val securityContext = mockk<SecurityContext>()
        every { securityContext.authentication } returns authentication

        mockkStatic(SecurityContextHolder::class)
        every { SecurityContextHolder.getContext() } returns securityContext
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