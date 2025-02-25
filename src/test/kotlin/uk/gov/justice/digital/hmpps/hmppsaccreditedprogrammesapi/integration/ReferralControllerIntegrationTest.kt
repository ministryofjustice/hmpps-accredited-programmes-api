package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration

import com.github.tomakehurst.wiremock.client.WireMock
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainAnyOf
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.optional.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.assertj.core.api.Assertions.assertThat
import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilCallTo
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.expectBody
import org.springframework.web.util.UriComponentsBuilder
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonerSearchApi.model.Prisoner
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.ResourceLoader
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.config.JwtAuthHelper
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.COURSE_ID
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.COURSE_NAME
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.ON_HOLD_REFERRAL_SUBMITTED
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.ON_HOLD_REFERRAL_SUBMITTED_COLOUR
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.ON_HOLD_REFERRAL_SUBMITTED_DESCRIPTION
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.ON_HOLD_REFERRAL_SUBMITTED_HINT
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.ORGANISATION_ID_MDI
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.PRISONER_1
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.PRISON_NUMBER_1
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.REFERRAL_STARTED
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.REFERRAL_STARTED_COLOUR
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.REFERRAL_STARTED_DESCRIPTION
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.REFERRAL_SUBMITTED
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.REFERRAL_SUBMITTED_COLOUR
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.REFERRAL_SUBMITTED_DESCRIPTION
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.REFERRAL_WITHDRAWN
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.REFERRAL_WITHDRAWN_ALT_DESCRIPTION
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.REFERRAL_WITHDRAWN_COLOUR
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.REFERRAL_WITHDRAWN_HINT
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.REFERRER_USERNAME
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.AccountType
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.AuditAction
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseSetting
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseStatus
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralStatusHistoryRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.AuditRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.CourseParticipationRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.OfferingRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.PNIResultEntityRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.PersonRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.StaffRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ConfirmationFields
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.CourseIntensity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.PaginatedReferralView
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Referral
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ReferralCreate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ReferralStatusRefData
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ReferralStatusUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ReferralUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ReferralView
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.StaffDetail
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.HmppsSubjectAccessRequestContent
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.type.ReferralStatus
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.CourseParticipationEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.CourseParticipationOutcomeFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.CourseParticipationSettingFactory
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
import java.time.Year
import java.util.*

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(JwtAuthHelper::class)
class ReferralControllerIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var offeringRepository: OfferingRepository

  @Autowired
  lateinit var personRepository: PersonRepository

  @Autowired
  lateinit var auditRepository: AuditRepository

  @Autowired
  lateinit var referralStatusHistoryRepository: ReferralStatusHistoryRepository

  @Autowired
  lateinit var referralRepository: ReferralRepository

  @Autowired
  lateinit var pniResultRepository: PNIResultEntityRepository

  @Autowired
  lateinit var staffRepository: StaffRepository

  @Autowired
  lateinit var courseParticipationRepository: CourseParticipationRepository

  @BeforeEach
  fun setUp() {
    persistenceHelper.clearAllTableContent()

    persistenceHelper.createCourse(
      COURSE_ID,
      "SC",
      COURSE_NAME,
      "Sample description",
      "SC++",
      "General offence",
    )
    persistenceHelper.createOrganisation(code = "BWN", name = "BWN org")
    persistenceHelper.createEnabledOrganisation("BWN", "BWN org")
    persistenceHelper.createOrganisation(code = "MDI", name = "MDI org")
    persistenceHelper.createEnabledOrganisation("MDI", "MDI org")

    persistenceHelper.createOffering(
      UUID.fromString("7fffcc6a-11f8-4713-be35-cf5ff1aee517"),
      COURSE_ID,
      "MDI",
      "nobody-mdi@digital.justice.gov.uk",
      "nobody2-mdi@digital.justice.gov.uk",
      true,
    )
    persistenceHelper.createOffering(
      UUID.fromString("790a2dfe-7de5-4504-bb9c-83e6e53a6537"),
      UUID.fromString("d3abc217-75ee-46e9-a010-368f30282367"),
      "BWN",
      "nobody-bwn@digital.justice.gov.uk",
      "nobody2-bwn@digital.justice.gov.uk",
      true,
    )

    persistenceHelper.createCourse(
      UUID.fromString("28e47d30-30bf-4dab-a8eb-9fda3f6400e8"),
      "CC",
      "Custom Course",
      "Sample description",
      "CC",
      "General offence",
      intensity = CourseIntensity.MODERATE.name,
    )
    persistenceHelper.createCourse(
      UUID.fromString("1811faa6-d568-4fc4-83ce-41118b90242e"),
      "RC",
      "RAPID Course",
      "Sample description",
      "RC",
      "General offence",
      intensity = CourseIntensity.HIGH.name,
    )
    persistenceHelper.createOffering(
      UUID.randomUUID(),
      UUID.fromString("1811faa6-d568-4fc4-83ce-41118b90242e"),
      "WSI",
      "nobody-bwn@digital.justice.gov.uk",
      "nobody2-bwn@digital.justice.gov.uk",
      true,
    )
  }

  @Test
  fun `should transfer existing referral to appropriate building choices course`() {
    // Given
    val buildingChoicesCourseId = UUID.randomUUID()
    persistenceHelper.createBuildingChoicesCourses(variantCourseId = buildingChoicesCourseId)
    val course = getAllCourses().first { it.identifier == "RC" }
    val offering = getAllOfferingsForCourse(course.id).first()
    val createdReferral = createReferral(offering.id, PRISON_NUMBER_1, null)

    val referralStatusUpdate = ReferralStatusUpdate(
      status = ReferralStatus.REFERRAL_SUBMITTED.name,
      ptUser = true,
    )
    updateReferralStatus(createdReferral.id, referralStatusUpdate)

    // When
    val newReferral = transferReferralToBuildingChoices(createdReferral.id, buildingChoicesCourseId)

    // Then
    val originalReferral = referralRepository.findById(createdReferral.id).get()
    originalReferral.status shouldBeEqual ReferralStatus.MOVED_TO_BUILDING_CHOICES.name

    val updatedNewReferral = referralRepository.findById(newReferral.id).get()
    updatedNewReferral.status shouldBeEqual ReferralStatus.REFERRAL_SUBMITTED.name
    updatedNewReferral.originalReferralId!! shouldBeEqual createdReferral.id

    val buildingChoicesOffering = offeringRepository.findById(newReferral.offeringId)
    buildingChoicesOffering.get().course.name shouldBeEqual "Building Choices: high intensity"
  }

  @Test
  fun `should return HTTP 500 when attempting to transfer an existing referral to building choices course and no matching building choices courses can be found`() {
    // Given
    val course = getAllCourses().first()
    val offering = getAllOfferingsForCourse(course.id).first()
    val createdReferral = createReferral(offering.id, PRISON_NUMBER_1, null)

    val referralStatusUpdate = ReferralStatusUpdate(
      status = ReferralStatus.REFERRAL_SUBMITTED.name,
      ptUser = true,
    )
    updateReferralStatus(createdReferral.id, referralStatusUpdate)

    // When
    val unknownCourseId = UUID.randomUUID()
    val errorResponse = performRequestAndExpectStatus(
      HttpMethod.POST,
      "/referrals/${createdReferral.id}/transfer-to-building-choices/$unknownCourseId",
      object : ParameterizedTypeReference<ErrorResponse>() {},
      HttpStatus.INTERNAL_SERVER_ERROR.value(),
    )

    // Then
    val originalReferral = referralRepository.findById(createdReferral.id).get()
    originalReferral.status shouldBeEqual ReferralStatus.REFERRAL_SUBMITTED.name

    errorResponse.status shouldBeEqual 500
    errorResponse.developerMessage?.shouldBeEqual("Unable to find building choices offering for course: $unknownCourseId and organisation: MDI")
  }

  @Test
  fun `should return NOT FOUND when attempting to transfer an non existent referral to building choices`() {
    // Given
    val referralId = UUID.randomUUID()
    val courseId = UUID.randomUUID()

    // When
    val errorResponse = performRequestAndExpectStatus(
      HttpMethod.POST,
      "/referrals/$referralId/transfer-to-building-choices/$courseId",
      object : ParameterizedTypeReference<ErrorResponse>() {},
      HttpStatus.NOT_FOUND.value(),
    )

    // Then
    errorResponse.status shouldBeEqual 404
    errorResponse.developerMessage?.shouldBeEqual("No referral found at /referrals/$referralId/transfer-to-building-choices/$courseId")
  }

  @Test
  fun `Creating a referral with an existing user should return 201 with correct body`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())

    val course = getAllCourses().first()
    val offering = getAllOfferingsForCourse(course.id).first()
    val originalReferralId = UUID.randomUUID()
    val referralCreated = createReferral(offering.id!!, PRISON_NUMBER_1, originalReferralId)

    val personEntity = personRepository.findPersonEntityByPrisonNumber(PRISON_NUMBER_1)

    personEntity.shouldNotBeNull()
    personEntity.surname.shouldBeEqual(PRISONER_1.lastName)
    personEntity.forename.shouldBeEqual(PRISONER_1.firstName)
    personEntity.location.shouldNotBeNull()

    referralCreated.id.shouldNotBeNull()

    getReferralById(referralCreated.id) shouldBeEqual Referral(
      id = referralCreated.id,
      offeringId = offering.id!!,
      referrerUsername = REFERRER_USERNAME,
      prisonNumber = PRISON_NUMBER_1,
      status = REFERRAL_STARTED.lowercase(),
      statusDescription = REFERRAL_STARTED_DESCRIPTION,
      statusColour = REFERRAL_STARTED_COLOUR,
      closed = false,
      additionalInformation = null,
      oasysConfirmed = false,
      hasReviewedProgrammeHistory = false,
      submittedOn = null,
      primaryPrisonOffenderManager = null,
      overrideReason = null,
      transferReason = null,
      originalReferralId = originalReferralId,
    )

    val auditEntity = auditRepository.findAll()
      .firstOrNull { it.prisonNumber == PRISON_NUMBER_1 && it.referralId == referralCreated.id }

    auditEntity shouldNotBe null

    // check the referral status is as expected
    val referralHistories =
      referralStatusHistoryRepository.getAllByReferralIdOrderByStatusStartDateDesc(referralCreated.id)
    referralHistories.size shouldBeGreaterThan 0

    val referralHistory = referralHistories[0]
    referralHistory.status.code shouldBeEqual "REFERRAL_STARTED"
  }

  @Test
  fun `Creating a referral which already exists results in conflict 409 response`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())

    val course = getAllCourses().first()
    val offering = getAllOfferingsForCourse(course.id).first()
    // only creates draft referral
    val referralCreated = createReferral(offeringId = offering.id!!, prisonNumber = PRISON_NUMBER_1)
    // submits a referral
    updateReferral(
      referralCreated.id,
      ReferralUpdate(
        oasysConfirmed = true,
        hasReviewedProgrammeHistory = true,
        additionalInformation = "test",
        overrideReason = "Scored higher in OSP, should go onto Kaizen",
      ),
    )
    submitReferral(referralCreated.id)

    val staffEntity = staffRepository.findAll()
    staffEntity.shouldNotBeEmpty()

    createDuplicateReferralResultsInConflict(offering.id, PRISON_NUMBER_1)
  }

  @Test
  fun `Submitting a referral and fetching it returns staff details as part of the referral`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())

    val course = getAllCourses().first()
    val offering = getAllOfferingsForCourse(course.id).first()
    // only creates draft referral
    val referralCreated = createReferral(offering.id!!, PRISON_NUMBER_1)
    // submits a referral
    updateReferral(
      referralCreated.id,
      ReferralUpdate(
        oasysConfirmed = true,
        hasReviewedProgrammeHistory = true,
        additionalInformation = "test",
      ),
    )
    val submitReferral = submitReferral(referralCreated.id)

    val referralById = getReferralById(submitReferral.id)

    referralById.id shouldBe submitReferral.id
    referralById.primaryPrisonOffenderManager shouldBe StaffDetail(
      staffId = "487505".toBigInteger(),
      firstName = "John",
      lastName = "Smith",
      primaryEmail = "john.smith@digital.justice.gov.uk",
      username = "JSMITH_ADM",
      accountType = AccountType.ADMIN,
    )
  }

  @Test
  @WithMockUser(username = "NONEXISTENT_USER")
  fun `Creating a referral with a nonexistent user should return 201 with correct body`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())
    val course = getAllCourses().first()
    val offering = getAllOfferingsForCourse(course.id).first()
    val referralCreated = createReferral(offering.id!!)

    referralCreated.id.shouldNotBeNull()

    getReferralById(referralCreated.id) shouldBeEqual Referral(
      id = referralCreated.id,
      offeringId = offering.id!!,
      referrerUsername = "NONEXISTENT_USER",
      prisonNumber = PRISON_NUMBER_1,
      status = REFERRAL_STARTED.lowercase(),
      statusDescription = REFERRAL_STARTED_DESCRIPTION,
      statusColour = REFERRAL_STARTED_COLOUR,
      closed = false,
      additionalInformation = null,
      oasysConfirmed = false,
      hasReviewedProgrammeHistory = false,
      submittedOn = null,
      overrideReason = null,
    )

    val auditEntity = auditRepository.findAll()
      .firstOrNull { it.prisonNumber == PRISON_NUMBER_1 && it.referralId == referralCreated.id }

    auditEntity shouldNotBe null
  }

  @Test
  fun `Updating a referral with a valid payload should return 204 with no body`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())
    val course = getAllCourses().first()
    val offering = getAllOfferingsForCourse(course.id).first()
    val referralCreated = createReferral(offering.id!!)

    val referralUpdate = ReferralUpdate(
      additionalInformation = "Additional information",
      oasysConfirmed = true,
      hasReviewedProgrammeHistory = true,
      overrideReason = "Override reason",
      transferReason = "Transfer reason",
    )

    updateReferral(referralCreated.id, referralUpdate)

    val referralById = getReferralById(referralCreated.id)

    referralById shouldBeEqual Referral(
      id = referralCreated.id,
      offeringId = offering.id!!,
      referrerUsername = REFERRER_USERNAME,
      prisonNumber = PRISON_NUMBER_1,
      status = REFERRAL_STARTED.lowercase(),
      statusDescription = REFERRAL_STARTED_DESCRIPTION,
      statusColour = REFERRAL_STARTED_COLOUR,
      closed = false,
      additionalInformation = "Additional information",
      oasysConfirmed = true,
      hasReviewedProgrammeHistory = true,
      submittedOn = null,
      overrideReason = "Override reason",
      transferReason = "Transfer reason",
    )
  }

  @Test
  fun `Updating a nonexistent referral should return 404 with error body`() {
    webTestClient
      .put()
      .uri("/referrals/${UUID.randomUUID()}")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(
        ReferralUpdate(
          additionalInformation = "Additional information",
          oasysConfirmed = true,
          hasReviewedProgrammeHistory = true,
          overrideReason = "Override reason",
        ),
      )
      .exchange().expectStatus().isNotFound
  }

  @Test
  fun `Updating a referral status should return 204 with no body`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())

    val courseId = UUID.randomUUID()
    val courseName = getRandomString(10)
    val audience = getRandomString(10)
    val offeringId = UUID.randomUUID()

    persistenceHelper.createOrganisation(code = "XXX", name = "XXX org")
    persistenceHelper.createEnabledOrganisation("XXX", "XXX org")

    createCourse(
      courseId = courseId,
      identifier = getRandomString(4),
      courseName = courseName,
      description = getRandomString(100),
      audience = audience,
    )

    createOffering(
      courseId = courseId,
      offeringId = offeringId,
      orgId = "XXX",
    )

    val referralCreated = createReferral(offeringId)

    val referralStatusUpdate = ReferralStatusUpdate(
      status = REFERRAL_SUBMITTED,
    )

    updateReferralStatus(referralCreated.id, referralStatusUpdate)

    getReferralById(referralCreated.id) shouldBeEqual Referral(
      id = referralCreated.id,
      offeringId = offeringId!!,
      referrerUsername = REFERRER_USERNAME,
      prisonNumber = PRISON_NUMBER_1,
      status = REFERRAL_SUBMITTED.lowercase(),
      closed = false,
      statusDescription = REFERRAL_SUBMITTED_DESCRIPTION,
      statusColour = REFERRAL_SUBMITTED_COLOUR,
      oasysConfirmed = false,
      additionalInformation = null,
      submittedOn = null,
      overrideReason = null,
    )
  }

  @Test
  fun `should NOT allow referral status updates to status of MOVED_TO_BUILDING_CHOICES`() {
    // Given
    val createdReferral = createReferral(PRISON_NUMBER_1)

    val referralStatusUpdate1 = ReferralStatusUpdate(
      status = ReferralStatus.REFERRAL_SUBMITTED.name,
      ptUser = true,
    )
    updateReferralStatus(createdReferral.id, referralStatusUpdate1)

    val referralStatusUpdate2 = ReferralStatusUpdate(
      status = ReferralStatus.MOVED_TO_BUILDING_CHOICES.name,
      ptUser = true,
    )

    // When
    webTestClient
      .put()
      .uri("/referrals/${createdReferral.id}/status")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(referralStatusUpdate2)
      .exchange().expectStatus().isBadRequest

    // Then
    referralRepository.findById(createdReferral.id).get().status shouldBeEqual ReferralStatus.REFERRAL_SUBMITTED.name
  }

  @Test
  fun `Updating a referral status to PROGRAMME_COMPLETE should create a course participation record`() {
    // Given
    val createdReferral = createReferral(PRISON_NUMBER_1)

    updateReferralStatusToOnProgramme(createdReferral)

    // When
    val referralStatusUpdate6 = ReferralStatusUpdate(
      status = ReferralStatus.PROGRAMME_COMPLETE.name,
      ptUser = true,
    )
    updateReferralStatus(createdReferral.id, referralStatusUpdate6)

    // Then
    val courseParticipationList = courseParticipationRepository.findByReferralId(createdReferral.id)
    assertThat(courseParticipationList).hasSize(1)
    val courseParticipation = courseParticipationList[0]
    assertThat(courseParticipation.prisonNumber).isEqualTo(PRISON_NUMBER_1)
    assertThat(courseParticipation.courseName).isEqualTo(COURSE_NAME)
    assertThat(courseParticipation.courseId).isEqualTo(COURSE_ID)
    assertThat(courseParticipation.referralId).isEqualTo(createdReferral.id)
    assertThat(courseParticipation.outcome?.status).isEqualTo(CourseStatus.COMPLETE)
    assertThat(courseParticipation.outcome?.yearCompleted).isEqualTo(Year.now())
    assertThat(courseParticipation.setting?.type).isEqualTo(CourseSetting.CUSTODY)
    assertThat(courseParticipation.setting?.location).isEqualTo("MDI org")
  }

  @Test
  fun `Updating a referral status to PROGRAMME_COMPLETE should update an existing course participation record with the same course name and prison number`() {
    // Given
    val createdReferral = createReferral(PRISON_NUMBER_1)

    val existCourseParticipation = CourseParticipationEntityFactory()
      .withCourseName(COURSE_NAME)
      .withPrisonNumber(PRISON_NUMBER_1)
      .withSetting(CourseParticipationSettingFactory().withType(CourseSetting.CUSTODY).withLocation("Location").produce())
      .withOutcome(CourseParticipationOutcomeFactory().withStatus(CourseStatus.COMPLETE).withYearStarted(Year.of(2018)).produce())
      .withCreatedDateTime(LocalDateTime.now())
      .produce()
    courseParticipationRepository.save(existCourseParticipation)

    updateReferralStatusToOnProgramme(createdReferral)

    // When
    val referralStatusUpdate6 = ReferralStatusUpdate(
      status = ReferralStatus.PROGRAMME_COMPLETE.name,
      ptUser = true,
    )
    updateReferralStatus(createdReferral.id, referralStatusUpdate6)

    // Then
    val courseParticipationList = courseParticipationRepository.findByPrisonNumber(PRISON_NUMBER_1)
    assertThat(courseParticipationList).hasSize(1)
    val courseParticipation = courseParticipationList[0]
    assertThat(courseParticipation.prisonNumber).isEqualTo(PRISON_NUMBER_1)
    assertThat(courseParticipation.courseName).isEqualTo(COURSE_NAME)
    assertThat(courseParticipation.courseId).isEqualTo(COURSE_ID)
    assertThat(courseParticipation.referralId).isEqualTo(createdReferral.id)
    assertThat(courseParticipation.outcome?.status).isEqualTo(CourseStatus.COMPLETE)
    assertThat(courseParticipation.outcome?.yearStarted).isEqualTo(Year.of(2018))
    assertThat(courseParticipation.outcome?.yearCompleted).isEqualTo(Year.now())
    assertThat(courseParticipation.setting?.type).isEqualTo(CourseSetting.CUSTODY)
    assertThat(courseParticipation.setting?.location).isEqualTo("MDI org")
  }

  @Test
  fun `Updating a referral status to DESELECTED should create a course participation record`() {
    // Given
    val createdReferral = createReferral(PRISON_NUMBER_1)

    updateReferralStatusToOnProgramme(createdReferral)

    // When
    val referralStatusUpdate6 = ReferralStatusUpdate(
      status = ReferralStatus.DESELECTED.name,
      ptUser = true,
    )
    updateReferralStatus(createdReferral.id, referralStatusUpdate6)

    // Then
    val courseParticipationList = courseParticipationRepository.findByReferralId(createdReferral.id)
    assertThat(courseParticipationList).hasSize(1)
    val courseParticipation = courseParticipationList[0]
    assertThat(courseParticipation.prisonNumber).isEqualTo(PRISON_NUMBER_1)
    assertThat(courseParticipation.courseName).isEqualTo(COURSE_NAME)
    assertThat(courseParticipation.outcome?.status).isEqualTo(CourseStatus.INCOMPLETE)
    assertThat(courseParticipation.outcome?.yearCompleted).isNull()
  }

  private fun updateReferralStatusToOnProgramme(createdReferral: Referral) {
    val referralStatusUpdate1 = ReferralStatusUpdate(
      status = ReferralStatus.REFERRAL_SUBMITTED.name,
      ptUser = true,
    )
    updateReferralStatus(createdReferral.id, referralStatusUpdate1)

    val referralStatusUpdate2 = ReferralStatusUpdate(
      status = ReferralStatus.AWAITING_ASSESSMENT.name,
      ptUser = true,
    )
    updateReferralStatus(createdReferral.id, referralStatusUpdate2)

    val referralStatusUpdate3 = ReferralStatusUpdate(
      status = ReferralStatus.ASSESSMENT_STARTED.name,
      ptUser = true,
    )
    updateReferralStatus(createdReferral.id, referralStatusUpdate3)

    val referralStatusUpdate4 = ReferralStatusUpdate(
      status = ReferralStatus.ASSESSED_SUITABLE.name,
      ptUser = true,
    )
    updateReferralStatus(createdReferral.id, referralStatusUpdate4)

    val referralStatusUpdate5 = ReferralStatusUpdate(
      status = ReferralStatus.ON_PROGRAMME.name,
      ptUser = true,
    )
    updateReferralStatus(createdReferral.id, referralStatusUpdate5)
  }

  @Test
  fun `Updating a referral status should insert a status history record`() {
    val referralCreated = createReferral(PRISON_NUMBER_1)

    val referralStatusUpdate1 = ReferralStatusUpdate(
      status = REFERRAL_SUBMITTED,
      ptUser = true,
    )
    updateReferralStatus(referralCreated.id, referralStatusUpdate1)

    val statusHistory1 =
      referralStatusHistoryRepository.getAllByReferralIdOrderByStatusStartDateDesc(referralCreated.id)
    statusHistory1.size shouldBeEqual 2
    statusHistory1[0].status.code shouldBeEqual REFERRAL_SUBMITTED
    statusHistory1[1].status.code shouldBeEqual REFERRAL_STARTED

    val referralStatusUpdate2 = ReferralStatusUpdate(
      status = REFERRAL_WITHDRAWN,
      category = "W_ADMIN",
      reason = "W_DUPLICATE",
      ptUser = true,
    )
    updateReferralStatus(referralCreated.id, referralStatusUpdate2)

    val statusHistory2 =
      referralStatusHistoryRepository.getAllByReferralIdOrderByStatusStartDateDesc(referralCreated.id)
    statusHistory2.size shouldBeEqual 3
    statusHistory2[0].status.code shouldBeEqual REFERRAL_WITHDRAWN
    statusHistory2[0].category!!.code shouldBeEqual "W_ADMIN"
    statusHistory2[0].reason!!.code shouldBeEqual "W_DUPLICATE"
    statusHistory2[0].previousStatus!!.code shouldBeEqual REFERRAL_SUBMITTED
    statusHistory2[0].statusEndDate shouldBe (null)

    statusHistory2[1].status.code shouldBeEqual REFERRAL_SUBMITTED
    statusHistory2[2].status.code shouldBeEqual REFERRAL_STARTED
  }

  @Test
  fun `Get referral status transitions`() {
    val referralCreated = createReferral(PRISON_NUMBER_1)

    val referralStatusUpdate1 = ReferralStatusUpdate(
      status = REFERRAL_SUBMITTED,
      ptUser = true,
    )
    updateReferralStatus(referralCreated.id, referralStatusUpdate1)

    val statuses = getReferralTransitions(referralCreated.id)
    statuses.size shouldBeEqual 2

    val withdrawnStatus = ReferralStatusRefData(
      code = REFERRAL_WITHDRAWN,
      description = REFERRAL_WITHDRAWN_ALT_DESCRIPTION,
      colour = REFERRAL_WITHDRAWN_COLOUR,
      hintText = REFERRAL_WITHDRAWN_HINT,
      hasNotes = true,
      hasConfirmation = false,
      closed = true,
      draft = false,
      hold = false,
      release = false,
      deselectAndKeepOpen = false,
      notesOptional = false,
    )
    val onHoldStatus = ReferralStatusRefData(
      code = ON_HOLD_REFERRAL_SUBMITTED,
      description = ON_HOLD_REFERRAL_SUBMITTED_DESCRIPTION,
      colour = ON_HOLD_REFERRAL_SUBMITTED_COLOUR,
      hintText = ON_HOLD_REFERRAL_SUBMITTED_HINT,
      hasNotes = true,
      hasConfirmation = false,
      closed = false,
      draft = false,
      hold = true,
      release = false,
      deselectAndKeepOpen = false,
      notesOptional = false,
    )

    statuses shouldContainAnyOf listOf(withdrawnStatus, onHoldStatus)
  }

  @Test
  fun `Get referral confirmation text`() {
    val referralCreated = createReferral(PRISON_NUMBER_1)

    val confirmationFields = getConfirmationText(referralCreated.id, "ON_HOLD_REFERRAL_SUBMITTED")

    confirmationFields.warningText shouldBe "Submitting this will pause the referral."
  }

  @Test
  fun `Get referral confirmation text assessment started to suitable not ready`() {
    // Given
    val referralCreated = createReferral(PRISON_NUMBER_1)

    val referralStatusUpdate1 = ReferralStatusUpdate(
      status = REFERRAL_SUBMITTED,
      ptUser = true,
    )
    updateReferralStatus(referralCreated.id, referralStatusUpdate1)
    val referralStatusUpdate2 = ReferralStatusUpdate(
      status = "AWAITING_ASSESSMENT",
      ptUser = true,
    )
    updateReferralStatus(referralCreated.id, referralStatusUpdate2)

    val referralStatusUpdate3 = ReferralStatusUpdate(
      status = "ASSESSMENT_STARTED",
      ptUser = true,
    )
    updateReferralStatus(referralCreated.id, referralStatusUpdate3)

    // When
    val confirmationFields = getConfirmationText(
      referralId = referralCreated.id,
      chosenStatusCode = "SUITABLE_NOT_READY",
      ptUser = true,
    )

    // Then
    confirmationFields.primaryHeading shouldBe "Pause referral: suitable but not ready"
    confirmationFields.primaryDescription shouldBe "The referral will be paused until the person is ready to continue."
    confirmationFields.secondaryHeading shouldBe "Give a reason"
    confirmationFields.secondaryDescription shouldBe "You must give a reason why the person is not ready to continue."
  }

  @Test
  fun `Get referral confirmation text assessment started to assessed as suitable`() {
    // Given
    val referralCreated = createReferral(PRISON_NUMBER_1)

    val referralStatusUpdate1 = ReferralStatusUpdate(
      status = REFERRAL_SUBMITTED,
      ptUser = true,
    )
    updateReferralStatus(referralCreated.id, referralStatusUpdate1)
    val referralStatusUpdate2 = ReferralStatusUpdate(
      status = "AWAITING_ASSESSMENT",
      ptUser = true,
    )
    updateReferralStatus(referralCreated.id, referralStatusUpdate2)

    val referralStatusUpdate3 = ReferralStatusUpdate(
      status = "ASSESSMENT_STARTED",
      ptUser = true,
    )
    updateReferralStatus(referralCreated.id, referralStatusUpdate3)

    // When
    val confirmationFields = getConfirmationText(
      referralId = referralCreated.id,
      chosenStatusCode = "ASSESSED_SUITABLE",
      ptUser = true,
    )

    // Then
    confirmationFields.primaryHeading shouldBe "Move referral to assessed as suitable"
    confirmationFields.primaryDescription shouldBe "Submitting this will change the status to assessed as suitable."
    confirmationFields.secondaryHeading shouldBe "Assessed as suitable"
    confirmationFields.secondaryDescription shouldBe "You can give more details about this status update."
  }

  @Test
  fun `Get referral confirmation text referral submitted to awaiting assessment`() {
    // Given
    val referralCreated = createReferral(PRISON_NUMBER_1)

    val referralStatusUpdate1 = ReferralStatusUpdate(
      status = REFERRAL_SUBMITTED,
      ptUser = true,
    )
    updateReferralStatus(referralCreated.id, referralStatusUpdate1)

    // When
    val confirmationFields = getConfirmationText(
      referralId = referralCreated.id,
      chosenStatusCode = "AWAITING_ASSESSMENT",
      ptUser = true,
    )

    // Then
    confirmationFields.primaryHeading shouldBe "Move referral to awaiting assessment"
    confirmationFields.primaryDescription shouldBe "Submitting this will change the status to awaiting assessment."
    confirmationFields.secondaryHeading shouldBe "Awaiting assessment"
    confirmationFields.secondaryDescription shouldBe "You can give more details about this status update."
  }

  @Test
  fun `Get referral confirmation text awaiting assessment to assessment started`() {
    // Given
    val referralCreated = createReferral(PRISON_NUMBER_1)

    val referralStatusUpdate1 = ReferralStatusUpdate(
      status = REFERRAL_SUBMITTED,
      ptUser = true,
    )
    updateReferralStatus(referralCreated.id, referralStatusUpdate1)

    val referralStatusUpdate2 = ReferralStatusUpdate(
      status = "AWAITING_ASSESSMENT",
      ptUser = true,
    )
    updateReferralStatus(referralCreated.id, referralStatusUpdate2)

    // When
    val confirmationFields = getConfirmationText(
      referralId = referralCreated.id,
      chosenStatusCode = "ASSESSMENT_STARTED",
      ptUser = true,
    )

    // Then
    confirmationFields.primaryHeading shouldBe "Move referral to assessment started"
    confirmationFields.primaryDescription shouldBe "Submitting this will change the status to assessment started."
    confirmationFields.secondaryHeading shouldBe "Assessment started"
    confirmationFields.secondaryDescription shouldBe "You can give more details about this status update."
  }

  @Test
  fun `Get referral confirmation text assessment as suitable to on programme`() {
    // Given
    setUp()
    val referralCreated = createReferral(PRISON_NUMBER_1)

    val referralStatusUpdate1 = ReferralStatusUpdate(
      status = REFERRAL_SUBMITTED,
      ptUser = true,
    )
    updateReferralStatus(referralCreated.id, referralStatusUpdate1)

    val referralStatusUpdate2 = ReferralStatusUpdate(
      status = "AWAITING_ASSESSMENT",
      ptUser = true,
    )
    updateReferralStatus(referralCreated.id, referralStatusUpdate2)

    val referralStatusUpdate3 = ReferralStatusUpdate(
      status = "ASSESSMENT_STARTED",
      ptUser = true,
    )
    updateReferralStatus(referralCreated.id, referralStatusUpdate3)

    val referralStatusUpdate4 = ReferralStatusUpdate(
      status = "ASSESSED_SUITABLE",
      ptUser = true,
    )
    updateReferralStatus(referralCreated.id, referralStatusUpdate4)

    // When
    val confirmationFields = getConfirmationText(
      referralId = referralCreated.id,
      chosenStatusCode = "ON_PROGRAMME",
      ptUser = true,
    )

    // Then
    confirmationFields.primaryHeading shouldBe "Move referral to on programme"
    confirmationFields.primaryDescription shouldBe "Submitting this will change the status to on programme."
    confirmationFields.secondaryHeading shouldBe "On programme"
    confirmationFields.secondaryDescription shouldBe "You can give more details about this status update."
  }

  @Test
  fun `Get referral confirmation text on programme to programme complete`() {
    // Given
    val referralCreated = createReferral(PRISON_NUMBER_1)

    val referralStatusUpdate1 = ReferralStatusUpdate(
      status = REFERRAL_SUBMITTED,
      ptUser = true,
    )
    updateReferralStatus(referralCreated.id, referralStatusUpdate1)

    val referralStatusUpdate2 = ReferralStatusUpdate(
      status = "AWAITING_ASSESSMENT",
      ptUser = true,
    )
    updateReferralStatus(referralCreated.id, referralStatusUpdate2)

    val referralStatusUpdate3 = ReferralStatusUpdate(
      status = "ASSESSMENT_STARTED",
      ptUser = true,
    )
    updateReferralStatus(referralCreated.id, referralStatusUpdate3)

    val referralStatusUpdate4 = ReferralStatusUpdate(
      status = "ASSESSED_SUITABLE",
      ptUser = true,
    )
    updateReferralStatus(referralCreated.id, referralStatusUpdate4)

    val referralStatusUpdate5 = ReferralStatusUpdate(
      status = "ON_PROGRAMME",
      ptUser = true,
    )
    updateReferralStatus(referralCreated.id, referralStatusUpdate5)

    // When
    val confirmationFields = getConfirmationText(
      referralId = referralCreated.id,
      chosenStatusCode = "PROGRAMME_COMPLETE",
      ptUser = true,
    )

    // Then
    confirmationFields.primaryHeading shouldBe "Move referral to programme complete"
    confirmationFields.primaryDescription shouldBe "Submitting this will change the status to programme complete."
    confirmationFields.secondaryHeading shouldBe "Programme complete"
    confirmationFields.secondaryDescription shouldBe "You can give more details about this status update."
  }

  @Test
  fun `Get referral status transitions for on programme`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())
    val referralCreated = createReferral(PRISON_NUMBER_1)

    val referralStatusUpdate1 = ReferralStatusUpdate(
      status = REFERRAL_SUBMITTED,
      ptUser = true,
    )
    updateReferralStatus(referralCreated.id, referralStatusUpdate1)
    val referralStatusUpdate2 = ReferralStatusUpdate(
      status = "AWAITING_ASSESSMENT",
      ptUser = true,
    )
    updateReferralStatus(referralCreated.id, referralStatusUpdate2)

    val referralStatusUpdate3 = ReferralStatusUpdate(
      status = "ASSESSMENT_STARTED",
      ptUser = true,
    )
    updateReferralStatus(referralCreated.id, referralStatusUpdate3)

    val referralStatusUpdate4 = ReferralStatusUpdate(
      status = "ASSESSED_SUITABLE",
      ptUser = true,
    )
    updateReferralStatus(referralCreated.id, referralStatusUpdate4)

    val referralStatusUpdate5 = ReferralStatusUpdate(
      status = "ON_PROGRAMME",
      ptUser = true,
    )
    updateReferralStatus(referralCreated.id, referralStatusUpdate5)

    val statuses = getReferralTransitions(referralCreated.id, true)

    val statusDescriptions = statuses.map {
      it.description
    }

    statusDescriptions shouldContainExactlyInAnyOrder listOf(
      "Programme complete",
      "Deselect and close referral",
      "Deselect and keep referral open",
    )

    // check that the follow on statuses are correct and that the alternative descriptions and hint text have been set
    val deselectStatuses = getReferralTransitions(referralCreated.id, ptUser = true, deselectAndKeepOpen = true)
    val assessedSuitable = deselectStatuses.first { it.code == "ASSESSED_SUITABLE" }
    val assessedSuitableNotReady = deselectStatuses.first { it.code == "SUITABLE_NOT_READY" }

    assessedSuitable.description shouldBe "Assessed as suitable and ready"
    assessedSuitable.hintText shouldBe "This person has been deselected. However, they still meet the suitability criteria and can be considered to join a programme when it runs again."

    assessedSuitableNotReady.description shouldBe "Assessed as suitable but not ready"
    assessedSuitableNotReady.hintText shouldBe "This person meets the suitability criteria but is not ready to start the programme. The referral will be paused until they are ready."
  }

  @Test
  fun `Submitting a referral with all fields set should return 204 with no body`() {
    val referralCreated = createReferral(PRISON_NUMBER_1)

    val referralUpdate = ReferralUpdate(
      additionalInformation = "Additional information",
      oasysConfirmed = true,
      hasReviewedProgrammeHistory = true,
      overrideReason = "Override reason",
    )

    updateReferral(referralCreated.id, referralUpdate)
    val readyToSubmitReferral = getReferralById(referralCreated.id)

    submitReferral(readyToSubmitReferral.id)

    getReferralById(readyToSubmitReferral.id).status shouldBeEqual REFERRAL_SUBMITTED.lowercase()

    val statusHistories =
      referralStatusHistoryRepository.getAllByReferralIdOrderByStatusStartDateDesc(referralCreated.id)
    statusHistories.size shouldBeEqual 2
    statusHistories[0].status.code shouldBeEqual "REFERRAL_SUBMITTED"
    statusHistories[1].status.code shouldBeEqual "REFERRAL_STARTED"

    // check for PNI - should not be persisted at this stage
    pniResultRepository.findByReferralIdAndPrisonNumber(referralCreated.id, PRISON_NUMBER_1) shouldBe null
  }

  @Test
  fun `Submitting a referral sets related course participation records draft status to false`() {
    // Given
    val referralCreated = createReferral(PRISON_NUMBER_1)

    val referralUpdate = ReferralUpdate(
      additionalInformation = "Additional information",
      oasysConfirmed = true,
      hasReviewedProgrammeHistory = true,
      overrideReason = "Override reason",
    )

    updateReferral(referralCreated.id, referralUpdate)
    val readyToSubmitReferral = getReferralById(referralCreated.id)

    val courseParticipationId = UUID.fromString("eb357e5d-5416-43bf-a8d2-0dc8fd92162e")
    persistenceHelper.createCourseParticipation(courseParticipationId, referralCreated.id, "A1234AA", "Red Course", "deaden", "Some detail", "Schulist End", "CUSTODY", "INCOMPLETE", 2023, null, isDraft = true, "Joanne Hamill", LocalDateTime.parse("2023-09-21T23:45:12"), null, null)

    // When
    submitReferral(readyToSubmitReferral.id)

    // Then
    val courseParticipationRecords = courseParticipationRepository.findByReferralId(referralCreated.id)
    assertThat(courseParticipationRecords).hasSize(1)
    assertThat(courseParticipationRecords[0].id).isEqualTo(courseParticipationId)
    assertThat(courseParticipationRecords[0].isDraft).isFalse
  }

  @Test
  fun `Submitting a referral when pom does not exist should return all fields and an HTTP 204`() {
    // Given
    val nonExistentPrisonNumber = "NON-EXISTENT"
    val referralCreated = createReferral(nonExistentPrisonNumber)

    val referralUpdate = ReferralUpdate(
      additionalInformation = "Additional information",
      oasysConfirmed = true,
      hasReviewedProgrammeHistory = true,
      overrideReason = null,
    )

    updateReferral(referralCreated.id, referralUpdate)
    val readyToSubmitReferral = getReferralById(referralCreated.id)

    // When
    submitReferral(readyToSubmitReferral.id)

    // Then
    getReferralById(readyToSubmitReferral.id).status shouldBeEqual REFERRAL_SUBMITTED.lowercase()

    val statusHistories =
      referralStatusHistoryRepository.getAllByReferralIdOrderByStatusStartDateDesc(referralCreated.id)
    statusHistories.size shouldBeEqual 2
    statusHistories[0].status.code shouldBeEqual "REFERRAL_SUBMITTED"
    statusHistories[1].status.code shouldBeEqual "REFERRAL_STARTED"

    // check for PNI - should not be persisted at this stage
    pniResultRepository.findByReferralIdAndPrisonNumber(referralCreated.id, nonExistentPrisonNumber) shouldBe null
  }

  @Test
  fun `should persist PNI when referral status is updated to On Programme`() {
    // Given
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())
    val referralCreated = createReferral(PRISON_NUMBER_1)

    val referralStatusUpdate1 = ReferralStatusUpdate(
      status = REFERRAL_SUBMITTED,
      ptUser = true,
    )
    updateReferralStatus(referralCreated.id, referralStatusUpdate1)
    val referralStatusUpdate2 = ReferralStatusUpdate(
      status = "AWAITING_ASSESSMENT",
      ptUser = true,
    )
    updateReferralStatus(referralCreated.id, referralStatusUpdate2)

    val referralStatusUpdate3 = ReferralStatusUpdate(
      status = "ASSESSMENT_STARTED",
      ptUser = true,
    )
    updateReferralStatus(referralCreated.id, referralStatusUpdate3)

    val referralStatusUpdate4 = ReferralStatusUpdate(
      status = "ASSESSED_SUITABLE",
      ptUser = true,
    )
    updateReferralStatus(referralCreated.id, referralStatusUpdate4)

    // When
    val referralStatusUpdate5 = ReferralStatusUpdate(
      status = "ON_PROGRAMME",
      ptUser = true,
    )
    updateReferralStatus(referralCreated.id, referralStatusUpdate5)

    // Then
    val pniResult = pniResultRepository.findByReferralIdAndPrisonNumber(referralCreated.id, PRISON_NUMBER_1)
    pniResult shouldNotBe null
    pniResult?.prisonNumber?.shouldBeEqual(PRISON_NUMBER_1)
    pniResult?.crn?.shouldBeEqual("X739590")
    pniResult?.oasysAssessmentId?.shouldBeEqual(2114584)
  }

  @Test
  fun `Submitting a nonexistent referral should return 404 with error body`() {
    webTestClient
      .post()
      .uri("/referrals/${UUID.randomUUID()}/submit")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isNotFound
  }

  fun createReferral(prisonNumber: String = PRISON_NUMBER_1, originalReferralId: UUID? = null): Referral {
    val course = getAllCourses().first()
    val offering = getAllOfferingsForCourse(course.id).first()
    return createReferral(offering.id, prisonNumber, originalReferralId)
  }

  fun createDuplicateReferralResultsInConflict(offeringId: UUID?, prisonNumber: String = PRISON_NUMBER_1) = webTestClient
    .post()
    .uri("/referrals")
    .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
    .contentType(MediaType.APPLICATION_JSON)
    .accept(MediaType.APPLICATION_JSON)
    .bodyValue(
      ReferralCreate(
        offeringId = offeringId!!,
        prisonNumber = prisonNumber,
      ),
    )
    .exchange()
    .expectStatus().is4xxClientError
    .expectBody<Referral>()
    .returnResult().responseBody!!

  fun createReferral(offeringId: UUID?, prisonNumber: String = PRISON_NUMBER_1, originalReferralId: UUID? = null) = webTestClient
    .post()
    .uri("/referrals")
    .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
    .contentType(MediaType.APPLICATION_JSON)
    .accept(MediaType.APPLICATION_JSON)
    .bodyValue(
      ReferralCreate(
        offeringId = offeringId!!,
        prisonNumber = prisonNumber,
        originalReferralId = originalReferralId,
      ),
    )
    .exchange()
    .expectStatus().isCreated
    .expectBody<Referral>()
    .returnResult().responseBody!!

  fun getReferralById(createdReferralId: UUID) = performRequestAndExpectOk(HttpMethod.GET, "/referrals/$createdReferralId", referralTypeReference())

  fun updateReferral(referralId: UUID, referralUpdate: ReferralUpdate): Any = webTestClient
    .put()
    .uri("/referrals/$referralId")
    .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
    .contentType(MediaType.APPLICATION_JSON)
    .bodyValue(referralUpdate)
    .exchange()
    .expectStatus().isNoContent

  private fun updateReferralStatus(createdReferralId: UUID, referralStatusUpdate: ReferralStatusUpdate) = webTestClient
    .put()
    .uri("/referrals/$createdReferralId/status")
    .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
    .contentType(MediaType.APPLICATION_JSON)
    .bodyValue(referralStatusUpdate)
    .exchange().expectStatus().isNoContent

  fun submitReferral(createdReferralId: UUID) = performRequestAndExpectOk(HttpMethod.POST, "/referrals/$createdReferralId/submit", referralTypeReference())

  private fun encodeValue(value: String): String = URLEncoder.encode(value, StandardCharsets.UTF_8.toString())

  @Test
  fun `Retrieving a list of filtered referral views for an organisation should return 200 with correct body`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())
    val course = getAllCourses().first()
    val offering = getAllOfferingsForCourse(course.id).first()
    val referralCreated = createReferral(offering.id, PRISON_NUMBER_1)
    val createdReferral = getReferralById(referralCreated.id)

    referralCreated.id.shouldNotBeNull()
    createdReferral.shouldNotBeNull()

    val statusFilter = listOf(createdReferral.status)
    val audienceFilter = course.audience
    val courseNameFilter = course.name

    val summary = getReferralViewsByOrganisationId(ORGANISATION_ID_MDI, statusFilter, audienceFilter, courseNameFilter)
    summary.content.shouldNotBeEmpty()

    summary.content?.forEach { actualSummary ->
      listOf(
        ReferralView(
          id = createdReferral.id,
          courseName = course.name,
          audience = course.audience,
          status = createdReferral.status.lowercase(),
          statusDescription = createdReferral.statusDescription,
          statusColour = createdReferral.statusColour,
          prisonNumber = createdReferral.prisonNumber,
          referrerUsername = REFERRER_USERNAME,
          forename = PRISONER_1.firstName,
          surname = PRISONER_1.lastName,
          sentenceType = "Determinate",

        ),
      ).forEach { referralView ->
        actualSummary.id shouldBe referralView.id
        actualSummary.courseName shouldBe referralView.courseName
        actualSummary.audience shouldBe referralView.audience
        actualSummary.status shouldBe referralView.status
        actualSummary.statusDescription shouldBe referralView.statusDescription
        actualSummary.statusColour shouldBe referralView.statusColour
        actualSummary.prisonNumber shouldBe referralView.prisonNumber
        actualSummary.referrerUsername shouldBe referralView.referrerUsername
        actualSummary.forename shouldBe referralView.forename
        actualSummary.surname shouldBe referralView.surname
        actualSummary.sentenceType shouldBe referralView.sentenceType
        actualSummary.listDisplayName shouldBe referralView.courseName
      }
    }
  }

  fun getReferralTransitions(referralId: UUID, ptUser: Boolean = false, deselectAndKeepOpen: Boolean = false) = performRequestAndExpectOk(
    HttpMethod.GET,
    "/referrals/$referralId/status-transitions?ptUser=$ptUser&deselectAndKeepOpen=$deselectAndKeepOpen",
    object : ParameterizedTypeReference<List<ReferralStatusRefData>>() {},
  )

  fun getConfirmationText(
    referralId: UUID,
    chosenStatusCode: String,
    ptUser: Boolean = false,
    deselectAndKeepOpen: Boolean = false,
  ) = performRequestAndExpectOk(
    HttpMethod.GET,
    "/referrals/$referralId/confirmation-text/$chosenStatusCode?ptUser=$ptUser&deselectAndKeepOpen=$deselectAndKeepOpen",
    object : ParameterizedTypeReference<ConfirmationFields>() {},
  )

  @Test
  fun `Retrieving a list of draft referral views for an organisation using referral group should return 200 with correct body`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())
    val course = getAllCourses().first()
    val offering = getAllOfferingsForCourse(course.id).first()
    val referralCreated = createReferral(offering.id, PRISON_NUMBER_1)
    val createdReferral = getReferralById(referralCreated.id)

    referralCreated.id.shouldNotBeNull()
    createdReferral.shouldNotBeNull()

    var summary = getReferralViewsByOrganisationId(ORGANISATION_ID_MDI, statusGroupFilter = "open")
    summary.content.shouldBeEmpty()

    summary = getReferralViewsByOrganisationId(ORGANISATION_ID_MDI, statusGroupFilter = "draft")
    summary.content.shouldNotBeEmpty()

    summary.content?.forEach { actualSummary ->
      listOf(
        ReferralView(
          id = createdReferral.id,
          courseName = course.name,
          audience = course.audience,
          status = createdReferral.status.lowercase(),
          statusDescription = createdReferral.statusDescription,
          statusColour = createdReferral.statusColour,
          prisonNumber = createdReferral.prisonNumber,
          referrerUsername = REFERRER_USERNAME,
          forename = PRISONER_1.firstName,
          surname = PRISONER_1.lastName,
        ),
      ).forEach { referralView ->
        actualSummary.id shouldBe referralView.id
        actualSummary.courseName shouldBe referralView.courseName
        actualSummary.audience shouldBe referralView.audience
        actualSummary.status shouldBe referralView.status
        actualSummary.statusDescription shouldBe referralView.statusDescription
        actualSummary.statusColour shouldBe referralView.statusColour
        actualSummary.prisonNumber shouldBe referralView.prisonNumber
        actualSummary.referrerUsername shouldBe referralView.referrerUsername
        actualSummary.forename shouldBe referralView.forename
        actualSummary.surname shouldBe referralView.surname
      }
    }
  }

  @Test
  fun `Retrieving a list of draft referral views for an organisation using referral group and status should return 200 with correct body`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())
    val course = getAllCourses().first()
    val offering = getAllOfferingsForCourse(course.id).first()
    val referralCreated = createReferral(offering.id, PRISON_NUMBER_1)
    val createdReferral = getReferralById(referralCreated.id)

    referralCreated.id.shouldNotBeNull()
    createdReferral.shouldNotBeNull()

    var summary = getReferralViewsByOrganisationId(
      ORGANISATION_ID_MDI,
      statusGroupFilter = "draft",
      statusFilter = listOf("WITHDRAWN"),
    )
    summary.content.shouldBeEmpty()

    summary = getReferralViewsByOrganisationId(
      ORGANISATION_ID_MDI,
      statusGroupFilter = "draft",
      statusFilter = listOf("REFERRAL_STARTED"),
    )
    summary.content.shouldNotBeEmpty()

    summary.content?.forEach { actualSummary ->
      listOf(
        ReferralView(
          id = createdReferral.id,
          courseName = course.name,
          audience = course.audience,
          status = createdReferral.status.lowercase(),
          statusDescription = createdReferral.statusDescription,
          statusColour = createdReferral.statusColour,
          prisonNumber = createdReferral.prisonNumber,
          referrerUsername = REFERRER_USERNAME,
          forename = PRISONER_1.firstName,
          surname = PRISONER_1.lastName,
        ),
      ).forEach { referralView ->
        actualSummary.id shouldBe referralView.id
        actualSummary.courseName shouldBe referralView.courseName
        actualSummary.audience shouldBe referralView.audience
        actualSummary.status shouldBe referralView.status
        actualSummary.statusDescription shouldBe referralView.statusDescription
        actualSummary.statusColour shouldBe referralView.statusColour
        actualSummary.prisonNumber shouldBe referralView.prisonNumber
        actualSummary.referrerUsername shouldBe referralView.referrerUsername
        actualSummary.forename shouldBe referralView.forename
        actualSummary.surname shouldBe referralView.surname
      }
    }
  }

  @Test
  fun `Retrieving a referral views for an organisation prisonerId return 200 with correct body`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())
    val course = getAllCourses().first()
    val offering = getAllOfferingsForCourse(course.id).first()
    val referralCreated = createReferral(offering.id, PRISON_NUMBER_1)
    val createdReferral = getReferralById(referralCreated.id)

    referralCreated.id.shouldNotBeNull()
    createdReferral.shouldNotBeNull()

    var summary = getReferralViewsByOrganisationId(ORGANISATION_ID_MDI, nameOrId = "A1234BB")
    summary.content.shouldBeEmpty()

    summary = getReferralViewsByOrganisationId(ORGANISATION_ID_MDI, nameOrId = PRISON_NUMBER_1)
    summary.content.shouldNotBeEmpty()

    summary.content?.forEach { actualSummary ->
      listOf(
        ReferralView(
          id = createdReferral.id,
          courseName = course.name,
          audience = course.audience,
          status = createdReferral.status.lowercase(),
          statusDescription = createdReferral.statusDescription,
          statusColour = createdReferral.statusColour,
          prisonNumber = createdReferral.prisonNumber,
          referrerUsername = REFERRER_USERNAME,
          forename = PRISONER_1.firstName,
          surname = PRISONER_1.lastName,
        ),
      ).forEach { referralView ->
        actualSummary.id shouldBe referralView.id
        actualSummary.courseName shouldBe referralView.courseName
        actualSummary.audience shouldBe referralView.audience
        actualSummary.status shouldBe referralView.status
        actualSummary.statusDescription shouldBe referralView.statusDescription
        actualSummary.statusColour shouldBe referralView.statusColour
        actualSummary.prisonNumber shouldBe referralView.prisonNumber
        actualSummary.referrerUsername shouldBe referralView.referrerUsername
        actualSummary.forename shouldBe referralView.forename
        actualSummary.surname shouldBe referralView.surname
      }
    }
  }

  @Test
  fun `Retrieving a referral views for an organisation one name return 200 with correct body`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())
    val courseId = UUID.randomUUID()
    val courseName = getRandomString(10)
    val audience = getRandomString(10)
    val offeringId = UUID.randomUUID()

    persistenceHelper.createOrganisation(code = "XXX", name = "XXX org")
    persistenceHelper.createEnabledOrganisation("XXX", "XXX org")

    createCourse(
      courseId = courseId,
      identifier = getRandomString(4),
      courseName = courseName,
      description = getRandomString(100),
      audience = audience,
    )

    createOffering(
      courseId = courseId,
      offeringId = offeringId,
      orgId = "XXX",
    )

    val referralCreated = createReferral(offeringId, PRISON_NUMBER_1)
    val createdReferral = getReferralById(referralCreated.id)

    referralCreated.id.shouldNotBeNull()
    createdReferral.shouldNotBeNull()

    var summary = getReferralViewsByOrganisationId("XXX", nameOrId = "STEVO")
    summary.content.shouldBeEmpty()

    summary = getReferralViewsByOrganisationId("XXX", nameOrId = "OHN")
    summary.content.shouldNotBeEmpty()

    summary.content?.forEach { actualSummary ->
      listOf(
        ReferralView(
          id = createdReferral.id,
          courseName = courseName,
          audience = audience,
          status = createdReferral.status.lowercase(),
          statusDescription = createdReferral.statusDescription,
          statusColour = createdReferral.statusColour,
          prisonNumber = createdReferral.prisonNumber,
          referrerUsername = REFERRER_USERNAME,
          forename = PRISONER_1.firstName,
          surname = PRISONER_1.lastName,
        ),
      ).forEach { referralView ->
        actualSummary.id shouldBe referralView.id
        actualSummary.courseName shouldBe referralView.courseName
        actualSummary.audience shouldBe referralView.audience
        actualSummary.status shouldBe referralView.status
        actualSummary.statusDescription shouldBe referralView.statusDescription
        actualSummary.statusColour shouldBe referralView.statusColour
        actualSummary.prisonNumber shouldBe referralView.prisonNumber
        actualSummary.referrerUsername shouldBe referralView.referrerUsername
        actualSummary.forename shouldBe referralView.forename
        actualSummary.surname shouldBe referralView.surname
      }
    }
  }

  @Test
  fun `Retrieving a referral views for an organisation two names return 200 with correct body`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())
    val course = getAllCourses().first()
    val offering = getAllOfferingsForCourse(course.id).first()
    val referralCreated = createReferral(offering.id, PRISON_NUMBER_1)
    val createdReferral = getReferralById(referralCreated.id)

    referralCreated.id.shouldNotBeNull()
    createdReferral.shouldNotBeNull()

    var summary = getReferralViewsByOrganisationId(ORGANISATION_ID_MDI, nameOrId = "STEVO MCSTEVO")
    summary.content.shouldBeEmpty()

    summary = getReferralViewsByOrganisationId(ORGANISATION_ID_MDI, nameOrId = "John Smith")
    summary.content.shouldNotBeEmpty()

    summary.content?.forEach { actualSummary ->
      listOf(
        ReferralView(
          id = createdReferral.id,
          courseName = course.name,
          audience = course.audience,
          status = createdReferral.status.lowercase(),
          statusDescription = createdReferral.statusDescription,
          statusColour = createdReferral.statusColour,
          prisonNumber = createdReferral.prisonNumber,
          referrerUsername = REFERRER_USERNAME,
          forename = PRISONER_1.firstName,
          surname = PRISONER_1.lastName,
        ),
      ).forEach { referralView ->
        actualSummary.id shouldBe referralView.id
        actualSummary.courseName shouldBe referralView.courseName
        actualSummary.audience shouldBe referralView.audience
        actualSummary.status shouldBe referralView.status
        actualSummary.statusDescription shouldBe referralView.statusDescription
        actualSummary.statusColour shouldBe referralView.statusColour
        actualSummary.prisonNumber shouldBe referralView.prisonNumber
        actualSummary.referrerUsername shouldBe referralView.referrerUsername
        actualSummary.forename shouldBe referralView.forename
        actualSummary.surname shouldBe referralView.surname
      }
    }
  }

  @Test
  fun `Retrieving a list of filtered referrals views for an organisation with unknown course filter should return 200 with empty body`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())
    val course = getAllCourses().first()
    getAllOfferingsForCourse(course.id).first()
    val referralCreated = createReferral(PRISON_NUMBER_1)
    val createdReferral = getReferralById(referralCreated.id)

    referralCreated.id.shouldNotBeNull()
    createdReferral.shouldNotBeNull()

    val statusFilter = listOf(createdReferral.status)
    val audienceFilter = course.audience
    val courseNameFilter = course.name + "not a course"

    val summary = getReferralViewsByOrganisationId(ORGANISATION_ID_MDI, statusFilter, audienceFilter, courseNameFilter)
    summary.content.shouldBeEmpty()
  }

  @Test
  fun `Referrals visible in referrer and poms my referrals view`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())
    val course = getAllCourses().first()
    val offering = getAllOfferingsForCourse(course.id).first()
    val referralId = UUID.randomUUID()

    persistenceHelper.createStaff(
      staffId = "10".toBigInteger(),
      firstName = "John",
      lastName = "Doe",
      username = "JOHN_DOE",
      primaryEmail = "john.doe@test.com",
    )
    persistenceHelper.createReferrerUser("TEST_REFERRER_USER_1")
    persistenceHelper.createReferral(
      referralId,
      offering.id!!,
      PRISON_NUMBER_1,
      "TEST_REFERRER_USER_1",
      "more information",
      true,
      true,
      "REFERRAL_SUBMITTED",
      LocalDateTime.parse("2023-11-13T19:11:00"),
      "10".toBigInteger(),
    )
    val statusFilter = listOf("REFERRAL_SUBMITTED")
    val audienceFilter = course.audience
    val courseNameFilter = course.name

    val summary = getReferralViewsByUsername(statusFilter, audienceFilter, courseNameFilter)
    summary.content.shouldNotBeEmpty()

    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken("JOHN_DOE"))
    val summary1 = getReferralViewsByUsername(statusFilter, audienceFilter, courseNameFilter, jwtAuthHelper.bearerToken("JOHN_DOE"))
    summary1.content.shouldNotBeEmpty()
  }

  @Test
  fun `Retrieving a list of filtered referral views for the current user should return 200 with correct body`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())
    val course = getAllCourses().first()
    val offering = getAllOfferingsForCourse(course.id).first()
    val referralCreated = createReferral(offering.id, PRISON_NUMBER_1)
    val createdReferral = getReferralById(referralCreated.id)

    referralCreated.id.shouldNotBeNull()
    createdReferral.shouldNotBeNull()

    val statusFilter = listOf(createdReferral.status)
    val audienceFilter = course.audience
    val courseNameFilter = course.name

    val summary = getReferralViewsByUsername(statusFilter, audienceFilter, courseNameFilter)
    summary.content.shouldNotBeEmpty()

    summary.content?.forEach { actualSummary ->
      listOf(
        ReferralView(
          id = createdReferral.id,
          courseName = course.name,
          audience = course.audience,
          status = createdReferral.status.lowercase(),
          statusDescription = createdReferral.statusDescription,
          statusColour = createdReferral.statusColour,
          prisonNumber = createdReferral.prisonNumber,
          referrerUsername = REFERRER_USERNAME,
          forename = PRISONER_1.firstName,
          surname = PRISONER_1.lastName,
        ),
      ).forEach { referralView ->
        actualSummary.id shouldBe referralView.id
        actualSummary.courseName shouldBe referralView.courseName
        actualSummary.audience shouldBe referralView.audience
        actualSummary.status shouldBe referralView.status
        actualSummary.statusDescription shouldBe referralView.statusDescription
        actualSummary.statusColour shouldBe referralView.statusColour
        actualSummary.prisonNumber shouldBe referralView.prisonNumber
        actualSummary.referrerUsername shouldBe referralView.referrerUsername
        actualSummary.forename shouldBe referralView.forename
        actualSummary.surname shouldBe referralView.surname
      }
    }
  }

  fun getReferralViewsByOrganisationId(
    organisationId: String,
    statusFilter: List<String>? = null,
    audienceFilter: String? = null,
    courseNameFilter: String? = null,
    statusGroupFilter: String? = null,
    pageNumber: Number = 0,
    sortColumn: String? = null,
    sortDirection: String? = null,
    nameOrId: String? = null,
  ): PaginatedReferralView {
    val uriBuilder = UriComponentsBuilder.fromUriString("/referrals/view/organisation/$organisationId/dashboard")
    statusFilter?.let { uriBuilder.queryParam("status", it.joinToString(",")) }
    audienceFilter?.let { uriBuilder.queryParam("audience", encodeValue(it)) }
    courseNameFilter?.let { uriBuilder.queryParam("courseName", encodeValue(it)) }
    statusGroupFilter?.let { uriBuilder.queryParam("statusGroup", encodeValue(it)) }
    uriBuilder.queryParam("page", pageNumber)
    sortColumn?.let { uriBuilder.queryParam("sortColumn", encodeValue(it)) }
    sortDirection?.let { uriBuilder.queryParam("sortDirection", encodeValue(it)) }
    nameOrId?.let { uriBuilder.queryParam("nameOrId", encodeValue(it)) }

    return performRequestAndExpectOk(HttpMethod.GET, uriBuilder.toUriString(), paginatedReferralViewTypeReference())
  }

  fun getReferralViewsByUsername(
    statusFilter: List<String>? = null,
    audienceFilter: String? = null,
    courseNameFilter: String? = null,
    token: String? = null,
  ): PaginatedReferralView {
    val uriBuilder = UriComponentsBuilder.fromUriString("/referrals/view/me/dashboard")
    statusFilter?.let { uriBuilder.queryParam("status", it.joinToString(",")) }
    audienceFilter?.let { uriBuilder.queryParam("audience", encodeValue(it)) }
    courseNameFilter?.let { uriBuilder.queryParam("courseName", encodeValue(it)) }

    return webTestClient
      .get()
      .uri(uriBuilder.toUriString())
      .header(HttpHeaders.AUTHORIZATION, token ?: jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody<PaginatedReferralView>()
      .returnResult().responseBody!!
  }

  @Test
  fun `Retrieving a list of referral views for an organisation should return 200 with correct body`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())
    val course = getAllCourses().first()
    val offering = getAllOfferingsForCourse(course.id).first()

    repeat(21) {
      val courseId = UUID.randomUUID()
      val offeringId = UUID.randomUUID()
      createCourse(
        courseId = courseId,
        identifier = getRandomString(4),
        courseName = getRandomString(10),
        description = getRandomString(50),
      )

      createOffering(
        courseId = courseId,
        offeringId = offeringId,
        orgId = ORGANISATION_ID_MDI,
      )
      createReferral(offeringId = offeringId, PRISON_NUMBER_1)
    }

    val summaryPage0 =
      getReferralViewsByOrganisationId(ORGANISATION_ID_MDI, sortColumn = "audience", sortDirection = "descending")
    summaryPage0.content.shouldNotBeEmpty()
    summaryPage0.content?.size?.shouldBeEqual(10)
    summaryPage0.totalPages.shouldBe(3)
    summaryPage0.totalElements.shouldBe(21)
    summaryPage0.pageNumber.shouldBe(0)
    summaryPage0.pageSize.shouldBe(10)

    val summaryPage1 = getReferralViewsByOrganisationId(
      ORGANISATION_ID_MDI,
      pageNumber = 1,
      sortColumn = "audience",
      sortDirection = "descending",
    )
    summaryPage1.content.shouldNotBeEmpty()
    summaryPage1.content?.size?.shouldBeEqual(10)
    summaryPage1.totalPages.shouldBe(3)
    summaryPage1.totalElements.shouldBe(21)
    summaryPage1.pageNumber.shouldBe(1)
    summaryPage1.pageSize.shouldBe(10)

    val summaryPage2 = getReferralViewsByOrganisationId(
      ORGANISATION_ID_MDI,
      pageNumber = 2,
      sortColumn = "audience",
      sortDirection = "descending",
    )
    summaryPage2.content.shouldNotBeEmpty()
    summaryPage2.content?.size?.shouldBeEqual(1)
    summaryPage2.totalPages.shouldBe(3)
    summaryPage2.totalElements.shouldBe(21)
    summaryPage2.pageNumber.shouldBe(2)
    summaryPage2.pageSize.shouldBe(10)
  }

  @Test
  fun `get subject access report for a referral`() {
    // Mocking a JWT token for the request
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())

    // Fetching a course and its offering
    val course = getAllCourses().first()
    val offering = getAllOfferingsForCourse(course.id).first()

    // Creating a referral for the given offering
    createReferral(offering.id, PRISON_NUMBER_1)

    // Fetching the referral entity
    val referralEntities = referralRepository.getSarReferrals(PRISON_NUMBER_1)
    referralEntities.shouldNotBeNull()
    referralEntities.shouldNotBeEmpty()
    val referralEntity = referralEntities.first()

    // Fetching the subject access report
    val response = getSubjectAccessReport(PRISON_NUMBER_1)
    response.shouldNotBeNull()

    // Validating the response content
    with(response.content.referrals.first()) {
      courseName shouldBe course.name
      audience shouldBe course.audience
      courseOrganisation shouldBe offering.organisationId
      oasysConfirmed shouldBe referralEntity.oasysConfirmed
      additionalInformation shouldBe referralEntity.additionalInformation
      overrideReason shouldBe referralEntity.overrideReason
      transferReason shouldBe referralEntity.transferReason
      originalReferralId shouldBe referralEntity.originalReferralId
      hasReviewedProgrammeHistory shouldBe referralEntity.hasReviewedProgrammeHistory
      statusCode shouldBe referralEntity.status
      referrerUsername shouldBe referralEntity.referrer.username
    }
  }

  fun getSubjectAccessReport(prisonerId: String): HmppsSubjectAccessRequestContent {
    val responseType: ParameterizedTypeReference<HmppsSubjectAccessRequestContent> = object : ParameterizedTypeReference<HmppsSubjectAccessRequestContent>() {}
    return performRequestAndExpectOk(HttpMethod.GET, "/subject-access-request?prn=$prisonerId", responseType)
  }

  @Test
  fun `Delete a nonexistent referral should return 404`() {
    webTestClient
      .delete()
      .uri("/referrals/${UUID.randomUUID()}")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .exchange().expectStatus().isNotFound
  }

  @Test
  fun `delete referral unsuccessful for non draft referrals`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())
    val referralCreated = createReferral(PRISON_NUMBER_1)

    val referralStatusUpdate = ReferralStatusUpdate(
      status = REFERRAL_SUBMITTED,
    )

    updateReferralStatus(referralCreated.id, referralStatusUpdate)

    webTestClient
      .delete()
      .uri("/referrals/${referralCreated.id}")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .exchange().expectStatus().isBadRequest
  }

  @Test
  fun `delete draft referral successful`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())

    val course = getAllCourses().first()
    val offering = getAllOfferingsForCourse(course.id).first()
    val createdReferral = createReferral(offering.id, PRISON_NUMBER_1)

    val referralHistories =
      referralStatusHistoryRepository.getAllByReferralIdOrderByStatusStartDateDesc(createdReferral.id)
    referralHistories.shouldNotBeEmpty()
    referralHistories.size shouldBe 1
    referralHistories[0].status.code.shouldBeEqual("REFERRAL_STARTED")
    referralHistories[0].status.draft.shouldBe(true)

    deleteReferralById(createdReferral.id)

    val twoSecondsAgo = LocalDateTime.now().minusSeconds(2)
    val auditEntity = auditRepository.findAll()
      .filter {
        it.prisonNumber == PRISON_NUMBER_1 &&
          it.referralId == createdReferral.id &&
          it.auditAction == AuditAction.DELETE_REFERRAL.name &&
          it.auditDateTime.isAfter(twoSecondsAgo)
      }

    auditEntity shouldNotBe null
  }

  @Test
  fun `should delete referral and associated course participation records`() {
    // Given
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())

    val course = getAllCourses().first()
    val offering = getAllOfferingsForCourse(course.id).first()
    val createdReferral = createReferral(offering.id, PRISON_NUMBER_1)

    persistenceHelper.createCourseParticipation(UUID.randomUUID(), createdReferral.id, "A1234AA", "Red Course", "deaden", "Some detail", "Schulist End", "CUSTODY", "INCOMPLETE", 2023, null, false, "Joanne Hamill", LocalDateTime.parse("2023-09-21T23:45:12"), null, null)
    persistenceHelper.createCourseParticipation(UUID.randomUUID(), createdReferral.id, "B2345BB", "Marzipan Course", "Reader's Digest", "This participation will be deleted", "Schulist End", "CUSTODY", "INCOMPLETE", 2023, null, false, "Adele Chiellini", LocalDateTime.parse("2023-11-26T10:20:45"), null, null)
    courseParticipationRepository.findByReferralId(createdReferral.id).size shouldBe 2

    // When
    deleteReferral(createdReferral.id)

    // Then
    referralRepository.findById(createdReferral.id).shouldBeEmpty()
    courseParticipationRepository.findByReferralId(createdReferral.id).size shouldBe 0
  }

  @Test
  fun `update all people`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())

    val nomsNumber = "C6666DD"
    val course = getAllCourses().first()
    val offering = getAllOfferingsForCourse(course.id).first()
    createReferral(offering.id, nomsNumber)

    val referralViewBefore = personRepository.findAll().firstOrNull { it.prisonNumber == nomsNumber }
    referralViewBefore shouldNotBe null
    referralViewBefore?.forename?.shouldBeEqual("JOHN")
    referralViewBefore?.surname?.shouldBeEqual("SMITH")

    val results = ResourceLoader.file<List<Prisoner>>("prison-search-results")
    val result = results[0]
    result.lastName = "changed"
    result.firstName = "name"
    wiremockServer.stubFor(
      WireMock.post(WireMock.urlEqualTo("/prisoner-search/prisoner-numbers")).withRequestBody(
        WireMock.containing(
          nomsNumber,
        ),
      )
        .willReturn(
          WireMock.aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(objectMapper.writeValueAsString(listOf(result))),
        ),
    )

    updateAllPeople()

    await untilCallTo {
      personRepository.findAll().firstOrNull { it.prisonNumber == nomsNumber }
    } matches { it?.surname == "changed" }

    val referralViewAfter = personRepository.findAll().firstOrNull { it.prisonNumber == nomsNumber }

    referralViewAfter shouldNotBe null
    referralViewAfter?.forename?.shouldBeEqual("name")
    referralViewAfter?.surname?.shouldBeEqual("changed")
  }

  @Test
  fun `should set sentence category to no active sentences when updating all people and no sentence data is returned`() {
    // Given
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())
    val nomsNumber = "A8610DY"

    val results = ResourceLoader.file<List<Prisoner>>("prison-search-results_A8610DY")
    wiremockServer.stubFor(
      WireMock.post(WireMock.urlEqualTo("/prisoner-search/prisoner-numbers")).withRequestBody(
        WireMock.containing(
          nomsNumber,
        ),
      )
        .willReturn(
          WireMock.aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(
              objectMapper.writeValueAsString(results),
            ),
        ),
    )

    val course = getAllCourses().first()
    val offering = getAllOfferingsForCourse(course.id).first()
    createReferral(offering.id, nomsNumber)

    val result = results[0]
    result.lastName = "changed"
    result.firstName = "name"
    wiremockServer.stubFor(
      WireMock.post(WireMock.urlEqualTo("/prisoner-search/prisoner-numbers")).withRequestBody(
        WireMock.containing(
          nomsNumber,
        ),
      )
        .willReturn(
          WireMock.aResponse()
            .withHeader("Content-Type", "application/json")
            .withBody(objectMapper.writeValueAsString(listOf(result))),
        ),
    )

    // When
    updateAllPeople()

    await untilCallTo {
      personRepository.findAll().firstOrNull { it.prisonNumber == nomsNumber }
    } matches { it?.surname == "changed" }

    val referralViewAfter = personRepository.findAll().firstOrNull { it.prisonNumber == nomsNumber }

    // Then
    referralViewAfter shouldNotBe null
    referralViewAfter?.sentenceType?.shouldBeEqual("No active sentences")
  }

  @Test
  fun `deleted referrals does not appear when we searching for specific referral`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())

    val course = getAllCourses().first()
    val offering = getAllOfferingsForCourse(course.id).first()
    val referralCreated = createReferral(offering.id, PRISON_NUMBER_1)

    deleteReferral(referralCreated.id)

    webTestClient
      .get()
      .uri("/referrals/${referralCreated.id}")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isNotFound
  }

  @Test
  fun `get duplicate referrals returns 204 when there is no duplicate referral`() {
    // Given
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())
    val course = getAllCourses().first()
    val offering = getAllOfferingsForCourse(course.id).first()

    webTestClient
      .get()
      .uri("/referrals/duplicates?prisonNumber=$PRISON_NUMBER_1&offeringId=${offering.id}")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isNoContent
  }

  @Test
  fun `get duplicate referrals returns duplicate referrals for matching prisonNumber and offeringId`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())
    val course = getAllCourses().first()
    val offering = getAllOfferingsForCourse(course.id).first()

    persistenceHelper.createReferrerUser("TEST_REFERRER_USER_1")
    val referralId = UUID.randomUUID()
    persistenceHelper.createReferral(
      referralId,
      offering.id!!,
      PRISON_NUMBER_1,
      "TEST_REFERRER_USER_1",
      "more information",
      true,
      true,
      "REFERRAL_SUBMITTED",
      LocalDateTime.now(),
    )

    val responseBody = performRequestAndExpectOk(HttpMethod.GET, "/referrals/duplicates?prisonNumber=$PRISON_NUMBER_1&offeringId=${offering.id}", referralListTypeReference())

    responseBody.size shouldBe 1
    responseBody.first().id shouldBe referralId
    responseBody.first().offeringId shouldBe offering.id!!
    responseBody.first().prisonNumber shouldBe PRISON_NUMBER_1
  }

  fun updateAllPeople() = webTestClient
    .post()
    .uri("/admin/person/updateAll")
    .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
    .accept(MediaType.APPLICATION_JSON)
    .exchange()
    .expectStatus().isOk

  fun deleteReferral(referralId: UUID) {
    webTestClient
      .delete()
      .uri("/referrals/$referralId")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .exchange()
      .expectStatus().isNoContent
  }

  fun createCourse(
    courseId: UUID,
    identifier: String,
    courseName: String,
    description: String,
    altName: String = "",
    audience: String = "General offence",
  ) {
    persistenceHelper.createCourse(
      courseId,
      identifier,
      courseName,
      description,
      altName,
      audience,
    )
  }

  fun createOffering(offeringId: UUID, courseId: UUID, orgId: String) {
    persistenceHelper.createOffering(
      offeringId,
      courseId,
      orgId,
      "nobody-bwn@digital.justice.gov.uk",
      "nobody2-bwn@digital.justice.gov.uk",
      true,
    )
  }

  fun getRandomString(length: Int): String {
    val allowedChars = ('A'..'Z') + ('a'..'z') + ('0'..'9')
    return (1..length)
      .map { allowedChars.random() }
      .joinToString("")
  }

  private fun deleteReferralById(referralId: UUID) {
    webTestClient
      .delete()
      .uri("/referrals/$referralId")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .exchange().expectStatus().isNoContent
  }

  private fun transferReferralToBuildingChoices(referralId: UUID, courseId: UUID): Referral = performRequestAndExpectOk(HttpMethod.POST, "/referrals/$referralId/transfer-to-building-choices/$courseId", referralTypeReference())

  private fun paginatedReferralViewTypeReference(): ParameterizedTypeReference<PaginatedReferralView> = object : ParameterizedTypeReference<PaginatedReferralView>() {}
  private fun referralTypeReference(): ParameterizedTypeReference<Referral> = object : ParameterizedTypeReference<Referral>() {}
  private fun referralListTypeReference(): ParameterizedTypeReference<List<Referral>> = object : ParameterizedTypeReference<List<Referral>>() {}
}
