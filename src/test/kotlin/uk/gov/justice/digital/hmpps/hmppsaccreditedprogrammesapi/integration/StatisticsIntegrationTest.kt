package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration

import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
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
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.ORGANISATION_ID_MDI
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.PRISON_NUMBER_1
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.REFERRAL_SUBMITTED
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralStatusHistoryRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.ReportContent
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.ReportType
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Referral
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ReferralCreate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ReferralStatusUpdate
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(JwtAuthHelper::class)
class StatisticsIntegrationTest : IntegrationTestBase() {

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
    val course = getAllCourses().first()
    val offering = getAllOfferingsForCourse(course.id).first()
    return createReferral(offering.id, prisonNumber)
  }

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

  fun getStatistics(reportType: String, locationCodes: String? = null, startDate: String): ReportContent =
    webTestClient
      .get()
      .uri("/statistics/report/$reportType?startDate=$startDate&locationCodes=$locationCodes")
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
