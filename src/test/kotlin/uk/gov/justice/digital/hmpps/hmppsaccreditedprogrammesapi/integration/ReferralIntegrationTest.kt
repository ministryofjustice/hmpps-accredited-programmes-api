package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration

import com.github.tomakehurst.wiremock.client.WireMock
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainAnyOf
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.awaitility.kotlin.await
import org.awaitility.kotlin.matches
import org.awaitility.kotlin.untilCallTo
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
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonerSearchApi.model.Prisoner
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.ResourceLoader
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
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.AuditAction
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralStatusHistoryRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.AuditRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.PNIResultEntityRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.PersonRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.ReferralRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ConfirmationFields
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.PaginatedReferralView
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Referral
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ReferralCreate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ReferralStatusRefData
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ReferralStatusUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ReferralUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ReferralView
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.HmppsSubjectAccessRequestContent
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.time.LocalDateTime
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

  @Autowired
  lateinit var referralRepository: ReferralRepository

  @Autowired
  lateinit var pniResultRepository: PNIResultEntityRepository

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
    val referralCreated = createReferral(offering.id!!, PRISON_NUMBER_1)

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
    submitReferral(referralCreated.id)

    createDuplicateReferralResultsInConflict(offering.id!!, PRISON_NUMBER_1)
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
    )

    updateReferral(referralCreated.id, referralUpdate)

    getReferralById(referralCreated.id) shouldBeEqual Referral(
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

    updateReferralStatus(referralCreated.id, referralStatusUpdate)

    getReferralById(referralCreated.id) shouldBeEqual Referral(
      id = referralCreated.id,
      offeringId = offering.id!!,
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

    val confirmationFields = getConfirmationText(
      referralId = referralCreated.id,
      chosenStatusCode = "SUITABLE_NOT_READY",
      ptUser = true,
    )

    confirmationFields.primaryHeading shouldBe "Pause referral: suitable but not ready"
    confirmationFields.primaryDescription shouldBe "The referral will be paused until the person is ready to continue."
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

    // check for PNI
    pniResultRepository.findByReferralIdAndPrisonNumber(referralCreated.id, PRISON_NUMBER_1) shouldNotBe null
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

  fun createReferral(prisonNumber: String = PRISON_NUMBER_1): Referral {
    val course = getAllCourses().first()
    val offering = getAllOfferingsForCourse(course.id).first()
    return createReferral(offering.id, prisonNumber)
  }

  fun createDuplicateReferralResultsInConflict(offeringId: UUID?, prisonNumber: String = PRISON_NUMBER_1) =
    webTestClient
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

  fun createReferral(offeringId: UUID?, prisonNumber: String = PRISON_NUMBER_1) =
    webTestClient
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
      .expectStatus().isCreated
      .expectBody<Referral>()
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
    val course = getAllCourses().first()
    val offering = getAllOfferingsForCourse(course.id).first()
    val referralCreated = createReferral(offering.id, PRISON_NUMBER_1)
    val createdReferral = getReferralById(referralCreated.id)

    referralCreated.id.shouldNotBeNull()
    createdReferral.shouldNotBeNull()

    var summary = getReferralViewsByOrganisationId(ORGANISATION_ID_MDI, nameOrId = "STEVO")
    summary.content.shouldBeEmpty()

    summary = getReferralViewsByOrganisationId(ORGANISATION_ID_MDI, nameOrId = "OHN")
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
      val courseId = UUID.randomUUID().toString()
      val offeringId = UUID.randomUUID().toString()
      createCourse(
        courseId = courseId,
        identifier = getRandomString(2),
        courseName = getRandomString(10),
        description = getRandomString(50),
      )

      createOffering(
        courseId = UUID.fromString(courseId),
        offeringId = UUID.fromString(offeringId),
        orgId = ORGANISATION_ID_MDI,
      )
      createReferral(offeringId = UUID.fromString(offeringId), PRISON_NUMBER_1)
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
      hasReviewedProgrammeHistory shouldBe referralEntity.hasReviewedProgrammeHistory
      statusCode shouldBe referralEntity.status
      referrerUsername shouldBe referralEntity.referrer.username
    }
  }

  fun getSubjectAccessReport(prisonerId: String) =
    webTestClient
      .get()
      .uri("/subject-access-request?prn=$prisonerId")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody<HmppsSubjectAccessRequestContent>()
      .returnResult().responseBody!!

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

    webTestClient
      .delete()
      .uri("/referrals/${createdReferral.id}")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .exchange().expectStatus().isNoContent

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

  fun updateAllPeople() =
    webTestClient
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
    courseId: String,
    identifier: String,
    courseName: String,
    description: String,
    altName: String = "",
    audience: String = "General offence",
  ) {
    persistenceHelper.createCourse(
      UUID.fromString(courseId),
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
}
