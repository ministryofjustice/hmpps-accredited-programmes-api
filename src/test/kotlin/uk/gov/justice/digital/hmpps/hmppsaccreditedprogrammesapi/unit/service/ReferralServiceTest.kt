package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.service

import io.kotest.matchers.shouldBe
import io.mockk.MockKAnnotations
import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.security.core.Authentication
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.context.SecurityContextHolder
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonRegisterApi.PrisonRegisterApiService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonRegisterApi.model.PrisonDetails
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonerSearchApi.PrisonerSearchApiService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.CLIENT_USERNAME
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.ORGANISATION_ID_MDI
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.PRISONERS
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.PRISONS
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.PRISON_NUMBER_1
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferrerUserEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.view.ReferralViewRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.OfferingRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.OrganisationRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.PersonRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.ReferrerUserRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.ReferralService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.OfferingEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.OrganisationEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.PersonEntityFactory
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
  private lateinit var prisonerSearchApiService: PrisonerSearchApiService

  @MockK(relaxed = true)
  private lateinit var personRepository: PersonRepository

  @MockK(relaxed = true)
  private lateinit var organisationRepository: OrganisationRepository

  @InjectMockKs
  private lateinit var referralService: ReferralService

  @BeforeEach
  fun setup() {
    MockKAnnotations.init(this)
    every { prisonRegisterApiService.getAllPrisons() } returns PRISONS
    every { prisonerSearchApiService.getPrisoners(any()) } returns PRISONERS
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
    mockSecurityContext(CLIENT_USERNAME)

    val referrer = ReferrerUserEntityFactory()
      .withUsername(CLIENT_USERNAME)
      .produce()
    every { referrerUserRepository.findById(CLIENT_USERNAME) } returns Optional.of(referrer)

    val offering = OfferingEntityFactory()
      .withId(UUID.randomUUID())
      .withOrganisationId(ORGANISATION_ID_MDI)
      .produce()
    every { offeringRepository.findById(any()) } returns Optional.of(offering)

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

    val createdReferralId = referralService.createReferral(PRISON_NUMBER_1, offering.id!!)

    createdReferralId shouldBe referralId

    verify { referrerUserRepository.findById(CLIENT_USERNAME) }
    verify { offeringRepository.findById(offering.id!!) }
    verify {
      referralRepository.save(
        match {
          it.prisonNumber == PRISON_NUMBER_1 &&
            it.referrer.username == CLIENT_USERNAME &&
            it.offering.id == offering.id
        },
      )
    }
  }

  @Test
  fun `createReferral with new organisation and existing user should create referral successfully`() {
    mockSecurityContext(CLIENT_USERNAME)

    val referrer = ReferrerUserEntityFactory()
      .withUsername(CLIENT_USERNAME)
      .produce()
    every { referrerUserRepository.findById(CLIENT_USERNAME) } returns Optional.of(referrer)

    val prisonCode = "XXX"
    val prisonName = "Secret Prison"

    val offering = OfferingEntityFactory()
      .withId(UUID.randomUUID())
      .withOrganisationId(prisonCode)
      .produce()
    every { offeringRepository.findById(any()) } returns Optional.of(offering)

    val person = PersonEntityFactory()
      .produce()
    every { personRepository.findPersonEntityByPrisonNumber(any()) } returns person

    every { personRepository.save(any()) } returns person

    every { organisationRepository.findOrganisationEntityByCode(prisonCode) } returns null

    val prisonDetail = PrisonDetails(prisonCode, prisonName)
    every { prisonRegisterApiService.getPrisonById(prisonCode) } returns prisonDetail

    every { organisationRepository.save(any()) } returns OrganisationEntityFactory().produce()

    val referralId = UUID.randomUUID()
    every { referralRepository.save(any<ReferralEntity>()) } answers {
      firstArg<ReferralEntity>().apply { id = referralId }
    }

    val createdReferralId = referralService.createReferral(PRISON_NUMBER_1, offering.id!!)

    createdReferralId shouldBe referralId

    verify { referrerUserRepository.findById(CLIENT_USERNAME) }
    verify { offeringRepository.findById(offering.id!!) }
    verify {
      referralRepository.save(
        match {
          it.prisonNumber == PRISON_NUMBER_1 &&
            it.referrer.username == CLIENT_USERNAME &&
            it.offering.id == offering.id
        },
      )
    }

    verify {
      organisationRepository.save(
        match {
          it.code == prisonCode &&
            it.name == prisonName
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

    val createdReferralId = referralService.createReferral(PRISON_NUMBER_1, offering.id!!)

    createdReferralId shouldBe referralId

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
  }
}
