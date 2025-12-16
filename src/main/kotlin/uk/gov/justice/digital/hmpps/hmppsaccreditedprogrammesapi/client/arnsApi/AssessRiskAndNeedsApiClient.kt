package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.arnsApi

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.BaseHMPPSClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.arnsApi.model.AllPredictorVersioned

private const val ARNS_API = "ARNS API"

@Component
class AssessRiskAndNeedsApiClient(
  @Qualifier("arnsApiWebClient") webClient: WebClient,
) : BaseHMPPSClient(webClient, jacksonObjectMapper()) {

  fun getRiskPredictors(assessmentPk: Long) = getRequest<AllPredictorVersioned<Any>>(ARNS_API) {
    path = "/assessments/id/$assessmentPk/risk/predictors/all"
  }
}
