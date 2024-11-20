package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.integration

import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.reactive.server.expectBody
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.config.JwtAuthHelper
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.config.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Alert
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Attitude
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Behaviour
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.DrugAlcoholDetail
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Health
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.LearningNeeds
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Lifestyle
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.OasysAssessmentDateInfo
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.OffenceDetail
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Psychiatric
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Relationships
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Risks
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.RoshAnalysis
import java.math.BigDecimal
import java.math.RoundingMode
import java.time.LocalDate
import java.time.Month

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@Import(JwtAuthHelper::class)
class OasysControllerIntegrationTest : IntegrationTestBase() {

  @Test
  fun `Get offence details from Oasys`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())
    val prisonNumber = "A9999BB"
    val offenceDetail = getOffenceDetailsByPrisonNumber(prisonNumber)

    offenceDetail.shouldNotBeNull()
    offenceDetail shouldBeEqual OffenceDetail(
      offenceDetails = "An attack took place on christmas eve in Alfreds ex partners house. The children were in bed and the dog was left out side.",
      contactTargeting = false,
      raciallyMotivated = false,
      revenge = true,
      domesticViolence = true,
      repeatVictimisation = true,
      victimWasStranger = true,
      stalking = true,
      recognisesImpact = false,
      numberOfOthersInvolved = null,
      othersInvolvedDetail = null,
      peerGroupInfluences = "No",
      motivationAndTriggers = "Mainly due to jealousy and fuelled by drug use",
      acceptsResponsibility = false,
      acceptsResponsibilityDetail = "This has happened numerous times in the past",
      patternOffending = null,
    )
  }

  @Test
  fun `Get offence details from Oasys with invalid prison number`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())
    val prisonNumber = "Z9999ZZ"
    val errorResponse = getOffenceDetailsByPrisonNumber404(prisonNumber)
    errorResponse shouldBeEqual
      ErrorResponse(
        status = HttpStatus.NOT_FOUND,
        userMessage = "Not Found: No assessment found for prison number: Z9999ZZ",
        developerMessage = "No assessment found for prison number: Z9999ZZ",
      )
  }

  @Test
  fun `Get relationships from Oasys`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())
    val prisonNumber = "A9999BB"
    val relationships = getRelationshipsByPrisonNumber(prisonNumber)

    relationships.shouldNotBeNull()
    relationships shouldBeEqual Relationships(
      dvEvidence = true,
      victimFormerPartner = true,
      victimFamilyMember = true,
      victimOfPartnerFamily = true,
      perpOfPartnerOrFamily = true,
      relIssuesDetails = "Free text",
      relCloseFamily = "0-No problems",
      relCurrRelationshipStatus = "Not in a relationship",
      prevCloseRelationships = "2-Significant problems",
      emotionalCongruence="0-No problems",
      relationshipWithPartner="0-No problems",
      prevOrCurrentDomesticAbuse="Yes"
    )
  }

  @Test
  fun `Get risk information from Oasys`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())
    val prisonNumber = "A9999BB"
    val risks = getRisksByPrisonNumber(prisonNumber)

    risks.shouldNotBeNull()
    risks shouldBeEqual Risks(
      ogrsYear1 = BigDecimal(8),
      ogrsYear2 = BigDecimal(15),
      ogrsRisk = LOW,
      ovpYear1 = BigDecimal(8),
      ovpYear2 = BigDecimal(15),
      ovpRisk = MEDIUM,
      rsrScore = BigDecimal(1.46).setScale(2, RoundingMode.HALF_UP),
      rsrRisk = LOW,
      ospcScore = HIGH,
      ospiScore = MEDIUM,
      overallRoshLevel = MEDIUM,
      riskPrisonersCustody = LOW,
      riskStaffCustody = LOW,
      riskKnownAdultCustody = LOW,
      riskPublicCustody = LOW,
      riskChildrenCustody = LOW,
      riskStaffCommunity = LOW,
      riskKnownAdultCommunity = LOW,
      riskPublicCommunity = MEDIUM,
      riskChildrenCommunity = LOW,
      imminentRiskOfViolenceTowardsPartner = null,
      imminentRiskOfViolenceTowardsOthers = null,
      alerts = listOf(
        Alert(
          description = "ACCT Open (HMPS)",
          alertType = "Self Harm",
          dateCreated = LocalDate.of(2016, 1, 21),
        ),
        Alert(
          description = "L1 Restriction No contact with any child",
          alertType = "Child Communication Measures",
          dateCreated = LocalDate.of(2014, 7, 23),
        ),
      ),
    )
  }

  @Test
  fun `Get lifestyle from Oasys`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())
    val prisonNumber = "A9999BB"
    val lifestyle = getLifestyleByPrisonNumber(prisonNumber)

    lifestyle.shouldNotBeNull()
    lifestyle shouldBeEqual Lifestyle(
      "drug taking",
      "regularly takes drugs and struggles to support this without resorting to crime",
      "1 - some problems",
    )
  }

  @Test
  fun `Get behaviour from Oasys`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())
    val prisonNumber = "A9999BB"
    val lifestyle = getBehaviourByPrisonNumber(prisonNumber)

    lifestyle.shouldNotBeNull()
    lifestyle shouldBeEqual Behaviour(
      temperControl = "4 - massive problems",
      problemSolvingSkills = "2 - slight problems",
      awarenessOfConsequences = "0 - no problems",
      achieveGoals = "3 - big problems",
      understandsViewsOfOthers = "1 - some problems",
      concreteAbstractThinking = "1 - some problems",
      sexualPreOccupation = "2 - slight problems",
      offenceRelatedSexualInterests = "2 - slight problems",
      aggressiveControllingBehaviour = "1 - some problems",
      impulsivity = "1 - some problems",
    )
  }

  @Test
  fun `Get rosh analysis from Oasys`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())
    val prisonNumber = "A9999BB"
    val roshAnalysis = getRoshAnalysisByPrisonNumber(prisonNumber)

    roshAnalysis.shouldNotBeNull()
    roshAnalysis shouldBeEqual RoshAnalysis(
      offenceDetails = "Assault with a base ball bat",
      whereAndWhen = "in the park",
      howDone = "with a base ball bat",
      whoVictims = "the gardener",
      anyoneElsePresent = "noone",
      whyDone = "anger issues",
      sources = "local police",
    )
  }

  @Test
  fun `Get psychiatric data from Oasys`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())
    val prisonNumber = "A9999BB"
    val psychiatric = getPsychiatricByPrisonNumber(prisonNumber)

    psychiatric.shouldNotBeNull()
    psychiatric shouldBeEqual Psychiatric(
      "0-No problems",
    )
  }

  @Test
  fun `Get health data from Oasys`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())
    val prisonNumber = "A9999BB"
    val health = getHealthByPrisonNumber(prisonNumber)

    health.shouldNotBeNull()
    health shouldBeEqual Health(
      true,
      "Has a prosthetic leg",
    )
  }

  @Test
  fun `Get attitude data from Oasys`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())
    val prisonNumber = "A9999BB"
    val attitude = getAttitudeByPrisonNumber(prisonNumber)

    attitude.shouldNotBeNull()
    attitude shouldBeEqual Attitude(
      proCriminalAttitudes = "1-Some problems",
      motivationToAddressBehaviour = "0-Very motivated",
      hostileOrientation = "1-Some problems",
    )
  }

  @Test
  fun `Get learning needs data from Oasys`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())
    val prisonNumber = "A9999BB"
    val attitude = getLearningNeedsByPrisonNumber(prisonNumber)

    attitude.shouldNotBeNull()
    attitude shouldBeEqual LearningNeeds(
      true,
      "0 - No problems",
      "1 - Some problems",
      "3 - big problems",
      "0 - no qualifications",
      "33",
      "Some text about how clever or thick this person is",
    )
  }

  @Test
  fun `Get drug and alcohol data from Oasys`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())
    val prisonNumber = "A9999BB"
    val drugAlcoholDetail = getDrugAndAlcoholDetail(prisonNumber)

    drugAlcoholDetail.shouldNotBeNull()
    drugAlcoholDetail shouldBeEqual DrugAlcoholDetail(
      drug = uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.OasysDrugDetail(
        levelOfUseOfMainDrug = "1-Some problems",
        drugsMajorActivity = "0-Very motivated",
      ),
      alcohol = uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.OasysAlcoholDetail(
        alcoholLinkedToHarm = "1-Some problems",
        alcoholIssuesDetails = "Known to have some problems",
        frequencyAndLevel = "frequent",
        bingeDrinking = "1-Some problems",
      ),
    )
  }

  @Test
  fun `Get latest assessement date info`() {
    mockClientCredentialsJwtRequest(jwt = jwtAuthHelper.bearerToken())
    val prisonNumber = "A9999CC"
    val oasysAssessmentDateInfo = getLatestAssessmentDateByPrisonNumber(prisonNumber)

    oasysAssessmentDateInfo shouldNotBe null
    oasysAssessmentDateInfo.hasOpenAssessment shouldBe true
    oasysAssessmentDateInfo.recentCompletedAssessmentDate shouldBe LocalDate.of(2023, Month.DECEMBER, 19)
  }

  fun getDrugAndAlcoholDetail(prisonNumber: String) =
    webTestClient
      .get()
      .uri("/oasys/$prisonNumber/drug-and-alcohol-details")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody<DrugAlcoholDetail>()
      .returnResult().responseBody!!

  fun getRoshAnalysisByPrisonNumber(prisonNumber: String) =
    webTestClient
      .get()
      .uri("/oasys/$prisonNumber/rosh-analysis")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody<RoshAnalysis>()
      .returnResult().responseBody!!

  fun getRelationshipsByPrisonNumber(prisonNumber: String) =
    webTestClient
      .get()
      .uri("/oasys/$prisonNumber/relationships")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody<Relationships>()
      .returnResult().responseBody!!

  fun getRisksByPrisonNumber(prisonNumber: String) =
    webTestClient
      .get()
      .uri("/oasys/$prisonNumber/risks-and-alerts")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody<Risks>()
      .returnResult().responseBody!!

  fun getLifestyleByPrisonNumber(prisonNumber: String) =
    webTestClient
      .get()
      .uri("/oasys/$prisonNumber/lifestyle")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody<Lifestyle>()
      .returnResult().responseBody!!

  fun getPsychiatricByPrisonNumber(prisonNumber: String) =
    webTestClient
      .get()
      .uri("/oasys/$prisonNumber/psychiatric")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody<Psychiatric>()
      .returnResult().responseBody!!

  fun getBehaviourByPrisonNumber(prisonNumber: String) =
    webTestClient
      .get()
      .uri("/oasys/$prisonNumber/behaviour")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody<Behaviour>()
      .returnResult().responseBody!!

  fun getHealthByPrisonNumber(prisonNumber: String) =
    webTestClient
      .get()
      .uri("/oasys/$prisonNumber/health")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody<Health>()
      .returnResult().responseBody!!

  fun getAttitudeByPrisonNumber(prisonNumber: String) =
    webTestClient
      .get()
      .uri("/oasys/$prisonNumber/attitude")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody<Attitude>()
      .returnResult().responseBody!!

  fun getLearningNeedsByPrisonNumber(prisonNumber: String) =
    webTestClient
      .get()
      .uri("/oasys/$prisonNumber/learning-needs")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody<LearningNeeds>()
      .returnResult().responseBody!!

  fun getOffenceDetailsByPrisonNumber(prisonNumber: String) =
    webTestClient
      .get()
      .uri("/oasys/$prisonNumber/offence-details")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody<OffenceDetail>()
      .returnResult().responseBody!!

  fun getOffenceDetailsByPrisonNumber404(prisonNumber: String) =
    webTestClient
      .get()
      .uri("/oasys/$prisonNumber/offence-details")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().is4xxClientError
      .expectBody<ErrorResponse>()
      .returnResult().responseBody!!

  fun getLatestAssessmentDateByPrisonNumber(prisonNumber: String) =
    webTestClient
      .get()
      .uri("/oasys/$prisonNumber/assessment_date")
      .header(HttpHeaders.AUTHORIZATION, jwtAuthHelper.bearerToken())
      .accept(MediaType.APPLICATION_JSON)
      .exchange()
      .expectStatus().isOk
      .expectBody<OasysAssessmentDateInfo>()
      .returnResult().responseBody!!

  companion object {
    private const val LOW = "Low"
    private const val MEDIUM = "Medium"
    private const val HIGH = "High"
  }
}
