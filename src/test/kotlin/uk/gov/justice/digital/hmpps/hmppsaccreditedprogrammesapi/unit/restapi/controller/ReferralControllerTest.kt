package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.restapi.controller

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.ninjasquad.springmockk.MockkBean
import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.verify
import jakarta.validation.ValidationException
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.put
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.PaginatedReferralSummary
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralStatus
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralSummary
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.config.JwtAuthHelper
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.randomPrisonNumber
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.randomReferrerId
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity.ReferralStatus.AWAITING_ASSESSMENT
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity.ReferralStatus.REFERRAL_STARTED
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity.ReferralStatus.REFERRAL_SUBMITTED
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer.toApi
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.ReferralService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.OfferingEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.ReferralEntityFactory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory.ReferralSummaryProjectionFactory
import java.time.LocalDateTime
import java.util.UUID
import java.util.stream.Stream

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(JwtAuthHelper::class)
class ReferralControllerTest
@Autowired
constructor(
  val mockMvc: MockMvc,
  val jwtAuthHelper: JwtAuthHelper,
) {

  companion object {
    private val referralSummary1 = ReferralSummary(
      id = UUID.randomUUID(),
      courseName = "Course for referralSummary1",
      audiences = listOf("Audience 1", "Audience 2"),
      status = ReferralStatus.referralStarted,
      prisonNumber = PRISON_NUMBER,
    )

    private val referralSummary2 = ReferralSummary(
      id = UUID.randomUUID(),
      courseName = "Course for referralSummary2",
      audiences = listOf("Audience 2", "Audience 3"),
      status = ReferralStatus.referralSubmitted,
      submittedOn = LocalDateTime.MIN.toString(),
      prisonNumber = PRISON_NUMBER,
    )

    private val referralSummary3 = ReferralSummary(
      id = UUID.randomUUID(),
      courseName = "Course for referralSummary3",
      audiences = listOf("Audience 3", "Audience 4"),
      status = ReferralStatus.referralSubmitted,
      submittedOn = LocalDateTime.MIN.toString(),
      prisonNumber = PRISON_NUMBER,
    )

    @JvmStatic
    fun parametersForGetReferralsByOrganisationIdWithFiltering(): Stream<Arguments> {
      return Stream.of(
        Arguments.of("REFERRAL_STARTED", null, "referralSummary1", listOf(referralSummary1)),
        Arguments.of(null, "Audience 2", null, listOf(referralSummary1, referralSummary2)),
        Arguments.of("REFERRAL_SUBMITTED", "Audience 3", null, listOf(referralSummary2, referralSummary3)),
        Arguments.of("REFERRAL_SUBMITTED", "Audience 4", "referralSummary3", listOf(referralSummary3)),
        Arguments.of(null, null, "Course", listOf(referralSummary1, referralSummary2, referralSummary3)),
        Arguments.of("AWAITING_ASSESSMENT", null, null, emptyList<ReferralSummary>()),
        Arguments.of(null, "Audience X", null, emptyList<ReferralSummary>()),
        Arguments.of(null, null, "Course for referralSummaryX", emptyList<ReferralSummary>()),
      )
    }
  }

  @MockkBean
  private lateinit var referralService: ReferralService

  @Test
  fun `createReferral with JWT and valid payload returns 201 with correct body`() {
    val referral = ReferralEntityFactory()
      .withOffering(
        OfferingEntityFactory()
          .withId(UUID.randomUUID())
          .produce(),
      )
      .withPrisonNumber(randomPrisonNumber())
      .withReferrerId(randomReferrerId())
      .produce()

    val payload = mapOf(
      "offeringId" to referral.offering.id,
      "prisonNumber" to referral.prisonNumber,
      "referrerId" to referral.referrerId,
    )

    every { referralService.createReferral(any(), any(), any()) } returns referral.id

    mockMvc.post("/referrals") {
      contentType = MediaType.APPLICATION_JSON
      header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      content = jacksonObjectMapper().writeValueAsString(payload)
    }.andExpect {
      status { isCreated() }
      content {
        contentType(MediaType.APPLICATION_JSON)
        jsonPath("$.referralId") { value(referral.id.toString()) }
      }
    }

    verify { referralService.createReferral(referral.prisonNumber, referral.offering.id!!, referral.referrerId) }
  }

  @Test
  fun `getReferralById with JWT returns 200 with correct body`() {
    val referral = ReferralEntityFactory()
      .withId(UUID.randomUUID())
      .withOffering(
        OfferingEntityFactory()
          .withId(UUID.randomUUID())
          .produce(),
      )
      .withOasysConfirmed(true)
      .withHasReviewedProgrammeHistory(true)
      .produce()

    every { referralService.getReferralById(any()) } returns referral

    mockMvc.get("/referrals/${referral.id}") {
      accept = MediaType.APPLICATION_JSON
      header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
    }.andExpect {
      status { isOk() }
      content {
        contentType(MediaType.APPLICATION_JSON)
        jsonPath("$.offeringId") { value(referral.offering.id.toString()) }
        jsonPath("$.prisonNumber") { value(referral.prisonNumber) }
        jsonPath("$.referrerId") { value(referral.referrerId) }
        jsonPath("$.status") { REFERRAL_STARTED }
        jsonPath("$.oasysConfirmed") { value(true) }
        jsonPath("$.hasReviewedProgrammeHistory") { value(true) }
        jsonPath("$.additionalInformation") { doesNotExist() }
      }
    }

    verify { referralService.getReferralById(referral.id!!) }
  }

  @Test
  fun `getReferralById without JWT returns 401`() {
    mockMvc.get("/referrals/${UUID.randomUUID()}") {
      accept = MediaType.APPLICATION_JSON
    }.andExpect {
      status { isUnauthorized() }
    }
  }

  @Test
  fun `getReferralById with random UUID returns 404 with error body`() {
    val referralId = UUID.randomUUID()

    every { referralService.getReferralById(any()) } returns null

    mockMvc.get("/referrals/$referralId") {
      accept = MediaType.APPLICATION_JSON
      header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
    }.andExpect {
      status { isNotFound() }
      content {
        contentType(MediaType.APPLICATION_JSON)
        jsonPath("$.status") { value(404) }
        jsonPath("$.errorCode") { isEmpty() }
        jsonPath("$.userMessage") { prefix("Not Found: No Referral found at /referrals/$referralId") }
        jsonPath("$.developerMessage") { prefix("No referral found at /courses/$referralId") }
        jsonPath("$.moreInfo") { isEmpty() }
      }
    }

    verify { referralService.getReferralById(referralId) }
  }

  @Test
  fun `getReferralById with invalid UUID returns 400 with error body`() {
    val referralId = "bad-id"

    mockMvc.get("/referrals/$referralId") {
      accept = MediaType.APPLICATION_JSON
      header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
    }.andExpect {
      status { isBadRequest() }
      content {
        contentType(MediaType.APPLICATION_JSON)
        jsonPath("$.status") { value(400) }
        jsonPath("$.errorCode") { isEmpty() }
        jsonPath("$.userMessage") { prefix("Request not readable: Failed to convert value of type 'java.lang.String' to required type 'java.util.UUID'; Invalid UUID string: bad-id") }
        jsonPath("$.developerMessage") { prefix("Failed to convert value of type 'java.lang.String' to required type 'java.util.UUID'; Invalid UUID string: bad-id") }
        jsonPath("$.moreInfo") { isEmpty() }
      }
    }
  }

  @Test
  fun `updateReferralStatusById with valid transition returns 204 with no body`() {
    val referral = ReferralEntityFactory().produce()

    val payload = mapOf(
      "status" to "referral_submitted",
    )

    every { referralService.updateReferralStatusById(any(), any()) } just Runs

    mockMvc.put("/referrals/${referral.id}/status") {
      contentType = MediaType.APPLICATION_JSON
      header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      content = jacksonObjectMapper().writeValueAsString(payload)
    }.andExpect {
      status { isNoContent() }
    }

    verify { referralService.updateReferralStatusById(referral.id!!, REFERRAL_SUBMITTED) }
  }

  @Test
  fun `updateReferralStatusById with invalid transition returns 409 with no body`() {
    val referral = ReferralEntityFactory().produce()

    val payload = mapOf(
      "status" to "awaiting_assessment",
    )

    val illegalArgumentException =
      IllegalArgumentException("Transition from $REFERRAL_STARTED to $AWAITING_ASSESSMENT is not valid")

    every { referralService.updateReferralStatusById(any(), any()) } throws illegalArgumentException

    mockMvc.put("/referrals/${referral.id}/status") {
      contentType = MediaType.APPLICATION_JSON
      header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      content = jacksonObjectMapper().writeValueAsString(payload)
    }.andExpect {
      status { isConflict() }
    }

    verify { referralService.updateReferralStatusById(referral.id!!, AWAITING_ASSESSMENT) }
  }

  @Test
  fun `submitReferralById with valid ID returns 204 with no body`() {
    val referral = ReferralEntityFactory()
      .withOffering(
        OfferingEntityFactory()
          .withId(UUID.randomUUID())
          .produce(),
      )
      .withPrisonNumber(randomPrisonNumber())
      .withReferrerId(randomReferrerId())
      .withAdditionalInformation("Additional Info")
      .withOasysConfirmed(true)
      .withHasReviewedProgrammeHistory(true)
      .produce()

    every { referralService.getReferralById(referral.id!!) } returns referral
    every { referralService.submitReferralById(referral.id!!) } just Runs

    mockMvc.post("/referrals/${referral.id}/submit") {
      header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
    }.andExpect {
      status { isNoContent() }
    }

    verify { referralService.getReferralById(referral.id!!) }
    verify { referralService.submitReferralById(referral.id!!) }
  }

  @Test
  fun `submitReferralById with invalid ID returns 404 with error body`() {
    val invalidReferralId = UUID.randomUUID()

    every { referralService.getReferralById(invalidReferralId) } returns null

    mockMvc.post("/referrals/$invalidReferralId/submit") {
      header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
    }.andExpect {
      status { isNotFound() }
    }

    verify { referralService.getReferralById(invalidReferralId) }
  }

  @Test
  fun `submitReferralById with incomplete referral returns 400 with error body`() {
    val referral = ReferralEntityFactory()
      .withOffering(
        OfferingEntityFactory()
          .withId(UUID.randomUUID())
          .produce(),
      )
      .withPrisonNumber(randomPrisonNumber())
      .withReferrerId(randomReferrerId())
      .produce()

    every { referralService.getReferralById(referral.id!!) } returns referral
    every { referralService.submitReferralById(referral.id!!) } throws ValidationException("additionalInformation is not valid: null")

    mockMvc.post("/referrals/${referral.id}/submit") {
      header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
    }.andExpect {
      status { isBadRequest() }
    }

    verify { referralService.getReferralById(referral.id!!) }
    verify { referralService.submitReferralById(referral.id!!) }
  }

  @Test
  fun `getReferralsByOrganisationId with valid organisationId returns 200 with paginated body`() {
    val organisationId = "MDI"
    val pageable = PageRequest.of(0, 10, Sort.by("referralId"))

    val firstReferralId = UUID.randomUUID()
    val audiencesForFirstReferral = listOf("Audience 1", "Audience 2", "Audience 3")
    val projectionsForFirstReferral = audiencesForFirstReferral.map { audience ->
      ReferralSummaryProjectionFactory()
        .withReferralId(firstReferralId)
        .withCourseName("Course name")
        .withAudience(audience)
        .withStatus(ReferralEntity.ReferralStatus.REFERRAL_STARTED)
        .withPrisonNumber(PRISON_NUMBER)
        .produce()
    }

    val secondReferralId = UUID.randomUUID()
    val audiencesForSecondReferral = listOf("Audience 4", "Audience 5", "Audience 6")
    val projectionsForSecondReferral = audiencesForSecondReferral.map { audience ->
      ReferralSummaryProjectionFactory()
        .withReferralId(secondReferralId)
        .withCourseName("Another course name")
        .withAudience(audience)
        .withStatus(ReferralEntity.ReferralStatus.REFERRAL_SUBMITTED)
        .withSubmittedOn(LocalDateTime.MIN)
        .withPrisonNumber(PRISON_NUMBER)
        .produce()
    }

    every { referralService.getReferralsByOrganisationId(organisationId, pageable, null, null, null) } returns
      PageImpl(projectionsForFirstReferral.toApi() + projectionsForSecondReferral.toApi(), pageable, 2L)

    val mvcResult = mockMvc.get("/referrals/organisation/$organisationId/dashboard") {
      param("page", "0")
      param("size", "10")
      header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      accept = MediaType.APPLICATION_JSON
    }.andExpect {
      status { isOk() }
      content { contentType(MediaType.APPLICATION_JSON) }
    }.andReturn()

    val jsonResponse = mvcResult.response.contentAsString
    val paginatedReferralSummary = jacksonObjectMapper()
      .readValue(jsonResponse, PaginatedReferralSummary::class.java)

    paginatedReferralSummary.pageSize shouldBe 10
    paginatedReferralSummary.totalPages shouldBe 1
    paginatedReferralSummary.content?.shouldHaveSize(2)
    paginatedReferralSummary.totalElements shouldBe 2L

    val firstReferral = paginatedReferralSummary.content?.find { it.id == firstReferralId }
    firstReferral shouldNotBe null
    firstReferral?.let { referral ->
      referral.courseName shouldBe "Course name"
      referral.audiences shouldContainExactlyInAnyOrder audiencesForFirstReferral
      referral.status shouldBe ReferralStatus.referralStarted
      referral.prisonNumber shouldBe PRISON_NUMBER
    }

    val secondReferral = paginatedReferralSummary.content?.find { it.id == secondReferralId }
    secondReferral shouldNotBe null
    secondReferral?.let { referral ->
      referral.courseName shouldBe "Another course name"
      referral.audiences shouldContainExactlyInAnyOrder audiencesForSecondReferral
      referral.status shouldBe ReferralStatus.referralSubmitted
      referral.submittedOn shouldBe LocalDateTime.MIN.toString()
      referral.prisonNumber shouldBe PRISON_NUMBER
    }

    verify { referralService.getReferralsByOrganisationId(organisationId, pageable, null, null, null) }
  }

  @Test
  fun `getReferralsByOrganisationId with valid organisationId and custom pagination count will return 200 with paginated body for each page`() {
    val organisationId = "MDI"
    val pageSize = 1
    val pageableFirstPage = PageRequest.of(0, pageSize, Sort.by("referralId"))
    val pageableSecondPage = PageRequest.of(1, pageSize, Sort.by("referralId"))

    val firstReferralId = UUID.randomUUID()
    val audiencesForFirstReferral = listOf("Audience 1", "Audience 2", "Audience 3")
    val projectionsForFirstReferral = audiencesForFirstReferral.map { audience ->
      ReferralSummaryProjectionFactory()
        .withReferralId(firstReferralId)
        .withCourseName("Course name")
        .withAudience(audience)
        .withStatus(ReferralEntity.ReferralStatus.REFERRAL_STARTED)
        .withPrisonNumber(PRISON_NUMBER)
        .produce()
    }

    val secondReferralId = UUID.randomUUID()
    val audiencesForSecondReferral = listOf("Audience 4", "Audience 5", "Audience 6")
    val projectionsForSecondReferral = audiencesForSecondReferral.map { audience ->
      ReferralSummaryProjectionFactory()
        .withReferralId(secondReferralId)
        .withCourseName("Another course name")
        .withAudience(audience)
        .withStatus(ReferralEntity.ReferralStatus.REFERRAL_SUBMITTED)
        .withSubmittedOn(LocalDateTime.MIN)
        .withPrisonNumber(PRISON_NUMBER)
        .produce()
    }

    every { referralService.getReferralsByOrganisationId(organisationId, pageableFirstPage, null, null, null) } returns
      PageImpl(projectionsForFirstReferral.toApi(), pageableFirstPage, 2)

    every { referralService.getReferralsByOrganisationId(organisationId, pageableSecondPage, null, null, null) } returns
      PageImpl(projectionsForSecondReferral.toApi(), pageableSecondPage, 2)

    val firstPageResult = mockMvc.get("/referrals/organisation/$organisationId/dashboard") {
      param("page", "0")
      param("size", pageSize.toString())
      header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      accept = MediaType.APPLICATION_JSON
    }.andExpect {
      status { isOk() }
      content { contentType(MediaType.APPLICATION_JSON) }
    }.andReturn()

    val firstPageResponse = jacksonObjectMapper()
      .readValue(firstPageResult.response.contentAsString, PaginatedReferralSummary::class.java)

    firstPageResponse.pageSize shouldBe 1
    firstPageResponse.totalPages shouldBe 2
    firstPageResponse.content?.shouldHaveSize(1)
    firstPageResponse.totalElements shouldBe 2

    firstPageResponse.content?.find { it.id == firstReferralId }?.let { referral ->
      referral.courseName shouldBe "Course name"
      referral.audiences shouldContainExactlyInAnyOrder audiencesForFirstReferral
      referral.status shouldBe ReferralStatus.referralStarted
      referral.prisonNumber shouldBe PRISON_NUMBER
    }

    val secondPageResult = mockMvc.get("/referrals/organisation/$organisationId/dashboard") {
      param("page", "1")
      param("size", pageSize.toString())
      header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      accept = MediaType.APPLICATION_JSON
    }.andExpect {
      status { isOk() }
      content { contentType(MediaType.APPLICATION_JSON) }
    }.andReturn()

    val secondPageResponse = jacksonObjectMapper()
      .readValue(secondPageResult.response.contentAsString, PaginatedReferralSummary::class.java)

    secondPageResponse.pageSize shouldBe 1
    secondPageResponse.totalPages shouldBe 2
    secondPageResponse.content?.shouldHaveSize(1)
    secondPageResponse.totalElements shouldBe 2

    secondPageResponse.content?.find { it.id == secondReferralId }?.let { referral ->
      referral.courseName shouldBe "Another course name"
      referral.audiences shouldContainExactlyInAnyOrder audiencesForSecondReferral
      referral.status shouldBe ReferralStatus.referralSubmitted
      referral.submittedOn shouldBe LocalDateTime.MIN.toString()
      referral.prisonNumber shouldBe PRISON_NUMBER
    }

    verify(exactly = 1) { referralService.getReferralsByOrganisationId(organisationId, pageableFirstPage, null, null, null) }
    verify(exactly = 1) { referralService.getReferralsByOrganisationId(organisationId, pageableSecondPage, null, null, null) }
  }

  @ParameterizedTest
  @MethodSource("parametersForGetReferralsByOrganisationIdWithFiltering")
  fun `getReferralsByOrganisationId with valid organisationId and filtering will return 200 with paginated body`(
    statusFilter: String?,
    audienceFilter: String?,
    courseNameFilter: String?,
    expectedReferralSummaries: List<ReferralSummary>,
  ) {
    val organisationId = "MDI"
    val pageable = PageRequest.of(0, 10, Sort.by("referralId"))

    every { referralService.getReferralsByOrganisationId(organisationId, pageable, statusFilter, audienceFilter, courseNameFilter) } returns
      PageImpl(expectedReferralSummaries, pageable, 1)

    val result = mockMvc.get("/referrals/organisation/$organisationId/dashboard") {
      param("page", "0")
      param("size", "10")
      statusFilter?.let { param("status", it) }
      audienceFilter?.let { param("audience", it) }
      courseNameFilter?.let { param("courseName", it) }
      header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      accept = MediaType.APPLICATION_JSON
    }.andExpect {
      status { isOk() }
      content { contentType(MediaType.APPLICATION_JSON) }
    }.andReturn()

    val response = jacksonObjectMapper()
      .readValue(result.response.contentAsString, PaginatedReferralSummary::class.java)

    response.content?.shouldContainExactlyInAnyOrder(expectedReferralSummaries)

    verify(exactly = 1) {
      referralService.getReferralsByOrganisationId(organisationId, pageable, statusFilter, audienceFilter, courseNameFilter)
    }
  }

  @Test
  fun `getReferralsByOrganisationId without JWT returns 401`() {
    mockMvc.get("/referrals/organisation/${UUID.randomUUID()}/dashboard") {
      accept = MediaType.APPLICATION_JSON
    }.andExpect {
      status { isUnauthorized() }
    }
  }

  @Test
  fun `getReferralsByOrganisationId with random organisationId returns 200 with paginated empty body`() {
    val orgId = UUID.randomUUID().toString()
    val pageable = PageRequest.of(0, 10, Sort.by("referralId"))

    every { referralService.getReferralsByOrganisationId(orgId, pageable, null, null, null) } returns
      PageImpl(emptyList(), pageable, 0)

    mockMvc.get("/referrals/organisation/$orgId/dashboard") {
      param("page", "0")
      param("size", "10")
      accept = MediaType.APPLICATION_JSON
      header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
    }.andExpect {
      status { isOk() }
      content {
        contentType(MediaType.APPLICATION_JSON)
        jsonPath("$.pageSize") { value(10) }
        jsonPath("$.totalElements") { value(0) }
        jsonPath("$.totalPages") { value(0) }
        jsonPath("$.pageNumber") { value(0) }
        jsonPath("$.pageIsEmpty") { value(true) }
      }
    }

    verify { referralService.getReferralsByOrganisationId(orgId, pageable, null, null, null) }
  }
}
