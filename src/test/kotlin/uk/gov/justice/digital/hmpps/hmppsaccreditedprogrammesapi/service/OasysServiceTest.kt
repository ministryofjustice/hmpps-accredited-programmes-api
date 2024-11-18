package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.ClientResult
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.OasysApiClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysAccommodation
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysAlcoholDetail
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysAssessmentTimeline
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysAttitude
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysBehaviour
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysDrugDetail
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysHealth
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysLearning
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysOffenceDetail
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysPsychiatric
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysRelationships
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysRoshFull
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.Sara
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.Timeline
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonApi.PrisonApiClient
import java.time.LocalDateTime

class OasysServiceTest {

  private val oasysApiClient = mockk<OasysApiClient>()
  private val prisonApiClient = mockk<PrisonApiClient>()
  private val auditService = mockk<AuditService>()
  val service = OasysService(oasysApiClient, prisonApiClient, auditService)

  @Test
  fun `should return assessmentId`() {
    val assessment1 =
      Timeline(123123, "COMPLETE", "LAYER3", LocalDateTime.now())
    val assessment2 = Timeline(
      id = 999999,
      status = "COMPLETE",
      type = "LAYER3",
      completedAt = LocalDateTime.now().minusDays(1),
    )
    val assessment3 = Timeline(111111, "STARTED", "LAYER3", null)
    val oasysAssessmentTimeline =
      OasysAssessmentTimeline("A9999BB", null, listOf(assessment1, assessment2, assessment3))

    every { oasysApiClient.getAssessments(any()) } returns ClientResult.Success(HttpStatus.OK, oasysAssessmentTimeline)

    val result = service.getAssessmentId("A9999BB")

    assertEquals(assessment1.id, result)
  }

  @Test
  fun `should return offence detail`() {
    val offenceDetail = OasysOffenceDetail(
      offenceAnalysis = "offence analysis",
      whatOccurred = listOf("Stalking"),
      recognisesImpact = "Yes",
      numberOfOthersInvolved = 0,
      othersInvolved = "No",
      peerGroupInfluences = "influences",
      offenceMotivation = "motivation",
      acceptsResponsibilityYesNo = "Yes",
      acceptsResponsibility = "fully",
      patternOffending = "pattern",
    )

    every { oasysApiClient.getOffenceDetail(any()) } returns ClientResult.Success(
      HttpStatus.OK,
      offenceDetail,
    )

    val result = service.getOffenceDetail(123123)

    assertEquals(offenceDetail, result)
  }

  @Test
  fun `should return relationships`() {
    val oasysRelationships = OasysRelationships(
      prevOrCurrentDomesticAbuse = "Yes",
      victimOfPartner = "Yes",
      victimOfFamily = "Yes",
      perpAgainstFamily = "Yes",
      perpAgainstPartner = "Yes",
      relIssuesDetails = "Free text",
      sara = null,
      emotionalCongruence = "0-No problems",
      relCloseFamily = "0-No problems",
      prevCloseRelationships = "2-Significant problems",
    )

    every { oasysApiClient.getRelationships(any()) } returns ClientResult.Success(
      HttpStatus.OK,
      oasysRelationships,
    )

    val result = service.getRelationships(123123)

    assertEquals(oasysRelationships, result)
  }

  @Test
  fun `should return Rosh Analysis`() {
    val oasysRoshFull = OasysRoshFull(
      currentOffenceDetails = "Offence detail",
      currentWhereAndWhen = "where when",
      currentHowDone = "how done",
      currentWhoVictims = "who were victims",
      currentAnyoneElsePresent = "Any one else involved",
      currentWhyDone = "motivation",
      currentSources = "source",
    )

    every { oasysApiClient.getRoshFull(any()) } returns ClientResult.Success(
      HttpStatus.OK,
      oasysRoshFull,
    )

    val result = service.getRoshFull(123123)

    assertEquals(oasysRoshFull, result)
  }

  @Test
  fun `should return psychiatric details`() {
    val psychiatric = OasysPsychiatric(
      currPsychiatricProblems = "0 - no problems",
      difficultiesCoping = null,
      currPsychologicalProblems = "0 - no problems",
      selfHarmSuicidal = null,
    )

    every { oasysApiClient.getPsychiatric(any()) } returns ClientResult.Success(
      HttpStatus.OK,
      psychiatric,
    )

    val result = service.getPsychiatric(123123)

    assertEquals(psychiatric, result)
  }

  @Test
  fun `should return behaviour details`() {
    val behaviour = OasysBehaviour(
      temperControl = "1",
      problemSolvingSkills = "2",
      awarenessOfConsequences = "3",
      achieveGoals = "4",
      understandsViewsOfOthers = "5",
      concreteAbstractThinking = "6",
      sexualPreOccupation = "7",
      offenceRelatedSexualInterests = "8",
      aggressiveControllingBehavour = "9",
      impulsivity = "10",
    )

    every { oasysApiClient.getBehaviour(any()) } returns ClientResult.Success(
      HttpStatus.OK,
      behaviour,
    )

    val result = service.getBehaviour(123123)

    assertEquals(behaviour, result)
  }

  @Test
  fun `should return health details`() {
    val health = OasysHealth(
      generalHealth = "Yes",
      generalHeathSpecify = "blind",
    )

    every { oasysApiClient.getHealth(any()) } returns ClientResult.Success(
      HttpStatus.OK,
      health,
    )

    val result = service.getHealth(123123)

    assertEquals(health, result)
  }

  @Test
  fun `should return attitude details`() {
    val attitude = OasysAttitude(
      proCriminalAttitudes = "0-no problems",
      motivationToAddressBehaviour = "1-some problems",
      hostileOrientation = "0-no problems",
    )

    every { oasysApiClient.getAttitude(any()) } returns ClientResult.Success(
      HttpStatus.OK,
      attitude,
    )

    val result = service.getAttitude(123123)

    assertEquals(attitude, result)
  }

  @Test
  fun `should return learning needs details`() {
    val oasysLearning = OasysLearning(
      workRelatedSkills = "0",
      problemsReadWriteNum = "1",
      learningDifficulties = "2",
      qualifications = "3",
      basicSkillsScore = "4",
      eTEIssuesDetails = "5",
    )
    val oasysAccommodation = OasysAccommodation(
      "Yes",
    )
    every { oasysApiClient.getLearning(any()) } returns ClientResult.Success(
      HttpStatus.OK,
      oasysLearning,
    )

    every { oasysApiClient.getAccommodation(any()) } returns ClientResult.Success(
      HttpStatus.OK,
      oasysAccommodation,
    )

    val learning = service.getLearning(123123)
    assertEquals(oasysLearning, learning)
    val accommodation = service.getAccommodation(123123)
    assertEquals(oasysAccommodation, accommodation)
  }

  @Test
  fun `should return alcohol details`() {
    val oasysAlcoholDetail = OasysAlcoholDetail(
      alcoholLinkedToHarm = "Yes",
      alcoholIssuesDetails = "Details about the problems",
      frequencyAndLevel = "0 - no problems",
      bingeDrinking = "1-Some problems",
    )

    every { oasysApiClient.getAlcoholDetail(any()) } returns ClientResult.Success(
      HttpStatus.OK,
      oasysAlcoholDetail,
    )

    val result = service.getAlcoholDetail(123123)

    assertEquals(oasysAlcoholDetail, result)
  }

  @Test
  fun `should return drug details`() {
    val oasysDrugDetail = OasysDrugDetail(
      LevelOfUseOfMainDrug = "1-Some problems",
      DrugsMajorActivity = "1-Some problems",
    )

    every { oasysApiClient.getDrugDetail(any()) } returns ClientResult.Success(
      HttpStatus.OK,
      oasysDrugDetail,
    )

    val result = service.getDrugDetail(123123)

    assertEquals(oasysDrugDetail, result)
  }

  @Test
  fun `should not return relationships when no completed SARA exists for provided prison number within six week period`() {
    // Given
    val oasysAssessmentTimeline = createAssessmentTimelineWithSaraOlderThanSixWeeks()

    every { oasysApiClient.getAssessments("A9999BB") } returns ClientResult.Success(HttpStatus.OK, oasysAssessmentTimeline)

    val oasysRelationShips1 = OasysRelationships(
      sara = null,
      prevOrCurrentDomesticAbuse = null,
      victimOfPartner = null,
      victimOfFamily = null,
      perpAgainstFamily = null,
      perpAgainstPartner = null,
      relIssuesDetails = null,
      emotionalCongruence = null,
      relCloseFamily = null,
      prevCloseRelationships = null,
    )

    val oasysRelationShips2 = OasysRelationships(
      sara = Sara(
        imminentRiskOfViolenceTowardsOthers = "HIGH",
        imminentRiskOfViolenceTowardsPartner = "HIGH",
      ),
      prevOrCurrentDomesticAbuse = null,
      victimOfPartner = null,
      victimOfFamily = null,
      perpAgainstFamily = null,
      perpAgainstPartner = null,
      relIssuesDetails = null,
      emotionalCongruence = null,
      relCloseFamily = null,
      prevCloseRelationships = null,
    )

    every { oasysApiClient.getRelationships(123123) } returns ClientResult.Success(HttpStatus.OK, oasysRelationShips1)
    every { oasysApiClient.getRelationships(999999) } returns ClientResult.Success(HttpStatus.OK, oasysRelationShips2)

    // When
    val oasysRelationships = service.getAssessmentIdWithCompletedSara(oasysAssessmentTimeline)

    // Then
    oasysRelationships.shouldBeNull()
  }

  @Test
  fun `should return relationships with completed SARA for provided prison number`() {
    // Given
    val oasysAssessmentTimeline = createAssessmentTimeline()

    every { oasysApiClient.getAssessments("A9999BB") } returns ClientResult.Success(HttpStatus.OK, oasysAssessmentTimeline)

    val oasysRelationShips1 = OasysRelationships(
      sara = null,
      prevOrCurrentDomesticAbuse = null,
      victimOfPartner = null,
      victimOfFamily = null,
      perpAgainstFamily = null,
      perpAgainstPartner = null,
      relIssuesDetails = null,
      emotionalCongruence = null,
      relCloseFamily = null,
      prevCloseRelationships = null,
    )

    val oasysRelationShips2 = OasysRelationships(
      sara = Sara(
        imminentRiskOfViolenceTowardsOthers = "HIGH",
        imminentRiskOfViolenceTowardsPartner = "HIGH",
      ),
      prevOrCurrentDomesticAbuse = null,
      victimOfPartner = null,
      victimOfFamily = null,
      perpAgainstFamily = null,
      perpAgainstPartner = null,
      relIssuesDetails = null,
      emotionalCongruence = null,
      relCloseFamily = null,
      prevCloseRelationships = null,
    )

    every { oasysApiClient.getRelationships(123123) } returns ClientResult.Success(HttpStatus.OK, oasysRelationShips1)
    every { oasysApiClient.getRelationships(999999) } returns ClientResult.Success(HttpStatus.OK, oasysRelationShips2)

    // When
    val completedSaraAssessment = service.getAssessmentIdWithCompletedSara(oasysAssessmentTimeline)

    // Then
    completedSaraAssessment.shouldNotBeNull()
    completedSaraAssessment.shouldBe(999999)
  }

  @Test
  fun `should return relationships with completed SARA for provided prison number when completed Sara occurs on same day as assessment`() {
    // Given
    val oasysAssessmentTimeline = createAssessmentTimelineWithSaraOnTheSameDay()

    every { oasysApiClient.getAssessments("A9999BB") } returns ClientResult.Success(HttpStatus.OK, oasysAssessmentTimeline)

    val oasysRelationShips1 = OasysRelationships(
      sara = null,
      prevOrCurrentDomesticAbuse = null,
      victimOfPartner = null,
      victimOfFamily = null,
      perpAgainstFamily = null,
      perpAgainstPartner = null,
      relIssuesDetails = null,
      emotionalCongruence = null,
      relCloseFamily = null,
      prevCloseRelationships = null,
    )

    val oasysRelationShips2 = OasysRelationships(
      sara = Sara(
        imminentRiskOfViolenceTowardsOthers = "HIGH",
        imminentRiskOfViolenceTowardsPartner = "HIGH",
      ),
      prevOrCurrentDomesticAbuse = null,
      victimOfPartner = null,
      victimOfFamily = null,
      perpAgainstFamily = null,
      perpAgainstPartner = null,
      relIssuesDetails = null,
      emotionalCongruence = null,
      relCloseFamily = null,
      prevCloseRelationships = null,
    )

    every { oasysApiClient.getRelationships(123123) } returns ClientResult.Success(HttpStatus.OK, oasysRelationShips1)
    every { oasysApiClient.getRelationships(999999) } returns ClientResult.Success(HttpStatus.OK, oasysRelationShips2)

    // When
    val completedSaraAssessment = service.getAssessmentIdWithCompletedSara(oasysAssessmentTimeline)

    // Then
    completedSaraAssessment.shouldNotBeNull()
    completedSaraAssessment.shouldBe(999999)
  }

  private fun createAssessmentTimeline(): OasysAssessmentTimeline {
    val assessment1 = Timeline(
      id = 123123,
      status = "COMPLETE",
      type = "LAYER3",
      completedAt = LocalDateTime.now(),
    )
    val assessment2 = Timeline(
      id = 999999,
      status = "COMPLETE",
      type = "LAYER3",
      completedAt = LocalDateTime.now().minusWeeks(5),
    )
    val assessment3 = Timeline(
      id = 111111,
      status = "STARTED",
      type = "LAYER3",
      completedAt = null,
    )
    val oasysAssessmentTimeline =
      OasysAssessmentTimeline("A9999BB", null, listOf(assessment1, assessment2, assessment3))
    return oasysAssessmentTimeline
  }

  private fun createAssessmentTimelineWithSaraOnTheSameDay(): OasysAssessmentTimeline {
    val completedAssessmentDateTime = LocalDateTime.of(2024, 10, 20, 15, 30)
    val assessment1 = Timeline(
      id = 123123,
      status = "COMPLETE",
      type = "LAYER3",
      completedAt = completedAssessmentDateTime,
    )
    val assessment2 = Timeline(
      id = 999999,
      status = "COMPLETE",
      type = "LAYER3",
      completedAt = completedAssessmentDateTime.minusHours(2),
    )
    val assessment3 = Timeline(
      id = 111111,
      status = "STARTED",
      type = "LAYER3",
      completedAt = null,
    )
    val oasysAssessmentTimeline =
      OasysAssessmentTimeline("A9999BB", null, listOf(assessment1, assessment2, assessment3))
    return oasysAssessmentTimeline
  }

  private fun createAssessmentTimelineWithSaraOlderThanSixWeeks(): OasysAssessmentTimeline {
    val assessment1 = Timeline(
      id = 123123,
      status = "COMPLETE",
      type = "LAYER3",
      completedAt = LocalDateTime.now(),
    )
    val assessment2 = Timeline(
      id = 999999,
      status = "COMPLETE",
      type = "LAYER3",
      completedAt = LocalDateTime.now().minusWeeks(7),
    )
    val assessment3 = Timeline(
      id = 111111,
      status = "STARTED",
      type = "LAYER3",
      completedAt = null,
    )
    val oasysAssessmentTimeline =
      OasysAssessmentTimeline("A9999BB", null, listOf(assessment1, assessment2, assessment3))
    return oasysAssessmentTimeline
  }
}