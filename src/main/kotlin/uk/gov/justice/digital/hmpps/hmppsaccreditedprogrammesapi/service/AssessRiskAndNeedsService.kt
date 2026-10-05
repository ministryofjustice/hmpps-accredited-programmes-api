package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.ClientResult
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.arnsApi.AssessRiskAndNeedsApiClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.arnsApi.model.AllPredictorVersioned

@Service
class AssessRiskAndNeedsService(

  private val assessRiskAndNeedsApiClient: AssessRiskAndNeedsApiClient,
) {
  private val log = LoggerFactory.getLogger(this::class.java)

  fun getRiskPredictors(assessmentId: Long): AllPredictorVersioned<Any>? = when (val result = assessRiskAndNeedsApiClient.getRiskPredictors(assessmentId)) {
    is ClientResult.Failure -> {
      // Degrade gracefully: a failure retrieving ARNS risk predictors for a single assessment
      // must not 500 the whole risks-and-alerts endpoint. Log and return null so the caller
      // (OasysService.getRisks) can build a partial Risks response. This also covers transient
      // ARNS connection drops, not just unknown-field deserialisation failures.
      log.error("Failure when retrieving risk predictors for assessment id : $assessmentId", result.toException())
      null
    }

    is ClientResult.Success -> result.body
  }
}
