package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Test
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AllPredictorVersionedDto
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.ClientResult
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.arnsApi.AssessRiskAndNeedsApiClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.arnsApi.model.AllPredictorVersioned

class AssessRiskAndNeedsServiceTest {

  private val assessRiskAndNeedsApiClient = mockk<AssessRiskAndNeedsApiClient>()
  private val service = AssessRiskAndNeedsService(assessRiskAndNeedsApiClient)

  @Test
  fun `getRiskPredictors returns the body on success`() {
    val assessmentId = 2516194818L
    val predictors: AllPredictorVersioned<Any> = AllPredictorVersionedDto(outputVersion = "2")
    every { assessRiskAndNeedsApiClient.getRiskPredictors(assessmentId) } returns
      ClientResult.Success(HttpStatus.OK, predictors)

    service.getRiskPredictors(assessmentId) shouldBe predictors
  }

  @Test
  fun `getRiskPredictors degrades to null on failure instead of rethrowing`() {
    // A single failing ARNS predictors call must not 500 the whole risks-and-alerts endpoint.
    val assessmentId = 2516194818L
    every { assessRiskAndNeedsApiClient.getRiskPredictors(assessmentId) } returns
      ClientResult.Failure.Other(
        HttpMethod.GET,
        "/assessments/id/$assessmentId/risk/predictors/all",
        RuntimeException("Connection prematurely closed BEFORE response"),
        "ARNS API",
      )

    service.getRiskPredictors(assessmentId).shouldBeNull()
  }
}
