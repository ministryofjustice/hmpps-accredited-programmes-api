package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.config.JwtAuthHelper
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.COURSE_ID
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.COURSE_NAME
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.PRISON_NUMBER_1
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Referral
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ReferralCreate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.PersonService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.ReferralService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.StaffService
import java.util.UUID

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(JwtAuthHelper::class)
class AdminControllerIntegrationTest : IntegrationTestBase() {

  @Autowired
  private lateinit var personService: PersonService

  @Autowired
  private lateinit var referralService: ReferralService

  @Autowired
  private lateinit var staffService: StaffService

  private val offeringId1 = UUID.fromString("7fffcc6a-11f8-4713-be35-cf5ff1aee517")
  private val offeringId2 = UUID.fromString("790a2dfe-7de5-4504-bb9c-83e6e53a6537")

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
    persistenceHelper.createOrganisation(code = "MDI", name = "MDI org")

    persistenceHelper.createOffering(
      offeringId1,
      COURSE_ID,
      "MDI",
      "nobody-mdi@digital.justice.gov.uk",
      "nobody2-mdi@digital.justice.gov.uk",
      true,
    )
    persistenceHelper.createOffering(
      offeringId2,
      UUID.fromString("d3abc217-75ee-46e9-a010-368f30282367"),
      "BWN",
      "nobody-bwn@digital.justice.gov.uk",
      "nobody2-bwn@digital.justice.gov.uk",
      true,
    )

    persistenceHelper.createReferrerUser("ACP_TEST")
  }

  @Test
  fun `delete all ACP test referrals successful`() {
    val referralCreated1 = createReferral(offeringId1)
    persistenceHelper.updateReferralWithUsername(referralCreated1.id, "ACP_TEST")

    val referralCreated2 = createReferral(offeringId2)
    persistenceHelper.updateReferralWithUsername(referralCreated2.id, "ACP_TEST")

    deleteTestReferrals()

    persistenceHelper.getReferralById(referralCreated1.id) shouldBe 0
    persistenceHelper.getReferralById(referralCreated2.id) shouldBe 0
  }

  fun getReferralById(createdReferralId: UUID) = performRequestAndExpectOk(HttpMethod.GET, "/referrals/$createdReferralId", referralTypeReference())

  fun deleteTestReferrals() {
    webTestClient
      .delete()
      .uri("/admin/cleanUpTestReferrals")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .exchange()
      .expectStatus().isNoContent
  }

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
}
