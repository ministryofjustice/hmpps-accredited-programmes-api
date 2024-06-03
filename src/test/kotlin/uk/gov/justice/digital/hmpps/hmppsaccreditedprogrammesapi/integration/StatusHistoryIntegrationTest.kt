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
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralStatusHistory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.config.JwtAuthHelper
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralStatusHistoryEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralStatusHistoryRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.ReferralStatusCategoryRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.ReferralStatusReasonRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.ReferralStatusRepository
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
  lateinit var referralStatusReasonRepository: ReferralStatusReasonRepository

  private val referralUUID = UUID.randomUUID()

  @BeforeEach
  fun setUp() {
    // Create some test data.
    val withdrawnRefData = referralStatusRepository.findByCode(WITHDRAWN) ?: throw NotFoundException("no status with code: $WITHDRAWN found")
    val startedRefData = referralStatusRepository.findByCode(REFERRAL_STARTED) ?: throw NotFoundException("no status with code: $REFERRAL_STARTED found")

    val categoryRefData = referralStatusCategoryRepository.getAllByReferralStatusCodeAndActiveIsTrue(WITHDRAWN)[0]
    val reasonRefData = referralStatusReasonRepository.getAllByReferralStatusCategoryCodeAndActiveIsTrue(categoryRefData.code, false)[0]

    val startDateOfFirst = LocalDateTime.now().minusDays(3)
    val startDateOfSecond = LocalDateTime.now().minusDays(2)
    val duration = ChronoUnit.MILLIS.between(startDateOfSecond, startDateOfFirst)

    val first = ReferralStatusHistoryEntity(
      referralId = referralUUID,
      status = startedRefData,
      statusStartDate = startDateOfFirst,
      statusEndDate = startDateOfSecond,
    )
    val second = ReferralStatusHistoryEntity(
      referralId = referralUUID,
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
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())

    val response = getStatusHistories(referralUUID)
    response.shouldNotBeNull()

    response.size shouldBeEqual 1
    val statusHistoryOne = response[0]
    statusHistoryOne.status?.shouldBeEqual(WITHDRAWN)
    statusHistoryOne.previousStatus?.shouldBeEqual(REFERRAL_STARTED)
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
}
