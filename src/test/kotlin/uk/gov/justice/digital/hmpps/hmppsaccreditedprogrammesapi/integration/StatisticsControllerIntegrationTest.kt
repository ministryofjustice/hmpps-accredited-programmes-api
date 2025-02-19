package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration

import au.com.dius.pact.core.support.isNotEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.config.JwtAuthHelper
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.DESELECTED
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.ORGANISATION_ID_MDI
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.PRISON_NUMBER_1
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.PROGRAMME_COMPLETE
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.REFERRAL_SUBMITTED
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.REFERRAL_WITHDRAWN
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralStatusHistoryRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.StatisticsRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.ReportContent
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.ReportType
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Referral
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ReferralCreate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ReferralStatusUpdate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ReportStatusCount
import java.time.LocalDate
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(JwtAuthHelper::class)
class StatisticsControllerIntegrationTest : IntegrationTestBase() {

  @Autowired
  lateinit var referralStatusHistoryRepository: ReferralStatusHistoryRepository

  @Autowired
  lateinit var statisticsRepository: StatisticsRepository

  val courseId1 = UUID.fromString("d3abc217-75ee-46e9-a010-368f30282367")
  val offeringId1 = UUID.fromString("7fffcc6a-11f8-4713-be35-cf5ff1aee517")

  @BeforeEach
  fun setUp() {
    persistenceHelper.clearAllTableContent()

    persistenceHelper.createCourse(
      courseId1,
      "SC",
      "Super Course",
      "Sample description",
      "SC++",
      "General offence",
    )
    persistenceHelper.createOrganisation(code = "BWN", name = "BWN org")
    persistenceHelper.createEnabledOrganisation("BWN", "BWN org")
    persistenceHelper.createOrganisation(code = "MDI", name = "MDI org")
    persistenceHelper.createEnabledOrganisation("MDI", "MDI org")

    persistenceHelper.createOffering(
      offeringId1,
      courseId1,
      "MDI",
      "nobody-mdi@digital.justice.gov.uk",
      "nobody2-mdi@digital.justice.gov.uk",
      true,
    )
    persistenceHelper.createOffering(
      UUID.fromString("790a2dfe-7de5-4504-bb9c-83e6e53a6537"),
      courseId1,
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

  fun createReferral(offeringId: UUID?, prisonNumber: String = PRISON_NUMBER_1) = webTestClient
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

  @Test
  fun `Get referral count statistics should return 200 with correct body`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())
    val createdReferral = createReferral(prisonNumber = PRISON_NUMBER_1)

    createdReferral.shouldNotBeNull()
    createdReferral.id.shouldNotBeNull()

    val reportContent = getStatistics(
      reportType = "REFERRAL_COUNT",
      startDate = "2023-06-01",
      locationCodes = ORGANISATION_ID_MDI,
    )

    reportContent.reportType shouldBe ReportType.REFERRAL_COUNT.toString()
  }

  @Test
  fun `should get referral count statistics for on programme report`() {
    // Given
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())
    val createdReferral = createReferral(prisonNumber = PRISON_NUMBER_1)
    createdReferral.shouldNotBeNull()

    progressReferralStatusToOnProgramme(createdReferral.id)

    val referral = getReferralById(createdReferral.id)
    referral.shouldNotBeNull()
    referral.status.shouldBe("on_programme") //

    val statusHistories =
      referralStatusHistoryRepository.getAllByReferralIdOrderByStatusStartDateDesc(referral.id)

    statusHistories shouldHaveSize 6
    statusHistories.first().status.code shouldBe "ON_PROGRAMME"

    // When
    val reportContent = getStatistics(
      reportType = "ON_PROGRAMME_COUNT",
      startDate = "2023-06-01",
      locationCodes = ORGANISATION_ID_MDI,
    )

    // Then
    reportContent.content.count shouldBe 1
    reportContent.reportType shouldBe ReportType.ON_PROGRAMME_COUNT.toString()
  }

  @Test
  fun `should return statistics by status`() {
    // Given
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())
    val createdReferral = createReferral(prisonNumber = PRISON_NUMBER_1)
    createdReferral.shouldNotBeNull()

    progressReferralStatusToOnProgramme(createdReferral.id)

    val referral = getReferralById(createdReferral.id)
    referral.shouldNotBeNull()
    referral.status.shouldBe("on_programme") //

    val referralCountByStatus =
      statisticsRepository.referralCountByStatus(
        LocalDate.now().minusDays(1),
        LocalDate.now(),
        listOf(),
      )

    referralCountByStatus.isNotEmpty()
  }

  @Test
  fun `should return referral status statistics for a given course id and location`() {
    // Given
    val createdReferral = createReferral(prisonNumber = PRISON_NUMBER_1)
    progressReferralStatusToOnProgramme(createdReferral.id)

    // When
    val reportStatusesByProgrammeType = getReportStatusesByProgrammeType(courseId1, startDate = LocalDate.now(), endDate = LocalDate.now().plusDays(1), locations = listOf(ORGANISATION_ID_MDI))

    // Then
    assertThat(reportStatusesByProgrammeType).isNotNull
      .hasSize(6)
      .extracting("count", "status", "organisationCode")
      .containsExactly(
        tuple(1L, "ASSESSED_SUITABLE", ORGANISATION_ID_MDI),
        tuple(1L, "ASSESSMENT_STARTED", ORGANISATION_ID_MDI),
        tuple(1L, "AWAITING_ASSESSMENT", ORGANISATION_ID_MDI),
        tuple(1L, "ON_PROGRAMME", ORGANISATION_ID_MDI),
        tuple(1L, "REFERRAL_STARTED", ORGANISATION_ID_MDI),
        tuple(1L, "REFERRAL_SUBMITTED", ORGANISATION_ID_MDI),
      )
  }

  @Test
  fun `should return referral status statistics for a given course id and ALL locations`() {
    // Given
    val createdReferral = createReferral(prisonNumber = PRISON_NUMBER_1)
    progressReferralStatusToOnProgramme(createdReferral.id)

    // When
    val reportStatusesByProgrammeType = getReportStatusesByProgrammeType(courseId1, startDate = LocalDate.now(), endDate = LocalDate.now().plusDays(1), locations = null)

    // Then
    assertThat(reportStatusesByProgrammeType).isNotNull
      .hasSize(6)
      .extracting("count", "status", "organisationCode")
      .containsExactly(
        tuple(1L, "ASSESSED_SUITABLE", ORGANISATION_ID_MDI),
        tuple(1L, "ASSESSMENT_STARTED", ORGANISATION_ID_MDI),
        tuple(1L, "AWAITING_ASSESSMENT", ORGANISATION_ID_MDI),
        tuple(1L, "ON_PROGRAMME", ORGANISATION_ID_MDI),
        tuple(1L, "REFERRAL_STARTED", ORGANISATION_ID_MDI),
        tuple(1L, "REFERRAL_SUBMITTED", ORGANISATION_ID_MDI),
      )
  }

  @Test
  fun `should return DESELECTED referral count for a given course id`() {
    // Given
    val createdReferral = createReferral(prisonNumber = PRISON_NUMBER_1)
    progressReferralStatusToOnProgramme(createdReferral.id)
    progressReferralStatusToStatus(createdReferral.id, DESELECTED)

    // When
    val reportStatusesByProgrammeType = getReportStatusesByProgrammeType(courseId1, startDate = LocalDate.now(), endDate = LocalDate.now().plusDays(1), locations = listOf(ORGANISATION_ID_MDI))

    // Then
    assertThat(reportStatusesByProgrammeType)
      .hasSize(7)
      .extracting("count", "status", "organisationCode")
      .contains(tuple(1L, DESELECTED, ORGANISATION_ID_MDI))
  }

  @Test
  fun `should return PROGRAMME_COMPLETE referral count for a given course id`() {
    // Given
    val createdReferral = createReferral(prisonNumber = PRISON_NUMBER_1)
    progressReferralStatusToOnProgramme(createdReferral.id)
    progressReferralStatusToStatus(createdReferral.id, PROGRAMME_COMPLETE)

    // When
    val reportStatusesByProgrammeType = getReportStatusesByProgrammeType(courseId1, startDate = LocalDate.now(), endDate = LocalDate.now().plusDays(1), locations = listOf(ORGANISATION_ID_MDI))

    // Then
    assertThat(reportStatusesByProgrammeType)
      .hasSize(7)
      .extracting("count", "status", "organisationCode")
      .contains(tuple(1L, PROGRAMME_COMPLETE, ORGANISATION_ID_MDI))
  }

  @Test
  fun `should return WITHDRAWN referral count for a given course id`() {
    // Given
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())
    val createdReferral = createReferral(prisonNumber = PRISON_NUMBER_1)
    createdReferral.shouldNotBeNull()

    progressReferralStatusToStatus(createdReferral.id, REFERRAL_SUBMITTED)
    progressReferralStatusToStatus(createdReferral.id, REFERRAL_WITHDRAWN)

    val courseId = UUID.fromString("d3abc217-75ee-46e9-a010-368f30282367")

    // When
    val reportStatusesByProgrammeType = getReportStatusesByProgrammeType(courseId, startDate = LocalDate.now(), endDate = LocalDate.now().plusDays(1), locations = listOf(ORGANISATION_ID_MDI))

    // Then
    assertThat(reportStatusesByProgrammeType)
      .hasSize(3)
      .extracting("count", "status", "organisationCode")
      .contains(tuple(1L, REFERRAL_WITHDRAWN, ORGANISATION_ID_MDI))
  }

  @Test
  fun `should return an empty list of referral statistics for an unknown course id`() {
    // Given
    val createdReferral = createReferral(prisonNumber = PRISON_NUMBER_1)

    progressReferralStatusToOnProgramme(createdReferral.id)
    val courseId = UUID.randomUUID()

    // When
    val reportStatusesByProgrammeType = getReportStatusesByProgrammeType(courseId, startDate = LocalDate.now(), endDate = LocalDate.now().plusDays(1), locations = listOf(ORGANISATION_ID_MDI))

    // Then
    assertThat(reportStatusesByProgrammeType).isEmpty()
  }

  private fun progressReferralStatusToStatus(referralId: UUID, status: String) {
    val referralStatusUpdate = ReferralStatusUpdate(
      status = status,
      ptUser = true,
    )
    updateReferralStatus(referralId, referralStatusUpdate)
  }

  private fun progressReferralStatusToOnProgramme(referralId: UUID) {
    val referralStatusUpdate1 = ReferralStatusUpdate(
      status = REFERRAL_SUBMITTED,
      ptUser = true,
    )
    updateReferralStatus(referralId, referralStatusUpdate1)

    val referralStatusUpdate2 = ReferralStatusUpdate(
      status = "AWAITING_ASSESSMENT",
      ptUser = true,
    )
    updateReferralStatus(referralId, referralStatusUpdate2)

    val referralStatusUpdate3 = ReferralStatusUpdate(
      status = "ASSESSMENT_STARTED",
      ptUser = true,
    )
    updateReferralStatus(referralId, referralStatusUpdate3)

    val referralStatusUpdate4 = ReferralStatusUpdate(
      status = "ASSESSED_SUITABLE",
      ptUser = true,
    )
    updateReferralStatus(referralId, referralStatusUpdate4)

    val referralStatusUpdate5 = ReferralStatusUpdate(
      status = "ON_PROGRAMME",
      ptUser = true,
    )
    updateReferralStatus(referralId, referralStatusUpdate5)
  }

  fun createReferral(prisonNumber: String = PRISON_NUMBER_1): Referral = createReferral(offeringId1, prisonNumber)

  fun getReportStatusesByProgrammeType(courseId: UUID, startDate: LocalDate, endDate: LocalDate, locations: List<String>?) = webTestClient
    .get()
    .uri("/statistics/report/count-by-status-for-programme?startDate=$startDate&endDate=$endDate&" + locations?.joinToString("&") { "locationCodes=$it" } + "&courseId=$courseId")
    .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
    .accept(MediaType.APPLICATION_JSON)
    .exchange()
    .expectBody<List<ReportStatusCount>>()
    .returnResult().responseBody!!

  fun getReferralById(createdReferralId: UUID) = webTestClient
    .get()
    .uri("/referrals/$createdReferralId")
    .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
    .accept(MediaType.APPLICATION_JSON)
    .exchange()
    .expectStatus().isOk
    .expectBody<Referral>()
    .returnResult().responseBody!!

  fun getStatistics(reportType: String, locationCodes: String? = null, startDate: String): ReportContent = webTestClient
    .get()
    .uri("/statistics/report/$reportType?startDate=$startDate&locationCodes=$locationCodes")
    .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
    .accept(MediaType.APPLICATION_JSON)
    .exchange()
    .expectStatus().isOk
    .expectBody<ReportContent>()
    .returnResult().responseBody!!

  private fun updateReferralStatus(createdReferralId: UUID, referralStatusUpdate: ReferralStatusUpdate) = webTestClient
    .put()
    .uri("/referrals/$createdReferralId/status")
    .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
    .contentType(MediaType.APPLICATION_JSON)
    .bodyValue(referralStatusUpdate)
    .exchange().expectStatus().isNoContent
}
