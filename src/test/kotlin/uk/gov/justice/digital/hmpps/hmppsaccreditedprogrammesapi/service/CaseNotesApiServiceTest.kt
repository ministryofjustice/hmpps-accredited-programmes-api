package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
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
}