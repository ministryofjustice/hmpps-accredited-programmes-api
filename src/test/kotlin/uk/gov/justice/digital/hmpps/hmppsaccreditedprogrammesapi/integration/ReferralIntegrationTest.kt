package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration

import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainAnyOf
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.security.test.context.support.WithMockUser
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.expectBody
import org.springframework.web.util.UriComponentsBuilder
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ConfirmationFields
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.PaginatedReferralView
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Referral
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralCreate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralCreated
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralStatusRefData
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralStatusUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralView
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.config.JwtAuthHelper
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
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralStatusHistoryRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.AuditRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.PersonRepository
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(JwtAuthHelper::class)
class ReferralIntegrationTest : IntegrationTestBase() {

  @Autowired
  lateinit var personRepository: PersonRepository

  @Autowired
  lateinit var auditRepository: AuditRepository

  @Autowired
  lateinit var referralStatusHistoryRepository: ReferralStatusHistoryRepository

  @BeforeEach
  fun setUp() {
    persistenceHelper.clearAllTableContent()

    persistenceHelper.createCourse(
      UUID.fromString("d3abc217-75ee-46e9-a010-368f30282367"),
      "SC",
      "Super Course",
      "Sample description",
      "SC++",
      "General offence",
    )
    persistenceHelper.createEnabledOrganisation("BWN", "BWN org")
    persistenceHelper.createEnabledOrganisation("MDI", "MDI org")

    persistenceHelper.createOffering(
      UUID.fromString("7fffcc6a-11f8-4713-be35-cf5ff1aee517"),
      UUID.fromString("d3abc217-75ee-46e9-a010-368f30282367"),
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
    )
    persistenceHelper.createCourse(
      UUID.fromString("1811faa6-d568-4fc4-83ce-41118b90242e"),
      "RC",
      "RAPID Course",
      "Sample description",
      "RC",
      "General offence",
    )
  }

  @Test
  fun `Creating a referral with an existing user should return 201 with correct body`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())

    val course = getAllCourses().first()
    val offering = getAllOfferingsForCourse(course.id).first()
    val referralCreated = createReferral(offering.id, PRISON_NUMBER_1)

    val personEntity = personRepository.findPersonEntityByPrisonNumber(PRISON_NUMBER_1)

    personEntity.shouldNotBeNull()
    personEntity.surname.shouldBeEqual(PRISONER_1.lastName)
    personEntity.forename.shouldBeEqual(PRISONER_1.firstName)

    referralCreated.referralId.shouldNotBeNull()

    getReferralById(referralCreated.referralId) shouldBeEqual Referral(
      id = referralCreated.referralId,
      offeringId = offering.id,
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
    )

    val auditEntity = auditRepository.findAll()
      .firstOrNull { it.prisonNumber == PRISON_NUMBER_1 && it.referralId == referralCreated.referralId }

    auditEntity shouldNotBe null

    // check the referral status is as expected
    val referralHistories =
      referralStatusHistoryRepository.getAllByReferralIdOrderByStatusStartDateDesc(referralCreated.referralId)
    referralHistories.size shouldBeGreaterThan 0

    val referralHistory = referralHistories[0]
    referralHistory.status.code shouldBeEqual "REFERRAL_STARTED"
  }

  @Test
  @WithMockUser(username = "NONEXISTENT_USER")
  fun `Creating a referral with a nonexistent user should return 201 with correct body`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())
    val course = getAllCourses().first()
    val offering = getAllOfferingsForCourse(course.id).first()
    val referralCreated = createReferral(offering.id)

    referralCreated.referralId.shouldNotBeNull()

    getReferralById(referralCreated.referralId) shouldBeEqual Referral(
      id = referralCreated.referralId,
      offeringId = offering.id,
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
    )

    val auditEntity = auditRepository.findAll()
      .firstOrNull { it.prisonNumber == PRISON_NUMBER_1 && it.referralId == referralCreated.referralId }

    auditEntity shouldNotBe null
  }

  @Test
  fun `Updating a referral with a valid payload should return 204 with no body`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())
    val course = getAllCourses().first()
    val offering = getAllOfferingsForCourse(course.id).first()
    val referralCreated = createReferral(offering.id)

    val referralUpdate = ReferralUpdate(
      additionalInformation = "Additional information",
      oasysConfirmed = true,
      hasReviewedProgrammeHistory = true,
    )

    updateReferral(referralCreated.referralId, referralUpdate)

    getReferralById(referralCreated.referralId) shouldBeEqual Referral(
      id = referralCreated.referralId,
      offeringId = offering.id,
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
        ),
      )
      .exchange().expectStatus().isNotFound
  }

  @Test
  fun `Updating a referral status should return 204 with no body`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())
    val course = getAllCourses().first()
    val offering = getAllOfferingsForCourse(course.id).first()
    val referralCreated = createReferral(offering.id)

    val referralStatusUpdate = ReferralStatusUpdate(
      status = REFERRAL_SUBMITTED,
    )

    updateReferralStatus(referralCreated.referralId, referralStatusUpdate)

    getReferralById(referralCreated.referralId) shouldBeEqual Referral(
      id = referralCreated.referralId,
      offeringId = offering.id,
      referrerUsername = REFERRER_USERNAME,
      prisonNumber = PRISON_NUMBER_1,
      status = REFERRAL_SUBMITTED.lowercase(),
      closed = false,
      statusDescription = REFERRAL_SUBMITTED_DESCRIPTION,
      statusColour = REFERRAL_SUBMITTED_COLOUR,
      oasysConfirmed = false,
      additionalInformation = null,
      submittedOn = null,
    )
  }

  @Test
  fun `Updating a referral status should insert a status history record`() {
    val course = getAllCourses().first()
    val offering = getAllOfferingsForCourse(course.id).first()
    val referralCreated = createReferral(offering.id)

    val referralStatusUpdate1 = ReferralStatusUpdate(
      status = REFERRAL_SUBMITTED,
      ptUser = true,
    )
    updateReferralStatus(referralCreated.referralId, referralStatusUpdate1)

    val statusHistory1 =
      referralStatusHistoryRepository.getAllByReferralIdOrderByStatusStartDateDesc(referralCreated.referralId)
    statusHistory1.size shouldBeEqual 2
    statusHistory1[0].status.code shouldBeEqual REFERRAL_SUBMITTED
    statusHistory1[1].status.code shouldBeEqual REFERRAL_STARTED

    val referralStatusUpdate2 = ReferralStatusUpdate(
      status = REFERRAL_WITHDRAWN,
      category = "W_ADMIN",
      reason = "W_DUPLICATE",
      ptUser = true,
    )
    updateReferralStatus(referralCreated.referralId, referralStatusUpdate2)

    val statusHistory2 =
      referralStatusHistoryRepository.getAllByReferralIdOrderByStatusStartDateDesc(referralCreated.referralId)
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
    val course = getAllCourses().first()
    val offering = getAllOfferingsForCourse(course.id).first()
    val referralCreated = createReferral(offering.id)

    val referralStatusUpdate1 = ReferralStatusUpdate(
      status = REFERRAL_SUBMITTED,
      ptUser = true,
    )
    updateReferralStatus(referralCreated.referralId, referralStatusUpdate1)

    val statuses = getReferralTransitions(referralCreated.referralId)
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
    )

    statuses shouldContainAnyOf listOf(withdrawnStatus, onHoldStatus)
  }

  @Test
  fun `Get referral confirmation text`() {
    val course = getAllCourses().first()
    val offering = getAllOfferingsForCourse(course.id).first()
    val referralCreated = createReferral(offering.id)

    val confirmationFields = getConfirmationText(referralCreated.referralId, "ON_HOLD_REFERRAL_SUBMITTED")

    confirmationFields.warningText shouldBe "Submitting this will pause the referral."
  }

  @Test
  fun `Get referral confirmation text assessment started to suitable not ready`() {
    val course = getAllCourses().first()
    val offering = getAllOfferingsForCourse(course.id).first()
    val referralCreated = createReferral(offering.id)

    val referralStatusUpdate1 = ReferralStatusUpdate(
      status = REFERRAL_SUBMITTED,
      ptUser = true,
    )
    updateReferralStatus(referralCreated.referralId, referralStatusUpdate1)
    val referralStatusUpdate2 = ReferralStatusUpdate(
      status = "AWAITING_ASSESSMENT",
      ptUser = true,
    )
    updateReferralStatus(referralCreated.referralId, referralStatusUpdate2)

    val referralStatusUpdate3 = ReferralStatusUpdate(
      status = "ASSESSMENT_STARTED",
      ptUser = true,
    )
    updateReferralStatus(referralCreated.referralId, referralStatusUpdate3)

    val confirmationFields = getConfirmationText(
      referralId = referralCreated.referralId,
      chosenStatusCode = "SUITABLE_NOT_READY",
      ptUser = true,
    )

    confirmationFields.primaryHeading shouldBe "Pause referral: suitable but not ready"
    confirmationFields.primaryDescription shouldBe "The referral will be paused until the person is ready to continue."
  }

  @Test
  fun `Get referral status transitions for on programme`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())
    val course = getAllCourses().first()
    val offering = getAllOfferingsForCourse(course.id).first()
    val referralCreated = createReferral(offering.id)

    val referralStatusUpdate1 = ReferralStatusUpdate(
      status = REFERRAL_SUBMITTED,
      ptUser = true,
    )
    updateReferralStatus(referralCreated.referralId, referralStatusUpdate1)
    val referralStatusUpdate2 = ReferralStatusUpdate(
      status = "AWAITING_ASSESSMENT",
      ptUser = true,
    )
    updateReferralStatus(referralCreated.referralId, referralStatusUpdate2)

    val referralStatusUpdate3 = ReferralStatusUpdate(
      status = "ASSESSMENT_STARTED",
      ptUser = true,
    )
    updateReferralStatus(referralCreated.referralId, referralStatusUpdate3)

    val referralStatusUpdate4 = ReferralStatusUpdate(
      status = "ASSESSED_SUITABLE",
      ptUser = true,
    )
    updateReferralStatus(referralCreated.referralId, referralStatusUpdate4)

    val referralStatusUpdate5 = ReferralStatusUpdate(
      status = "ON_PROGRAMME",
      ptUser = true,
    )
    updateReferralStatus(referralCreated.referralId, referralStatusUpdate5)

    val statuses = getReferralTransitions(referralCreated.referralId, true)

    val statusDescriptions = statuses.map {
      it.description
    }

    statusDescriptions shouldContainExactlyInAnyOrder listOf(
      "Programme complete",
      "Deselect and close referral",
      "Deselect and keep referral open",
    )

    // check that the follow on statuses are correct and that the alternative descriptions and hint text have been set
    val deselectStatuses = getReferralTransitions(referralCreated.referralId, ptUser = true, deselectAndKeepOpen = true)
    val assessedSuitable = deselectStatuses.first { it.code == "ASSESSED_SUITABLE" }
    val assessedSuitableNotReady = deselectStatuses.first { it.code == "SUITABLE_NOT_READY" }

    assessedSuitable.description shouldBe "Assessed as suitable and ready"
    assessedSuitable.hintText shouldBe "This person has been deselected. However, they still meet the suitability criteria and can be considered to join a programme when it runs again."

    assessedSuitableNotReady.description shouldBe "Assessed as suitable but not ready"
    assessedSuitableNotReady.hintText shouldBe "This person meet the suitability criteria but is not ready to start the programme. The referral will be paused until they are ready."
  }

  @Test
  fun `Submitting a referral with all fields set should return 204 with no body`() {
    val course = getAllCourses().first()
    val offering = getAllOfferingsForCourse(course.id).first()
    val referralCreated = createReferral(offering.id)

    val referralUpdate = ReferralUpdate(
      additionalInformation = "Additional information",
      oasysConfirmed = true,
      hasReviewedProgrammeHistory = true,
    )

    updateReferral(referralCreated.referralId, referralUpdate)
    val readyToSubmitReferral = getReferralById(referralCreated.referralId)

    submitReferral(readyToSubmitReferral.id)

    getReferralById(readyToSubmitReferral.id).status shouldBeEqual REFERRAL_SUBMITTED.lowercase()

    val statusHistories =
      referralStatusHistoryRepository.getAllByReferralIdOrderByStatusStartDateDesc(referralCreated.referralId)
    statusHistories.size shouldBeEqual 2
    statusHistories[0].status.code shouldBeEqual "REFERRAL_SUBMITTED"
    statusHistories[1].status.code shouldBeEqual "REFERRAL_STARTED"
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

  fun createReferral(offeringId: UUID, prisonNumber: String = PRISON_NUMBER_1) =
    webTestClient
      .post()
      .uri("/referrals")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .contentType(MediaType.APPLICATION_JSON)
      .accept(MediaType.APPLICATION_JSON)
      .bodyValue(
        ReferralCreate(
          offeringId = offeringId,
          prisonNumber = prisonNumber,
        ),
      )
      .exchange()
      .expectStatus().isCreated
      .expectBody<ReferralCreated>()
      .returnResult().responseBody!!

  fun getReferralById(createdReferralId: UUID) =
    webTestClient
      .get()
      .uri("/referrals/$createdReferralId")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody<Referral>()
      .returnResult().responseBody!!

  fun updateReferral(referralId: UUID, referralUpdate: ReferralUpdate): Any =
    webTestClient
      .put()
      .uri("/referrals/$referralId")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(referralUpdate)
      .exchange()
      .expectStatus().isNoContent

  private fun updateReferralStatus(createdReferralId: UUID, referralStatusUpdate: ReferralStatusUpdate) =
    webTestClient
      .put()
      .uri("/referrals/$createdReferralId/status")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(referralStatusUpdate)
      .exchange().expectStatus().isNoContent

  fun submitReferral(createdReferralId: UUID) {
    webTestClient
      .post()
      .uri("/referrals/$createdReferralId/submit")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isNoContent
  }

  private fun encodeValue(value: String): String {
    return URLEncoder.encode(value, StandardCharsets.UTF_8.toString())
  }

  @Test
  fun `Retrieving a list of filtered referral views for an organisation should return 200 with correct body`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())
    val course = getAllCourses().first()
    val offering = getAllOfferingsForCourse(course.id).first()
    val referralCreated = createReferral(offering.id, PRISON_NUMBER_1)
    val createdReferral = getReferralById(referralCreated.referralId)

    referralCreated.referralId.shouldNotBeNull()
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
          sentenceType = "CJA03 Standard Determinate Sentence",
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
      }
    }
  }

  fun getReferralTransitions(referralId: UUID, ptUser: Boolean = false, deselectAndKeepOpen: Boolean = false) =
    webTestClient
      .get()
      .uri("/referrals/$referralId/status-transitions?ptUser=$ptUser&deselectAndKeepOpen=$deselectAndKeepOpen")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody<List<ReferralStatusRefData>>()
      .returnResult().responseBody!!

  fun getConfirmationText(
    referralId: UUID,
    chosenStatusCode: String,
    ptUser: Boolean = false,
    deselectAndKeepOpen: Boolean = false,
  ) =
    webTestClient
      .get()
      .uri("/referrals/$referralId/confirmation-text/$chosenStatusCode?ptUser=$ptUser&deselectAndKeepOpen=$deselectAndKeepOpen")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody<ConfirmationFields>()
      .returnResult().responseBody!!

  @Test
  fun `Retrieving a list of draft referral views for an organisation using referral group should return 200 with correct body`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())
    val course = getAllCourses().first()
    val offering = getAllOfferingsForCourse(course.id).first()
    val referralCreated = createReferral(offering.id, PRISON_NUMBER_1)
    val createdReferral = getReferralById(referralCreated.referralId)

    referralCreated.referralId.shouldNotBeNull()
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
  fun `Retrieving a list of filtered referrals views for an organisation with unknown course filter should return 200 with empty body`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())
    val course = getAllCourses().first()
    val offering = getAllOfferingsForCourse(course.id).first()
    val referralCreated = createReferral(offering.id, PRISON_NUMBER_1)
    val createdReferral = getReferralById(referralCreated.referralId)

    referralCreated.referralId.shouldNotBeNull()
    createdReferral.shouldNotBeNull()

    val statusFilter = listOf(createdReferral.status)
    val audienceFilter = course.audience
    val courseNameFilter = course.name + "not a course"

    val summary = getReferralViewsByOrganisationId(ORGANISATION_ID_MDI, statusFilter, audienceFilter, courseNameFilter)
    summary.content.shouldBeEmpty()
  }

  @Test
  fun `Retrieving a list of filtered referral views for the current user should return 200 with correct body`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())
    val course = getAllCourses().first()
    val offering = getAllOfferingsForCourse(course.id).first()
    val referralCreated = createReferral(offering.id, PRISON_NUMBER_1)
    val createdReferral = getReferralById(referralCreated.referralId)

    referralCreated.referralId.shouldNotBeNull()
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
  ): PaginatedReferralView {
    val uriBuilder = UriComponentsBuilder.fromUriString("/referrals/view/organisation/$organisationId/dashboard")
    statusFilter?.let { uriBuilder.queryParam("status", it.joinToString(",")) }
    audienceFilter?.let { uriBuilder.queryParam("audience", encodeValue(it)) }
    courseNameFilter?.let { uriBuilder.queryParam("courseName", encodeValue(it)) }
    statusGroupFilter?.let { uriBuilder.queryParam("statusGroup", encodeValue(it)) }
    uriBuilder.queryParam("page", pageNumber)
    sortColumn?.let { uriBuilder.queryParam("sortColumn", encodeValue(it)) }
    sortDirection?.let { uriBuilder.queryParam("sortDirection", encodeValue(it)) }

    return webTestClient
      .get()
      .uri(uriBuilder.toUriString())
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody<PaginatedReferralView>()
      .returnResult().responseBody!!
  }

  fun getReferralViewsByUsername(
    statusFilter: List<String>? = null,
    audienceFilter: String? = null,
    courseNameFilter: String? = null,
  ): PaginatedReferralView {
    val uriBuilder = UriComponentsBuilder.fromUriString("/referrals/view/me/dashboard")
    statusFilter?.let { uriBuilder.queryParam("status", it.joinToString(",")) }
    audienceFilter?.let { uriBuilder.queryParam("audience", encodeValue(it)) }
    courseNameFilter?.let { uriBuilder.queryParam("courseName", encodeValue(it)) }

    return webTestClient
      .get()
      .uri(uriBuilder.toUriString())
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
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
      createReferral(offering.id, PRISON_NUMBER_1)
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
}
