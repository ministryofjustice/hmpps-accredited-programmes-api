package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.arnsApi

import com.fasterxml.jackson.databind.DeserializationFeature
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
) : BaseHMPPSClient(
  webClient,
  // Tolerate unknown fields across the whole ARNS response tree. ARNS evolves its schema
  // independently (e.g. the added "assessmentType" field), so new fields must not fail
  // deserialisation. Scoped to this client only — no blast radius to the other HMPPS clients.
  jacksonObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false),
) {

  fun getRiskPredictors(assessmentPk: Long) = getRequest<AllPredictorVersioned<Any>>(ARNS_API) {
    path = "/assessments/id/$assessmentPk/risk/predictors/all"
  }
}
