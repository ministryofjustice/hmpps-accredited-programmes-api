package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.ClientResult
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.OasysApiClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysAssessmentTimeline
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysOffenceDetail
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysOffenceDetailWrapper
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysRelationships
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysRelationshipsWrapper
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.Timeline
import java.time.LocalDateTime

class OasysApiServiceTest {

  private val oasysApiClient = mockk<OasysApiClient>()
  val service = OasysService(oasysApiClient)

  @Test
  fun `should return assessmentId`() {
    val assessment1 =
      Timeline(123123, "COMPLETE", "LAYER3", LocalDateTime.now(), LocalDateTime.now().minusDays(10))
    val assessment2 = Timeline(
      999999,
      "COMPLETE",
      "LAYER3",
      LocalDateTime.now().minusDays(1),
      LocalDateTime.now().minusDays(10),
    )
    val assessment3 = Timeline(111111, "STARTED", "LAYER3", null, LocalDateTime.now().minusDays(10))
    val oasysAssessmentTimeline = OasysAssessmentTimeline(listOf(assessment1, assessment2, assessment3))

    every { oasysApiClient.getAssessments(any()) } returns ClientResult.Success(HttpStatus.OK, oasysAssessmentTimeline)

    val result = service.getAssessmentId("A9999BB")

    assertEquals(assessment1.assessmentPk, result)
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
    val response = OasysOffenceDetailWrapper(
      listOf(offenceDetail),
    )

    every { oasysApiClient.getOffenceDetail(any()) } returns ClientResult.Success(
      HttpStatus.OK,
      response,
    )

    val result = service.getOffenceDetail(123123)

    assertEquals(response, result)
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
    )
    val response = OasysRelationshipsWrapper(
      listOf(oasysRelationships),
    )

    every { oasysApiClient.getRelationships(any()) } returns ClientResult.Success(
      HttpStatus.OK,
      response,
    )

    val result = service.getRelationships(123123)

    assertEquals(response, result)
  }
}
