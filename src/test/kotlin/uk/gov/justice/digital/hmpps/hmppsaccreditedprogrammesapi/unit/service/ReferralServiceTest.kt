package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.service

import io.kotest.matchers.shouldBe
import io.mockk.CapturingSlot
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Captor
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonRegisterApi.model.Prison
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.ASSESSED_SUITABLE
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.ASSESSED_SUITABLE_COLOUR
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.ASSESSED_SUITABLE_DESCRIPTION
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.ASSESSED_SUITABLE_HINT
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.ON_PROGRAMME
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.ON_PROGRAMME_COLOUR
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.ON_PROGRAMME_DESCRIPTION
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.ON_PROGRAMME_HINT
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.ORGANISATION_ID_MDI
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.PRISON_NUMBER_1
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.REFERRER_USERNAME
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferrerUserEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.ReferralStatusCategoryRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.ReferralStatusReasonRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.ReferralStatusRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.view.ReferralViewRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.OfferingRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.OrganisationRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.PersonRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.ReferrerUserRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ReferralStatusRefData
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ReferralStatusUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.AuditService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.CaseNotesApiService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.EnabledOrganisationService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.FeatureSwitchService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.OrganisationService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.PeopleSearchApiService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.PersonService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.PniService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.PrisonRegisterApiService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.ReferralReferenceDataService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.ReferralService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.ReferralStatusHistoryService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.EnabledOrganisationEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.OfferingEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.OrganisationEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.PersonEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.ReferrerUserEntityFactory
import java.util.Optional
import java.util.UUID

class ReferralServiceTest {

  @MockK(relaxed = true)
  private lateinit var referralRepository: ReferralRepository

  @MockK(relaxed = true)
  private lateinit var referralViewRepository: ReferralViewRepository

  @MockK(relaxed = true)
  private lateinit var referrerUserRepository: ReferrerUserRepository

  @MockK(relaxed = true)
  private lateinit var offeringRepository: OfferingRepository

  @MockK(relaxed = true)
  private lateinit var prisonRegisterApiService: PrisonRegisterApiService

  @MockK(relaxed = true)
  private lateinit var peopleSearchApiService: PeopleSearchApiService

  @MockK(relaxed = true)
  private lateinit var personRepository: PersonRepository

  @MockK(relaxed = true)
  private lateinit var organisationRepository: OrganisationRepository

  @MockK(relaxed = true)
  private lateinit var auditService: AuditService

  @MockK(relaxed = true)
  private lateinit var referralStatusHistoryService: ReferralStatusHistoryService

  @MockK(relaxed = true)
  private lateinit var referralStatusRepository: ReferralStatusRepository

  @MockK(relaxed = true)
  private lateinit var referralStatusCategoryRepository: ReferralStatusCategoryRepository

  @MockK(relaxed = true)
  private lateinit var referralStatusReasonRepository: ReferralStatusReasonRepository

  @MockK(relaxed = true)
  private lateinit var referralReferenceDataService: ReferralReferenceDataService

  @MockK(relaxed = true)
  private lateinit var enabledOrganisationService: EnabledOrganisationService

  @MockK(relaxed = true)
  private lateinit var personService: PersonService

  @MockK(relaxed = true)
  private lateinit var pniService: PniService

  @MockK(relaxed = true)
  private lateinit var featureSwitchService: FeatureSwitchService

  @MockK(relaxed = true)
  private lateinit var caseNotesApiService: CaseNotesApiService

  @MockK(relaxed = true)
  private lateinit var organisationService: OrganisationService

  @Captor
  private lateinit var referralEntityCaptor: CapturingSlot<ReferralEntity>

  @InjectMockKs
  private lateinit var referralService: ReferralService

  @BeforeEach
  fun setup() {
    MockKAnnotations.init(this)
    referralEntityCaptor = slot()
  }

  private fun mockSecurityContext(username: String) {
    val authentication = mockk<Authentication>()
    every { authentication.name } returns username

    val securityContext = mockk<SecurityContext>()
    every { securityContext.authentication } returns authentication

    mockkStatic(SecurityContextHolder::class)
    every { SecurityContextHolder.getContext() } returns securityContext
  }

  @Test
  fun `createReferral with existing user should create referral successfully`() {
    mockSecurityContext(REFERRER_USERNAME)

    val referrer = ReferrerUserEntityFactory()
      .withUsername(REFERRER_USERNAME)
      .produce()
    every { referrerUserRepository.findById(REFERRER_USERNAME) } returns Optional.of(referrer)

    val offering = OfferingEntityFactory()
      .withId(UUID.randomUUID())
      .withOrganisationId(ORGANISATION_ID_MDI)
      .produce()
    every { offeringRepository.findById(any()) } returns Optional.of(offering)

    every { enabledOrganisationService.getEnabledOrganisation(any()) } returns EnabledOrganisationEntityFactory().produce()

    val person = PersonEntityFactory()
      .produce()
    every { personRepository.findPersonEntityByPrisonNumber(any()) } returns person

    every { personRepository.save(any()) } returns person

    val organisation = OrganisationEntityFactory().withCode(ORGANISATION_ID_MDI).produce()
    every { organisationRepository.findOrganisationEntityByCode(ORGANISATION_ID_MDI) } returns organisation

    val referralId = UUID.randomUUID()
    every { referralRepository.save(any<ReferralEntity>()) } answers {
      firstArg<ReferralEntity>().apply { id = referralId }
    }

    val createdReferral = referralService.createReferral(PRISON_NUMBER_1, offering.id!!)

    createdReferral.id shouldBe referralId

    verify { referrerUserRepository.findById(REFERRER_USERNAME) }
    verify { offeringRepository.findById(offering.id!!) }
    verify {
      referralRepository.save(
        match {
          it.prisonNumber == PRISON_NUMBER_1 &&
            it.referrer.username == REFERRER_USERNAME &&
            it.offering.id == offering.id
        },
      )
    }
    verify {
      referralStatusHistoryService.createReferralHistory(
        match {
          it.prisonNumber == PRISON_NUMBER_1
        },
      )
    }
    verify {
      auditService.audit(
        match {
          it.prisonNumber == PRISON_NUMBER_1
        },
      )
    }
  }

  @Test
  fun `createReferral with new organisation and existing user should create referral successfully`() {
    mockSecurityContext(REFERRER_USERNAME)

    val referrer = ReferrerUserEntityFactory()
      .withUsername(REFERRER_USERNAME)
      .produce()
    every { referrerUserRepository.findById(REFERRER_USERNAME) } returns Optional.of(referrer)

    val prisonCode = "XXX"
    val prisonName = "Secret Prison"

    val offering = OfferingEntityFactory()
      .withId(UUID.randomUUID())
      .withOrganisationId(prisonCode)
      .produce()
    every { offeringRepository.findById(any()) } returns Optional.of(offering)
    every { enabledOrganisationService.getEnabledOrganisation(any()) } returns EnabledOrganisationEntityFactory().produce()
    val person = PersonEntityFactory()
      .produce()
    every { personRepository.findPersonEntityByPrisonNumber(any()) } returns person

    every { personRepository.save(any()) } returns person

    every { organisationRepository.findOrganisationEntityByCode(prisonCode) } returns null

    val prisonDetail = Prison(
      prisonId = prisonCode,
      prisonName = prisonName,
      active = false,
      male = false,
      female = true,
      contracted = true,
      types = emptyList(),
      categories = emptySet(),
      addresses = emptyList(),
      operators = emptyList(),
    )
    every { prisonRegisterApiService.getPrisonById(prisonCode) } returns prisonDetail

    every { organisationRepository.save(any()) } returns OrganisationEntityFactory().produce()

    val referralId = UUID.randomUUID()
    every { referralRepository.save(any<ReferralEntity>()) } answers {
      firstArg<ReferralEntity>().apply { id = referralId }
    }

    val createdReferral = referralService.createReferral(PRISON_NUMBER_1, offering.id!!)

    createdReferral.id shouldBe referralId

    verify { referrerUserRepository.findById(REFERRER_USERNAME) }
    verify { offeringRepository.findById(offering.id!!) }
    verify { organisationService.createOrganisationIfNotPresent(prisonCode) }
    verify {
      referralRepository.save(
        match {
          it.prisonNumber == PRISON_NUMBER_1 &&
            it.referrer.username == REFERRER_USERNAME &&
            it.offering.id == offering.id
        },
      )
    }

    verify {
      auditService.audit(
        match {
          it.prisonNumber == PRISON_NUMBER_1
        },
      )
    }
  }

  @Test
  fun `createReferral with nonexistent user should create user and referral successfully`() {
    mockSecurityContext("NONEXISTENT_USER")

    every { referrerUserRepository.findById("NONEXISTENT_USER") } returns Optional.empty()

    every { referrerUserRepository.save(any<ReferrerUserEntity>()) } answers {
      firstArg<ReferrerUserEntity>().apply { username = "NONEXISTENT_USER" }
    }

    val offering = OfferingEntityFactory()
      .withId(UUID.randomUUID())
      .produce()
    every { offeringRepository.findById(any()) } returns Optional.of(offering)

    val referralId = UUID.randomUUID()
    every { referralRepository.save(any<ReferralEntity>()) } answers {
      firstArg<ReferralEntity>().apply { id = referralId }
    }

    val person = PersonEntityFactory()
      .produce()
    every { personRepository.findPersonEntityByPrisonNumber(any()) } returns person

    every { personRepository.save(any()) } returns person

    val createdReferral = referralService.createReferral(PRISON_NUMBER_1, offering.id!!)

    createdReferral.id shouldBe referralId

    verify {
      referrerUserRepository.save(
        match {
          it.username == "NONEXISTENT_USER"
        },
      )
    }

    verify { offeringRepository.findById(offering.id!!) }
    verify {
      referralRepository.save(
        match {
          it.prisonNumber == PRISON_NUMBER_1 &&
            it.referrer.username == "NONEXISTENT_USER" &&
            it.offering.id == offering.id
        },
      )
    }

    verify {
      auditService.audit(
        match {
          it.prisonNumber == PRISON_NUMBER_1
        },
      )
    }
  }

  @Test
  fun `should persist PNI when updating referral status to On Programme`() {
    // Given
    mockSecurityContext(REFERRER_USERNAME)
    val referralId = UUID.randomUUID()
    val referral = ReferralEntityFactory()
      .withId(referralId)
      .withStatus("ASSESSED_SUITABLE")
      .produce()

    every { referralRepository.getReferenceById(any()) } returns referral

    val referralStatusList =
      mutableListOf<ReferralStatusRefData>(
        ReferralStatusRefData(
          code = ON_PROGRAMME,
          description = ON_PROGRAMME_DESCRIPTION,
          colour = ON_PROGRAMME_COLOUR,
          hintText = ON_PROGRAMME_HINT,
          hasNotes = true,
          hasConfirmation = false,
          closed = true,
          draft = false,
          hold = false,
          release = false,
          deselectAndKeepOpen = false,
          notesOptional = false,
        ),
      )
    every { referralReferenceDataService.getNextStatusTransitions("ASSESSED_SUITABLE") } returns referralStatusList

    // When
    val referralStatusUpdate = ReferralStatusUpdate(status = ON_PROGRAMME)
    referralService.updateReferralStatusById(referralId, referralStatusUpdate)

    // Then
    verify { pniService.savePni(referral.prisonNumber, gender = null, savePni = true, referral.id) }
    verify { auditService.audit(capture(referralEntityCaptor), any(), any()) }

    val capturedReferralEntity = referralEntityCaptor.captured
    assertThat(capturedReferralEntity.status).isEqualTo(ON_PROGRAMME)
  }

  @Test
  fun `should NOT persist PNI when updating referral status for special deselected case`() {
    // Given
    mockSecurityContext(REFERRER_USERNAME)
    val referralId = UUID.randomUUID()
    val referral = ReferralEntityFactory()
      .withId(referralId)
      .withStatus(ON_PROGRAMME)
      .produce()

    every { referralRepository.getReferenceById(any()) } returns referral

    val referralStatusList =
      mutableListOf<ReferralStatusRefData>(
        ReferralStatusRefData(
          code = ASSESSED_SUITABLE,
          description = ASSESSED_SUITABLE_DESCRIPTION,
          colour = ASSESSED_SUITABLE_COLOUR,
          hintText = ASSESSED_SUITABLE_HINT,
          hasNotes = true,
          hasConfirmation = false,
          closed = true,
          draft = false,
          hold = false,
          release = false,
          deselectAndKeepOpen = false,
          notesOptional = false,
        ),
      )
    every { referralReferenceDataService.getNextStatusTransitions(ON_PROGRAMME, true) } returns referralStatusList

    // When
    val referralStatusUpdate = ReferralStatusUpdate(status = "ASSESSED_SUITABLE", ptUser = true)
    referralService.updateReferralStatusById(referralId, referralStatusUpdate)

    // Then
    verify(exactly = 0) { pniService.savePni(referral.prisonNumber, gender = null, savePni = true, referral.id) }
  }
}
