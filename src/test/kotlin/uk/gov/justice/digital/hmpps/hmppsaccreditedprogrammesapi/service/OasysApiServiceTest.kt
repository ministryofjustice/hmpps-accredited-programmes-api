package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.ClientResult
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.arnsApi.ArnsApiClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.OasysApiClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysAccommodation
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysAssessmentTimeline
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysAttitude
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysBehaviour
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysHealth
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysLearning
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysOffenceDetail
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysPsychiatric
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysRelationships
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysRoshFull
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.Timeline
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonApi.PrisonApiClient
import java.time.LocalDateTime

class OasysApiServiceTest {

  private val oasysApiClient = mockk<OasysApiClient>()
  private val arnsApiClient = mockk<ArnsApiClient>()
  private val prisonApiClient = mockk<PrisonApiClient>()
  private val auditService = mockk<AuditService>()
  val service = OasysService(oasysApiClient, arnsApiClient, prisonApiClient, auditService)

  @Test
  fun `should return assessmentId`() {
    val assessment1 =
      Timeline(123123, "COMPLETE", "LAYER3", LocalDateTime.now())
    val assessment2 = Timeline(
      999999,
      "COMPLETE",
      "LAYER3",
      LocalDateTime.now().minusDays(1),
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
      "offence analysis",
      listOf("Stalking"),
      "Yes",
      0,
      "No",
      "influences",
      "motivation",
      "Yes",
      "fully",
      "pattern",
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
      "Yes",
      "Yes",
      "Yes",
      "Yes",
      "Yes",
      "Free text",
      null,
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
      "Offence detail",
      "where when",
      "how done",
      "who were victims",
      "Any one else involved",
      "motivation",
      "source",
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
      "0 - no problems",
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
      "1",
      "2",
      "3",
      "4",
      "5",
      "6",
      "7",
      "8",
      "9",
      "10",
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
      "Yes",
      "blind",
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
      "0-no problems",
      "1-some problems",
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
      "0",
      "1",
      "2",
      "3",
      "4",
      "5",
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
}
