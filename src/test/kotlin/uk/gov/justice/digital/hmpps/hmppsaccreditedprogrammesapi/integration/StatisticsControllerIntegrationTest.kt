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
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.type.ReferralStatus
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

  val courseId1 = UUID.randomUUID()
  val courseId2 = UUID.randomUUID()
  val offeringId1 = UUID.randomUUID()
  val offeringId2 = UUID.randomUUID()

  @BeforeEach
  fun setUp() {
    persistenceHelper.clearAllTableContent()
    persistenceHelper.createOrganisation(code = "BWN", name = "BWN org")
    persistenceHelper.createEnabledOrganisation("BWN", "BWN org")
    persistenceHelper.createOrganisation(code = "MDI", name = "MDI org")
    persistenceHelper.createEnabledOrganisation("MDI", "MDI org")

    persistenceHelper.createCourse(
      courseId1,
      "SC",
      "Super Course",
      "Sample description",
      "SC++",
      "General offence",
    )

    persistenceHelper.createCourse(
      courseId2,
      "CC",
      "Custom Course",
      "Sample description",
      "CC",
      "Custom offence",
    )
    persistenceHelper.createCourse(
      UUID.fromString("1811faa6-d568-4fc4-83ce-41118b90242e"),
      "RC",
      "RAPID Course",
      "Sample description",
      "RC",
      "General offence",
    )

    persistenceHelper.createOffering(
      offeringId1,
      courseId1,
      "MDI",
      "nobody-mdi@digital.justice.gov.uk",
      "nobody2-mdi@digital.justice.gov.uk",
      true,
    )
    persistenceHelper.createOffering(
      offeringId2,
      courseId2,
      "MDI",
      "nobody-bwn@digital.justice.gov.uk",
      "nobody2-bwn@digital.justice.gov.uk",
      true,
    )
  }

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
    referral.status.shouldBe("on_programme")

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
  fun `should get referral count statistics for on programme report where course is provided`() {
    // Given
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())
    val createdReferral1  = createReferral(offeringId1, PRISON_NUMBER_1)
    progressReferralStatusToOnProgramme(createdReferral1.id)
    val createdReferral2  = createReferral(offeringId2, PRISON_NUMBER_1)
    progressReferralStatusToOnProgramme(createdReferral2.id)

    // When
    val reportContent = getStatistics(
      reportType = "ON_PROGRAMME_COUNT",
      startDate = "2023-06-01",
      locationCodes = ORGANISATION_ID_MDI,
      courseId = courseId1,
    )

    // Then
    reportContent.reportType shouldBe ReportType.ON_PROGRAMME_COUNT.toString()
    reportContent.content.count shouldBe 1
    reportContent.content.courseCounts?.get(0)?.name shouldBe "Super Course"
    reportContent.content.courseCounts?.get(0)?.audience shouldBe "General offence"
    reportContent.content.courseCounts?.get(0)?.count shouldBe 1
    reportContent.parameters.courseId shouldBe courseId1
  }

  @Test
  fun `should get programme complete count statistics for all courses when no course id is provided`() {
    // Given
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())
    val createdReferral = createReferral(prisonNumber = PRISON_NUMBER_1)
    createdReferral.shouldNotBeNull()
    progressReferralStatusToOnProgramme(createdReferral.id)
    progressReferralStatusToStatus(createdReferral.id, PROGRAMME_COMPLETE)

    // When
    val reportContent = getStatistics(
      reportType = "PROGRAMME_COMPLETE_COUNT",
      startDate = "2023-06-01",
      locationCodes = ORGANISATION_ID_MDI,
    )

    // Then
    reportContent.content.count shouldBe 1
    reportContent.content.courseCounts?.first()?.name shouldBe "Super Course"
    reportContent.content.courseCounts?.first()?.audience shouldBe "General offence"
    reportContent.parameters.courseId shouldBe null
    reportContent.reportType shouldBe ReportType.PROGRAMME_COMPLETE_COUNT.toString()
  }

  @Test
  fun `should get programme complete count statistics for a specific course when course id is provided`() {
    // Given
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())
    // create first referral with course 1 and offering 1
    val createdReferral1  = createReferral(offeringId1, PRISON_NUMBER_1)
    progressReferralStatusToOnProgramme(createdReferral1.id)
    progressReferralStatusToStatus(createdReferral1.id, PROGRAMME_COMPLETE)
    // create second referral with course 2 and offering 2
    val createdReferral2  = createReferral(offeringId2, PRISON_NUMBER_1)
    progressReferralStatusToOnProgramme(createdReferral2.id)
    progressReferralStatusToStatus(createdReferral2.id, PROGRAMME_COMPLETE)

    // When
    val reportContent = getStatistics(
      reportType = "PROGRAMME_COMPLETE_COUNT",
      startDate = "2023-06-01",
      locationCodes = ORGANISATION_ID_MDI,
      courseId = courseId2,
    )

    // Then
    reportContent.content.count shouldBe 1
    reportContent.content.courseCounts?.first()?.name shouldBe "Custom Course"
    reportContent.content.courseCounts?.first()?.audience shouldBe "Custom offence"
    reportContent.content.courseCounts?.first()?.count shouldBe 1
    reportContent.parameters.courseId shouldBe courseId2
    reportContent.reportType shouldBe ReportType.PROGRAMME_COMPLETE_COUNT.toString()
  }

  @Test
  fun `should get withdrawn count statistics for all courses when no course id is provided`() {
    // Given
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())
    val createdReferral1  = createReferral(offeringId1, PRISON_NUMBER_1)
    progressReferralStatusToStatus(createdReferral1.id, ReferralStatus.REFERRAL_SUBMITTED.name)
    progressReferralStatusToStatus(createdReferral1.id, ReferralStatus.WITHDRAWN.name)
    val createdReferral2  = createReferral(offeringId2, PRISON_NUMBER_1)
    progressReferralStatusToStatus(createdReferral2.id, ReferralStatus.REFERRAL_SUBMITTED.name)
    progressReferralStatusToStatus(createdReferral2.id, ReferralStatus.WITHDRAWN.name)

    // When
    val reportContent = getStatistics(
      reportType = ReportType.WITHDRAWN_COUNT.toString(),
      startDate = "2023-06-01",
      locationCodes = ORGANISATION_ID_MDI,
    )
    // Then
    reportContent.content.count shouldBe 2
    reportContent.content.courseCounts?.get(0)?.name shouldBe "Custom Course"
    reportContent.content.courseCounts?.get(0)?.audience shouldBe "Custom offence"
    reportContent.content.courseCounts?.get(0)?.count shouldBe 1
    reportContent.content.courseCounts?.get(1)?.name shouldBe "Super Course"
    reportContent.content.courseCounts?.get(1)?.audience shouldBe "General offence"
    reportContent.content.courseCounts?.get(1)?.count shouldBe 1
    reportContent.parameters.courseId shouldBe null
    reportContent.reportType shouldBe ReportType.WITHDRAWN_COUNT.toString()
  }

  @Test
  fun `should get withdrawn count statistics for specific course when course id is provided`() {
    // Given
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())
    val createdReferral1  = createReferral(offeringId1, PRISON_NUMBER_1)
    progressReferralStatusToStatus(createdReferral1.id, ReferralStatus.REFERRAL_SUBMITTED.name)
    progressReferralStatusToStatus(createdReferral1.id, ReferralStatus.WITHDRAWN.name)
    val createdReferral2  = createReferral(offeringId2, PRISON_NUMBER_1)
    progressReferralStatusToStatus(createdReferral2.id, ReferralStatus.REFERRAL_SUBMITTED.name)
    progressReferralStatusToStatus(createdReferral2.id, ReferralStatus.WITHDRAWN.name)

    // When
    val reportContent = getStatistics(
      reportType = ReportType.WITHDRAWN_COUNT.toString(),
      startDate = "2023-06-01",
      locationCodes = ORGANISATION_ID_MDI,
      courseId = courseId2,
    )
    // Then
    reportContent.content.count shouldBe 1
    reportContent.content.courseCounts?.get(0)?.name shouldBe "Custom Course"
    reportContent.content.courseCounts?.get(0)?.audience shouldBe "Custom offence"
    reportContent.content.courseCounts?.get(0)?.count shouldBe 1
    reportContent.parameters.courseId shouldBe courseId2
    reportContent.reportType shouldBe ReportType.WITHDRAWN_COUNT.toString()
  }

  @Test
  fun `should get ineligible count statistics for all courses when no course id is provided`() {
    // Given
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())
    val createdReferral1  = createReferral(offeringId1, PRISON_NUMBER_1)
    progressReferralStatusToStatus(createdReferral1.id, ReferralStatus.REFERRAL_SUBMITTED.name)
    progressReferralStatusToStatus(createdReferral1.id, ReferralStatus.NOT_ELIGIBLE.name)
    val createdReferral2  = createReferral(offeringId2, PRISON_NUMBER_1)
    progressReferralStatusToStatus(createdReferral2.id, ReferralStatus.REFERRAL_SUBMITTED.name)
    progressReferralStatusToStatus(createdReferral2.id, ReferralStatus.NOT_ELIGIBLE.name)

    // When
    val reportContent = getStatistics(
      reportType = ReportType.NOT_ELIGIBLE_COUNT.toString(),
      startDate = "2023-06-01",
      locationCodes = ORGANISATION_ID_MDI,
    )
    // Then
    reportContent.content.count shouldBe 2
    reportContent.content.courseCounts?.get(0)?.name shouldBe "Custom Course"
    reportContent.content.courseCounts?.get(0)?.audience shouldBe "Custom offence"
    reportContent.content.courseCounts?.get(0)?.count shouldBe 1
    reportContent.content.courseCounts?.get(1)?.name shouldBe "Super Course"
    reportContent.content.courseCounts?.get(1)?.audience shouldBe "General offence"
    reportContent.content.courseCounts?.get(1)?.count shouldBe 1
    reportContent.parameters.courseId shouldBe null
    reportContent.reportType shouldBe ReportType.NOT_ELIGIBLE_COUNT.toString()
  }

  @Test
  fun `should get ineligible count statistics for specific courses when course id is provided`() {
    // Given
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())
    val createdReferral1  = createReferral(offeringId1, PRISON_NUMBER_1)
    progressReferralStatusToStatus(createdReferral1.id, ReferralStatus.REFERRAL_SUBMITTED.name)
    progressReferralStatusToStatus(createdReferral1.id, ReferralStatus.NOT_ELIGIBLE.name)
    val createdReferral2  = createReferral(offeringId2, PRISON_NUMBER_1)
    progressReferralStatusToStatus(createdReferral2.id, ReferralStatus.REFERRAL_SUBMITTED.name)
    progressReferralStatusToStatus(createdReferral2.id, ReferralStatus.NOT_ELIGIBLE.name)

    // When
    val reportContent = getStatistics(
      reportType = ReportType.NOT_ELIGIBLE_COUNT.toString(),
      startDate = "2023-06-01",
      locationCodes = ORGANISATION_ID_MDI,
      courseId = courseId2,
    )
    // Then
    reportContent.content.count shouldBe 1
    reportContent.content.courseCounts?.get(0)?.name shouldBe "Custom Course"
    reportContent.content.courseCounts?.get(0)?.audience shouldBe "Custom offence"
    reportContent.content.courseCounts?.get(0)?.count shouldBe 1
    reportContent.parameters.courseId shouldBe courseId2
    reportContent.reportType shouldBe ReportType.NOT_ELIGIBLE_COUNT.toString()
  }

  @Test
  fun `should get not suitable count statistics for all courses when no course id is provided`() {
    // Given
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())
    val createdReferral1  = createReferral(offeringId1, PRISON_NUMBER_1)
    progressReferralStatusToStatus(createdReferral1.id, ReferralStatus.REFERRAL_SUBMITTED.name)
    progressReferralStatusToStatus(createdReferral1.id, ReferralStatus.AWAITING_ASSESSMENT.name)
    progressReferralStatusToStatus(createdReferral1.id, ReferralStatus.ASSESSMENT_STARTED.name)
    progressReferralStatusToStatus(createdReferral1.id, ReferralStatus.NOT_SUITABLE.name)
    val createdReferral2  = createReferral(offeringId2, PRISON_NUMBER_1)
    progressReferralStatusToStatus(createdReferral2.id, ReferralStatus.REFERRAL_SUBMITTED.name)
    progressReferralStatusToStatus(createdReferral2.id, ReferralStatus.AWAITING_ASSESSMENT.name)
    progressReferralStatusToStatus(createdReferral2.id, ReferralStatus.ASSESSMENT_STARTED.name)
    progressReferralStatusToStatus(createdReferral2.id, ReferralStatus.NOT_SUITABLE.name)

    // When
    val reportContent = getStatistics(
      reportType = ReportType.NOT_SUITABLE_COUNT.toString(),
      startDate = "2023-06-01",
      locationCodes = ORGANISATION_ID_MDI,
    )

    // Then
    reportContent.content.count shouldBe 2
    reportContent.content.courseCounts?.get(0)?.name shouldBe "Custom Course"
    reportContent.content.courseCounts?.get(0)?.audience shouldBe "Custom offence"
    reportContent.content.courseCounts?.get(0)?.count shouldBe 1
    reportContent.content.courseCounts?.get(1)?.name shouldBe "Super Course"
    reportContent.content.courseCounts?.get(1)?.audience shouldBe "General offence"
    reportContent.content.courseCounts?.get(1)?.count shouldBe 1
    reportContent.parameters.courseId shouldBe null
    reportContent.reportType shouldBe ReportType.NOT_SUITABLE_COUNT.toString()
  }

  @Test
  fun `should get not suitable count statistics for specific course when course id is provided`() {
    // Given
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())
    val createdReferral1  = createReferral(offeringId1, PRISON_NUMBER_1)
    progressReferralStatusToStatus(createdReferral1.id, ReferralStatus.REFERRAL_SUBMITTED.name)
    progressReferralStatusToStatus(createdReferral1.id, ReferralStatus.AWAITING_ASSESSMENT.name)
    progressReferralStatusToStatus(createdReferral1.id, ReferralStatus.ASSESSMENT_STARTED.name)
    progressReferralStatusToStatus(createdReferral1.id, ReferralStatus.NOT_SUITABLE.name)
    val createdReferral2  = createReferral(offeringId2, PRISON_NUMBER_1)
    progressReferralStatusToStatus(createdReferral2.id, ReferralStatus.REFERRAL_SUBMITTED.name)
    progressReferralStatusToStatus(createdReferral2.id, ReferralStatus.AWAITING_ASSESSMENT.name)
    progressReferralStatusToStatus(createdReferral2.id, ReferralStatus.ASSESSMENT_STARTED.name)
    progressReferralStatusToStatus(createdReferral2.id, ReferralStatus.NOT_SUITABLE.name)

    // When
    val reportContent = getStatistics(
      reportType = ReportType.NOT_SUITABLE_COUNT.toString(),
      startDate = "2023-06-01",
      locationCodes = ORGANISATION_ID_MDI,
      courseId = courseId2,
    )

    // Then
    reportContent.content.count shouldBe 1
    reportContent.content.courseCounts?.get(0)?.name shouldBe "Custom Course"
    reportContent.content.courseCounts?.get(0)?.audience shouldBe "Custom offence"
    reportContent.content.courseCounts?.get(0)?.count shouldBe 1
    reportContent.parameters.courseId shouldBe courseId2
    reportContent.reportType shouldBe ReportType.NOT_SUITABLE_COUNT.toString()
  }

  @Test
  fun `should get deselected count statistics for all courses when no course id is provided`() {
    // Given
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())
    val createdReferral1  = createReferral(offeringId1, PRISON_NUMBER_1)
    progressReferralStatusToOnProgramme(createdReferral1.id)
    progressReferralStatusToStatus(createdReferral1.id, ReferralStatus.DESELECTED.name)
    val createdReferral2  = createReferral(offeringId2, PRISON_NUMBER_1)
    progressReferralStatusToOnProgramme(createdReferral2.id)
    progressReferralStatusToStatus(createdReferral2.id, ReferralStatus.DESELECTED.name)

    // When
    val reportContent = getStatistics(
      reportType = ReportType.DESELECTED_COUNT.toString(),
      startDate = "2023-06-01",
      locationCodes = ORGANISATION_ID_MDI,
    )

    // Then
    reportContent.content.count shouldBe 2
    reportContent.content.courseCounts?.get(0)?.name shouldBe "Custom Course"
    reportContent.content.courseCounts?.get(0)?.audience shouldBe "Custom offence"
    reportContent.content.courseCounts?.get(0)?.count shouldBe 1
    reportContent.content.courseCounts?.get(1)?.name shouldBe "Super Course"
    reportContent.content.courseCounts?.get(1)?.audience shouldBe "General offence"
    reportContent.content.courseCounts?.get(1)?.count shouldBe 1
    reportContent.parameters.courseId shouldBe null
    reportContent.reportType shouldBe ReportType.DESELECTED_COUNT.toString()
  }

  @Test
  fun `should get deselected count statistics for specific course when course id is provided`() {
    // Given
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())
    val createdReferral1  = createReferral(offeringId1, PRISON_NUMBER_1)
    progressReferralStatusToOnProgramme(createdReferral1.id)
    progressReferralStatusToStatus(createdReferral1.id, ReferralStatus.DESELECTED.name)
    val createdReferral2  = createReferral(offeringId2, PRISON_NUMBER_1)
    progressReferralStatusToOnProgramme(createdReferral2.id)
    progressReferralStatusToStatus(createdReferral2.id, ReferralStatus.DESELECTED.name)

    // When
    val reportContent = getStatistics(
      reportType = ReportType.DESELECTED_COUNT.toString(),
      startDate = "2023-06-01",
      locationCodes = ORGANISATION_ID_MDI,
      courseId = courseId2,
    )

    // Then
    reportContent.content.count shouldBe 1
    reportContent.content.courseCounts?.get(0)?.name shouldBe "Custom Course"
    reportContent.content.courseCounts?.get(0)?.audience shouldBe "Custom offence"
    reportContent.content.courseCounts?.get(0)?.count shouldBe 1
    reportContent.parameters.courseId shouldBe courseId2
    reportContent.reportType shouldBe ReportType.DESELECTED_COUNT.toString()
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

    // When
    val reportStatusesByProgrammeType = getReportStatusesByProgrammeType(courseId1, startDate = LocalDate.now(), endDate = LocalDate.now().plusDays(1), locations = listOf(ORGANISATION_ID_MDI))

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

  fun createReferral(prisonNumber: String = PRISON_NUMBER_1): Referral {
    return createReferral(offeringId1, prisonNumber)
  }

  fun getReportStatusesByProgrammeType(courseId: UUID, startDate: LocalDate, endDate: LocalDate, locations: List<String>?) =
    webTestClient
      .get()
      .uri("/statistics/report/count-by-status-for-programme?startDate=$startDate&endDate=$endDate&" + locations?.joinToString("&") { "locationCodes=$it" } + "&courseId=$courseId")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectBody<List<ReportStatusCount>>()
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

  fun getStatistics(reportType: String, locationCodes: String? = null, startDate: String, courseId: UUID? = null): ReportContent =
    webTestClient
      .get()
      .uri("/statistics/report/$reportType?startDate=$startDate&locationCodes=$locationCodes" + if (courseId != null) "&courseId=$courseId" else "")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody<ReportContent>()
      .returnResult().responseBody!!

  private fun updateReferralStatus(createdReferralId: UUID, referralStatusUpdate: ReferralStatusUpdate) =
    webTestClient
      .put()
      .uri("/referrals/$createdReferralId/status")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(referralStatusUpdate)
      .exchange().expectStatus().isNoContent
}
