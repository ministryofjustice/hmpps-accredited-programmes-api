package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration

import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.ActiveProfiles
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.config.JwtAuthHelper
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.REFERRAL_WITHDRAWN_COLOUR
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.REFERRAL_WITHDRAWN_DESCRIPTION
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.REFERRAL_WITHDRAWN_HINT
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.type.SexualOffenceCategoryType
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ReferralStatusCategory
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ReferralStatusReason
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ReferralStatusRefData
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ReferralStatusType
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.SexualOffenceDetails
import java.util.UUID

private const val WITHDRAWN = "WITHDRAWN"
private const val CATEGORY_ADMIN = "W_ADMIN"
private const val REASON_DUPLICATE = "W_DUPLICATE"

private const val DESELECTED = "DESELECTED"
private const val PERSONAL = "D_PERSONAL"

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(JwtAuthHelper::class)
class ReferenceDataControllerIntegrationTest : IntegrationTestBase() {

  val withdrawnStatusExpected = ReferralStatusRefData(
    code = WITHDRAWN,
    description = REFERRAL_WITHDRAWN_DESCRIPTION,
    colour = REFERRAL_WITHDRAWN_COLOUR,
    hintText = REFERRAL_WITHDRAWN_HINT,
    hasNotes = true,
    hasConfirmation = false,
    draft = false,
    closed = true,
    hold = false,
    release = false,
    notesOptional = false,
  )
  val categoryExpected =
    ReferralStatusCategory(code = CATEGORY_ADMIN, description = "Administrative error", referralStatusCode = WITHDRAWN)
  val reasonExpected = ReferralStatusReason(
    code = REASON_DUPLICATE,
    description = "Duplicate referral",
    referralCategoryCode = CATEGORY_ADMIN,
    categoryDescription = null,
  )

  @Test
  fun `get all referral statuses`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())

    val response = getReferralStatuses()
    response.shouldNotBeNull()
    response.size.shouldBeGreaterThan(0)
    val withdrawnStatus = response.firstOrNull { it.code == WITHDRAWN }
    withdrawnStatus!! shouldBeEqual withdrawnStatusExpected
  }

  @Test
  fun `get single referral status`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())

    val response = getReferralStatus(WITHDRAWN)
    response.shouldNotBeNull()
    response shouldBeEqual withdrawnStatusExpected
  }

  @Test
  fun `get all referral status categories for a code`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())

    val response = getReferralStatusCategories(WITHDRAWN)
    response.shouldNotBeNull()
    response.size.shouldBeGreaterThan(0)
    val category = response.firstOrNull { it.code == CATEGORY_ADMIN }
    category!! shouldBeEqual categoryExpected
  }

  @Test
  fun `get single referral status category`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())

    val response = getReferralStatusCategory(CATEGORY_ADMIN)
    response.shouldNotBeNull()
    response shouldBeEqual categoryExpected
  }

  @Test
  fun `get all referral status reasons for a code`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())

    val response = getReferralStatusReasons(WITHDRAWN, CATEGORY_ADMIN)
    response.shouldNotBeNull()
    response.size.shouldBeGreaterThan(0)
    val category = response.firstOrNull { it.code == REASON_DUPLICATE }
    category!! shouldBeEqual reasonExpected
  }

  @Test
  fun `get all deselection referral status reasons for a deselection closed`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())

    val response = getReferralStatusReasonsDeselectedFlag(DESELECTED, PERSONAL, false)

    response.shouldNotBeNull()
    response.size.shouldBeEqual(4)
  }

  @Test
  fun `get all deselection referral status reasons for a deselection open`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())

    val response = getReferralStatusReasonsDeselectedFlag(DESELECTED, PERSONAL, true)

    response.shouldNotBeNull()
    response.size.shouldBeEqual(3)
  }

  @Test
  fun `get single referral status reason`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())

    val response = getReferralStatusReason(REASON_DUPLICATE)
    response.shouldNotBeNull()
    response shouldBeEqual reasonExpected
  }

  @Test
  fun `get diagram`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())

    val response = getStatusTransitionDiagram()
    response.shouldNotBeNull()
  }

  @Test
  fun `should return all withdrawal referral status reasons`() {
    // Given
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())
    // When
    val response = getAllReferralStatusReasonsForType(ReferralStatusType.WITHDRAWN.name)
    // Then
    response.shouldNotBeNull()
    response.size.shouldBeEqual(17)
    response[0].code shouldBeEqual REASON_DUPLICATE
    response[0].description shouldBeEqual "Duplicate referral"
    response[0].referralCategoryCode shouldBe CATEGORY_ADMIN
  }

  @Test
  fun `should return all deselected referral status reasons when keep open is false`() {
    // Given
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())
    // When
    val response = getAllReferralStatusReasonsForType(ReferralStatusType.DESELECTED.name)
    // Then
    response.shouldNotBeNull()
    response.size.shouldBeEqual(21)
    response[0].code shouldBeEqual "D_ATTITUDE"
    response[0].description shouldBeEqual "Attitude to group facilitators or others"
    response[0].referralCategoryCode shouldBe "D_MOTIVATION"
  }

  @Test
  fun `should return filtered deselected referral status reasons when keep open is true`() {
    // Given
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())
    // When
    val response = getAllReferralStatusReasonsForType(ReferralStatusType.DESELECTED.name, true)
    // Then
    response.shouldNotBeNull()
    response.size.shouldBeEqual(16)
    response[0].code shouldBeEqual "D_ATTITUDE"
    response[0].description shouldBeEqual "Attitude to group facilitators or others"
    response[0].referralCategoryCode shouldBe "D_MOTIVATION"
  }

  @Test
  fun `should return all assessed suitable referral status reasons`() {
    // Given
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())
    // When
    val response = getAllReferralStatusReasonsForType(ReferralStatusType.ASSESSED_SUITABLE.name)
    // Then
    response.shouldNotBeNull()
    response.size.shouldBeEqual(9)
    response.filter { it.referralCategoryCode == "AS_RISK" }.size.shouldBeEqual(5)
    assertThat(response.filter { it.referralCategoryCode == "AS_RISK" }).allMatch { it.categoryDescription == "Risk and need" }
    response.filter { it.referralCategoryCode == "AS_RISK" && it.code == "AS_REOFFENDING_RISK" }.getOrNull(0)?.description?.shouldBeEqual(
      "The person's psychological risk assessment shows high risk of reoffending",
    )

    response.filter { it.referralCategoryCode == "AS_INCOMPLETE" }.size.shouldBeEqual(2)
    assertThat(response.filter { it.referralCategoryCode == "AS_INCOMPLETE" }).allMatch { it.categoryDescription == "Incomplete assessment" }
    response.filter { it.referralCategoryCode == "AS_INCOMPLETE" && it.code == "AS_OUTDATED" }.getOrNull(0)?.description?.shouldBeEqual(
      "The risk and need assessment is outdated",
    )

    response.filter { it.referralCategoryCode == "AS_SENTENCE" }.size.shouldBeEqual(1)
    assertThat(response.filter { it.referralCategoryCode == "AS_SENTENCE" }).allMatch { it.categoryDescription == "Sentence type" }
    response.filter { it.referralCategoryCode == "AS_SENTENCE" && it.code == "AS_HIGH_ROSH" }.getOrNull(0)?.description?.shouldBeEqual(
      "The person has an Indefinite Sentence for the Public Protection and high ROSH (Risk of Serious Harm)",
    )

    response.filter { it.referralCategoryCode == "AS_OPERATIONAL" }.size.shouldBeEqual(1)
    assertThat(response.filter { it.referralCategoryCode == "AS_OPERATIONAL" }).allMatch { it.categoryDescription == "Operational" }
    response.filter { it.referralCategoryCode == "AS_OPERATIONAL" && it.code == "AS_NOT_ENOUGH_TIME" }.getOrNull(0)?.description?.shouldBeEqual(
      "There is not enough time to complete a high intensity programme so the person should complete a moderate intensity programme",
    )
  }

  @Test
  fun `should return all sexual offence details`() {
    // Given
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())

    // When
    val response = performRequestAndExpectOk(HttpMethod.GET, "/reference-data/sexual-offence-details", object : ParameterizedTypeReference<List<SexualOffenceDetails>>() {})

    // Then
    response.shouldNotBeNull()
    response.size.shouldBeEqual(23)

    response.filter { it.categoryCode == SexualOffenceCategoryType.AGAINST_MINORS }.size.shouldBeEqual(6)
    assertThat(response.filter { it.categoryCode == SexualOffenceCategoryType.AGAINST_MINORS }).allMatch { it.categoryDescription == "Sexual offence against somebody aged under 18" }
    response.filter { it.categoryCode == SexualOffenceCategoryType.AGAINST_MINORS && it.id == UUID.fromString("f5afed62-0747-432e-97b4-19b255e72b52") }.getOrNull(0)?.score?.shouldBeEqual(1)

    response.filter { it.categoryCode == SexualOffenceCategoryType.INCLUDES_VIOLENCE_FORCE_HUMILIATION }.size.shouldBeEqual(11)
    assertThat(response.filter { it.categoryCode == SexualOffenceCategoryType.INCLUDES_VIOLENCE_FORCE_HUMILIATION }).allMatch { it.categoryDescription == "Sexual offences that include violence, force or humiliation" }
    response.filter { it.categoryCode == SexualOffenceCategoryType.INCLUDES_VIOLENCE_FORCE_HUMILIATION && it.id == UUID.fromString("eca2a59e-9917-4e98-81df-a430649742b9") }.getOrNull(0)?.score?.shouldBeEqual(1)

    response.filter { it.categoryCode == SexualOffenceCategoryType.OTHER }.size.shouldBeEqual(6)
    assertThat(response.filter { it.categoryCode == SexualOffenceCategoryType.OTHER }).allMatch { it.categoryDescription == "Other types of sexual offending" }
    response.filter { it.categoryCode == SexualOffenceCategoryType.OTHER && it.id == UUID.fromString("70813fb3-33c8-4812-94cd-201eff0cdd6e") }.getOrNull(0)?.score?.shouldBeEqual(2)
  }

  @Test
  fun `should return bad request and error response when referral status type is not a permitted ReferralStatusType`() {
    // Given
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())

    // When
    val response = performRequestAndExpectStatus(
      HttpMethod.GET,
      "/reference-data/referral-statuses/invalid-request/categories/reasons",
      object : ParameterizedTypeReference<ErrorResponse>() {},
      HttpStatus.BAD_REQUEST.value(),
    )

    // Then
    response.shouldNotBeNull()
    response.status shouldBe 400
    response.userMessage shouldStartWith "Request not readable: Method parameter 'referralStatusType': Failed to convert value of type 'java.lang.String' to required type"
  }

  fun getReferralStatuses() = performRequestAndExpectOk(HttpMethod.GET, "/reference-data/referral-statuses", object : ParameterizedTypeReference<List<ReferralStatusRefData>>() {})
  fun getReferralStatus(code: String) = performRequestAndExpectOk(HttpMethod.GET, "/reference-data/referral-statuses/$code", object : ParameterizedTypeReference<ReferralStatusRefData>() {})
  fun getStatusTransitionDiagram() = performRequestAndExpectOk(HttpMethod.GET, "/status-transition-diagram", object : ParameterizedTypeReference<String>() {})
  fun getReferralStatusCategories(statusCode: String) = performRequestAndExpectOk(HttpMethod.GET, "/reference-data/referral-statuses/$statusCode/categories", object : ParameterizedTypeReference<List<ReferralStatusCategory>>() {})
  fun getReferralStatusCategory(code: String) = performRequestAndExpectOk(HttpMethod.GET, "/reference-data/referral-statuses/categories/$code", object : ParameterizedTypeReference<ReferralStatusCategory>() {})
  fun getReferralStatusReasons(statusCode: String, categoryCode: String) = performRequestAndExpectOk(HttpMethod.GET, "/reference-data/referral-statuses/$statusCode/categories/$categoryCode/reasons", object : ParameterizedTypeReference<List<ReferralStatusReason>>() {})
  fun getReferralStatusReasonsDeselectedFlag(statusCode: String, categoryCode: String, deselectAndKeepOpen: Boolean) = performRequestAndExpectOk(HttpMethod.GET, "/reference-data/referral-statuses/$statusCode/categories/$categoryCode/reasons?deselectAndKeepOpen=$deselectAndKeepOpen", object : ParameterizedTypeReference<List<ReferralStatusReason>>() {})
  fun getReferralStatusReason(code: String) = performRequestAndExpectOk(HttpMethod.GET, "/reference-data/referral-statuses/categories/reasons/$code", object : ParameterizedTypeReference<ReferralStatusReason>() {})
  fun getAllReferralStatusReasonsForType(referralStatusType: String, deselectAndKeepOpen: Boolean = false) = performRequestAndExpectOk(HttpMethod.GET, "/reference-data/referral-statuses/$referralStatusType/categories/reasons?deselectAndKeepOpen=$deselectAndKeepOpen", object : ParameterizedTypeReference<List<ReferralStatusReason>>() {})
}
