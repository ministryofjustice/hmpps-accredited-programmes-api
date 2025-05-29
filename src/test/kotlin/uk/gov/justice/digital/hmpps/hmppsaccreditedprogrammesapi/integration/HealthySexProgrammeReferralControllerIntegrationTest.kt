package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration

import io.kotest.matchers.equals.shouldBeEqual
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.config.JwtAuthHelper
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.PRISON_NUMBER_1
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.OverrideType
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.SexualOffenceDetailsRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.HspReferralCreate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.HspReferralDetails
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ReferralCreate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.type.ReferralStatus
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.CourseEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.OfferingEntityFactory
import java.util.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(JwtAuthHelper::class)
class HealthySexProgrammeReferralControllerIntegrationTest : IntegrationTestBase() {

  @Autowired
  lateinit var sexualOffenceDetailsRepository: SexualOffenceDetailsRepository

  @Autowired
  lateinit var referralRepository: ReferralRepository

  @BeforeEach
  fun setUp() {
    persistenceHelper.clearAllTableContent()
  }

  @Test
  fun `should return 404 not found for unknown referral`() {
    // Given
    val referralId = UUID.randomUUID()

    // When
    val response = performRequestAndExpectStatus(
      HttpMethod.GET,
      "/referrals/$referralId/hsp-details",
      object : ParameterizedTypeReference<ErrorResponse>() {},
      HttpStatus.NOT_FOUND.value(),
    )

    // Then
    assertThat(response.developerMessage).isEqualTo("No referral found for referral: $referralId")
  }

  @Test
  fun `should return 200 and HSP details for known HSP referral`() {
    // Given
    persistenceHelper.createOrganisation(code = "NAT", name = "NAT org")

    val courseEntity = CourseEntityFactory().produce()
    persistenceHelper.createCourse(courseEntity)

    val offeringEntity = OfferingEntityFactory().withCourse(courseEntity).withOrganisationId("NAT").produce()
    persistenceHelper.createOffering(offeringEntity)

    val twoSexualOffenceDetails = sexualOffenceDetailsRepository.findAll().take(2)

    val hspReferralCreate = HspReferralCreate(
      offeringEntity.id!!,
      PRISON_NUMBER_1,
      listOf(twoSexualOffenceDetails.first().id!!, twoSexualOffenceDetails.last().id!!),
      "Is definitely eligible for HSP",
    )

    val createdHspReferral = createHSPReferral(hspReferralCreate)

    // When
    val hspReferralDetails = performRequestAndExpectOk(HttpMethod.GET, "/referrals/${createdHspReferral.id}/hsp-details", hspReferralDetailsTypeReference())

    // Then
    assertThat(hspReferralDetails).isNotNull
    assertThat(hspReferralDetails.selectedOffences.size).isEqualTo(23)
    assertThat(hspReferralDetails.selectedOffences.count { it.score == 0 }).isEqualTo(21)
    assertThat(hspReferralDetails.selectedOffences.count { it.score == 1 }).isEqualTo(2)

    val firstSelectedOffence = hspReferralDetails.selectedOffences.find { it.id == twoSexualOffenceDetails.first().id }
    val lastSelectedOffence = hspReferralDetails.selectedOffences.find { it.id == twoSexualOffenceDetails.last().id }
    assertThat(firstSelectedOffence).matches { it?.score == 1 && it.id == twoSexualOffenceDetails.first().id }
    assertThat(lastSelectedOffence).matches { it?.score == 1 && it.id == twoSexualOffenceDetails.last().id }

    assertThat(hspReferralDetails.eligibilityOverrideReason).isEqualTo("Is definitely eligible for HSP")
  }

  @Test
  fun `should return 200 and no selected HSP details for a known non-HSP referral`() {
    // Given
    persistenceHelper.createOrganisation(code = "NAT", name = "NAT org")

    val courseEntity = CourseEntityFactory().produce()
    persistenceHelper.createCourse(courseEntity)

    val offeringEntity = OfferingEntityFactory().withCourse(courseEntity).withOrganisationId("NAT").produce()
    persistenceHelper.createOffering(offeringEntity)

    val referralCreate = ReferralCreate(
      offeringId = offeringEntity.id!!,
      prisonNumber = PRISON_NUMBER_1,
    )
    val referral = createReferral(referralCreate)

    // When
    val hspReferralDetails = performRequestAndExpectOk(HttpMethod.GET, "/referrals/${referral.id}/hsp-details", hspReferralDetailsTypeReference())

    // Then
    assertThat(hspReferralDetails).isNotNull
    assertThat(hspReferralDetails.prisonNumber).isEqualTo(PRISON_NUMBER_1)
    assertThat(hspReferralDetails.eligibilityOverrideReason).isNull()
    assertThat(hspReferralDetails.selectedOffences).size().isEqualTo(23)
    assertThat(hspReferralDetails.selectedOffences.count { it.score == 0 }).isEqualTo(23)
  }

  @Test
  fun `should create an HSP referral with associated HSP offence details`() {
    // Given
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())

    val courseEntity = CourseEntityFactory().produce()
    persistenceHelper.createCourse(courseEntity)

    val offeringEntity = OfferingEntityFactory().withCourse(courseEntity).withOrganisationId("MDI").produce()
    persistenceHelper.createOffering(offeringEntity)

    val sexualOffenceDetailsEntity1 = sexualOffenceDetailsRepository.findAll().find { it.score == 2 }.let { it!! }
    val sexualOffenceDetailsEntity2 = sexualOffenceDetailsRepository.findAll().findLast { it.score == 2 }.let { it!! }

    val hspReferralCreate = HspReferralCreate(
      offeringEntity.id!!,
      PRISON_NUMBER_1,
      listOf(sexualOffenceDetailsEntity1.id!!, sexualOffenceDetailsEntity2.id!!),
      "Is definitely eligible for HSP",
    )

    // When
    val createdHspReferral = createHSPReferral(hspReferralCreate)

    // Then
    createdHspReferral.prisonNumber shouldBeEqual PRISON_NUMBER_1
    val savedReferral = referralRepository.findByIdWithHspDetails(createdHspReferral.id).get()
    savedReferral.status shouldBeEqual ReferralStatus.REFERRAL_STARTED.name
    savedReferral.selectedSexualOffenceDetails.size shouldBeEqual 2

    savedReferral.selectedSexualOffenceDetails.map { it.sexualOffenceDetails?.description } shouldBeEqual listOf(
      sexualOffenceDetailsEntity1.description,
      sexualOffenceDetailsEntity2.description)

    savedReferral.eligibilityOverrideReasons.size shouldBeEqual 1
    savedReferral.eligibilityOverrideReasons.first().reason.shouldBeEqual("Is definitely eligible for HSP")
    savedReferral.eligibilityOverrideReasons.first().overrideType.shouldBeEqual(OverrideType.HEALTHY_SEX_PROGRAMME)
  }

  @Test
  fun `should create an HSP referral when total offence score is below HSP but an override reason has been provided `() {
    // Given
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())

    val courseEntity = CourseEntityFactory().produce()
    persistenceHelper.createCourse(courseEntity)

    val offeringEntity = OfferingEntityFactory().withCourse(courseEntity).withOrganisationId("MDI").produce()
    persistenceHelper.createOffering(offeringEntity)

    val sexualOffenceDetailsEntity = sexualOffenceDetailsRepository.findAll().find { it.score == 1 }.let { it!! }

    val hspReferralCreate = HspReferralCreate(
      offeringEntity.id!!,
      PRISON_NUMBER_1,
      listOf(sexualOffenceDetailsEntity.id!!),
      "Is definitely eligible for HSP",
    )

    // When
    val createdHspReferral = createHSPReferral(hspReferralCreate)

    // Then
    val savedReferral = referralRepository.findByIdWithHspDetails(createdHspReferral.id).get()
    savedReferral.selectedSexualOffenceDetails.first().sexualOffenceDetails?.description?.shouldBeEqual(sexualOffenceDetailsEntity.description)
    savedReferral.eligibilityOverrideReasons.first().reason.shouldBeEqual("Is definitely eligible for HSP")
  }

  @Test
  fun `should return Http BAD REQUEST when creating an HSP referral without selected offence details`() {
    // Given
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())

    val courseEntity = CourseEntityFactory().produce()
    persistenceHelper.createCourse(courseEntity)

    val offeringEntity = OfferingEntityFactory().withCourse(courseEntity).withOrganisationId("MDI").produce()
    persistenceHelper.createOffering(offeringEntity)

    val hspReferralCreate = HspReferralCreate(
      offeringEntity.id!!,
      PRISON_NUMBER_1,
      emptyList(),
    )

    // When & Then
    performRequestAndExpectStatusWithBody(
      HttpMethod.POST,
      "/referral/hsp",
      body = hspReferralCreate,
      expectedResponseStatus = HttpStatus.BAD_REQUEST.value(),
    )
  }

  @Test
  fun `should return Http BAD REQUEST when creating an HSP referral with offence details scoring below HSP threshold`() {
    // Given
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())

    val courseEntity = CourseEntityFactory().produce()
    persistenceHelper.createCourse(courseEntity)

    val offeringEntity = OfferingEntityFactory().withCourse(courseEntity).withOrganisationId("MDI").produce()
    persistenceHelper.createOffering(offeringEntity)

    val sexualOffenceDetailsEntity = sexualOffenceDetailsRepository.findAll().find { it.score == 2 }.let { it!! }

    val hspReferralCreate = HspReferralCreate(
      offeringEntity.id!!,
      PRISON_NUMBER_1,
      listOf(sexualOffenceDetailsEntity.id!!),
      eligibilityOverrideReason = null,
    )

    // When & Then
    performRequestAndExpectStatusWithBody(
      HttpMethod.POST,
      "/referral/hsp",
      body = hspReferralCreate,
      expectedResponseStatus = HttpStatus.BAD_REQUEST.value(),
    )
  }

  fun createHSPReferral(hspReferralCreate: HspReferralCreate) = performRequestAndExpectStatusWithBody(HttpMethod.POST, "/referral/hsp", body = hspReferralCreate, expectedResponseStatus = HttpStatus.CREATED.value(), returnType = referralTypeReference())
  fun createReferral(referralCreate: ReferralCreate) = performRequestAndExpectStatusWithBody(HttpMethod.POST, "/referrals", body = referralCreate, expectedResponseStatus = HttpStatus.CREATED.value(), returnType = referralTypeReference())

  fun hspReferralDetailsTypeReference(): ParameterizedTypeReference<HspReferralDetails> = object : ParameterizedTypeReference<HspReferralDetails>() {}
}
