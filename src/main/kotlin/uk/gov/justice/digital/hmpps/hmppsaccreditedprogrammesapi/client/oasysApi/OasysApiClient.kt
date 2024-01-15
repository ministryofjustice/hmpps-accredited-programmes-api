package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.cache.WebClientCache
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.BaseHMPPSClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysAssessmentTimeline
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysOffenceDetail
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysRelationships

@Component
class OasysApiClient(
  @Qualifier("oasysApiWebClient")
  webClient: WebClient,
  webClientCache: WebClientCache,
) : BaseHMPPSClient(webClient, jacksonObjectMapper(), webClientCache) {

  fun getAssessments(prisonerNumber: String) = getRequest<OasysAssessmentTimeline> {
    path = "/timeline/$prisonerNumber"
  }

  fun getOffenceDetail(assessmentPk: Long) = getRequest<OasysOffenceDetail> {
    path = "/$assessmentPk/section/section2"
  }

  fun getRelationships(assessmentPk: Long) = getRequest<OasysRelationships> {
    path = "/$assessmentPk/section/section6"
  }
}
