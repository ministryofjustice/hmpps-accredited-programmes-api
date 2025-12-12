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
      log.error("Failure when retrieving risk predictors for assessment id : $assessmentId", result.toException())
      result.throwException()
    }

    is ClientResult.Success -> result.body
  }
}
