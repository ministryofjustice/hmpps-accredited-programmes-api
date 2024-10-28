package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration

import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.nulls.shouldNotBeNull
import org.junit.jupiter.api.AfterEach
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
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.PRISON_NUMBER_1
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralStatusHistoryEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralStatusHistoryRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferrerUserEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.ReferralStatusCategoryRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.ReferralStatusReasonRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.ReferralStatusRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.ReferrerUserRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Referral
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ReferralCreate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ReferralStatusHistory
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

private const val WITHDRAWN = "WITHDRAWN"
private const val REFERRAL_STARTED = "REFERRAL_STARTED"
private const val CATEGORY_ADMIN = "W_ADMIN"
private const val REASON_DUPLICATE = "W_DUPLICATE"

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(JwtAuthHelper::class)
class StatusHistoryIntegrationTest : IntegrationTestBase() {

  @Autowired
  lateinit var referralStatusHistoryRepository: ReferralStatusHistoryRepository

  @Autowired
  lateinit var referralStatusRepository: ReferralStatusRepository

  @Autowired
  lateinit var referralStatusCategoryRepository: ReferralStatusCategoryRepository

  @Autowired
  lateinit var referrerUserRepository: ReferrerUserRepository

  @Autowired
  lateinit var referralStatusReasonRepository: ReferralStatusReasonRepository

  lateinit var referralId: UUID

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
    persistenceHelper.createdOrganisation(code = "BWN", name = "BWN org")
    persistenceHelper.createEnabledOrganisation("BWN", "BWN org")
    persistenceHelper.createdOrganisation(code = "MDI", name = "MDI org")
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
    // Create some test data.
    val withdrawnRefData = referralStatusRepository.findByCode(WITHDRAWN) ?: throw NotFoundException("no status with code: $WITHDRAWN found")
    val startedRefData = referralStatusRepository.findByCode(REFERRAL_STARTED) ?: throw NotFoundException("no status with code: $REFERRAL_STARTED found")

    val categoryRefData = referralStatusCategoryRepository.getAllByReferralStatusCodeAndActiveIsTrue(WITHDRAWN)[0]
    val reasonRefData = referralStatusReasonRepository.getAllByReferralStatusCategoryCodeAndActiveIsTrue(categoryRefData.code, false)[0]

    val startDateOfFirst = LocalDateTime.now().minusDays(3)
    val startDateOfSecond = LocalDateTime.now().minusDays(2)
    val duration = ChronoUnit.MILLIS.between(startDateOfSecond, startDateOfFirst)

    val course = getAllCourses().first()
    val offering = getAllOfferingsForCourse(course.id).first()
    val referralCreated = createReferral(offering.id!!, PRISON_NUMBER_1)
    referralId = referralCreated.id

    referrerUserRepository.save(ReferrerUserEntity("UNKNOWN_USER"))

    val first = ReferralStatusHistoryEntity(
      referralId = referralId,
      status = startedRefData,
      statusStartDate = startDateOfFirst,
      statusEndDate = startDateOfSecond,
    )
    val second = ReferralStatusHistoryEntity(
      referralId = referralId,
      status = withdrawnRefData,
      category = categoryRefData,
      reason = reasonRefData,
      notes = "Referral withdrawn",
      statusStartDate = startDateOfSecond,
      previousStatus = first.status,
      durationAtThisStatus = duration,
    )
    referralStatusHistoryRepository.saveAll(listOf(first, second))
  }

  @AfterEach
  fun clearDown() {
    referralStatusHistoryRepository.deleteAll()
  }

  @Test
  fun `get referral status history for a referral`() {
    // Given
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())
    // When
    val referralStatusHistories = getStatusHistories(referralId)
    // Then
    referralStatusHistories.shouldNotBeNull()
    referralStatusHistories.size shouldBeEqual 1
    val statusHistoryOne = referralStatusHistories[0]
    statusHistoryOne.status?.shouldBeEqual(WITHDRAWN)
    statusHistoryOne.previousStatus?.shouldBeEqual(REFERRAL_STARTED)
    statusHistoryOne.categoryDescription?.shouldBeEqual("Administrative error")
    statusHistoryOne.reasonDescription?.shouldBeEqual("Duplicate referral")
  }

  fun getStatusHistories(id: UUID?) =
    webTestClient
      .get()
      .uri("/referrals/$id/status-history")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody<List<ReferralStatusHistory>>()
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
}
