package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.BaseHMPPSClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysAccommodation
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysAssessmentTimeline
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysAttitude
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysBehaviour
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysHealth
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysLearning
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysLifestyle
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysOffenceDetail
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysOffendingInfo
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysPsychiatric
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysRelationships
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysRoshFull
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysRoshSummary

@Component
class OasysApiClient(
  @Qualifier("oasysApiWebClient")
  webClient: WebClient,
) : BaseHMPPSClient(webClient, jacksonObjectMapper()) {

  fun getAssessments(prisonerNumber: String) = getRequest<OasysAssessmentTimeline> {
    path = "/timeline/$prisonerNumber"
  }

  fun getOffendingInfo(assessmentPk: Long) = getRequest<OasysOffendingInfo> {
    path = "/$assessmentPk/section/section1"
  }

  fun getOffenceDetail(assessmentPk: Long) = getRequest<OasysOffenceDetail> {
    path = "/$assessmentPk/section/section2"
  }

  fun getAccommodation(assessmentPk: Long) = getRequest<OasysAccommodation> {
    path = "/$assessmentPk/section/section3"
  }

  fun getLearning(assessmentPk: Long) = getRequest<OasysLearning> {
    path = "/$assessmentPk/section/section4"
  }

  fun getRelationships(assessmentPk: Long) = getRequest<OasysRelationships> {
    path = "/$assessmentPk/section/section6"
  }

  fun getLifestyle(assessmentPk: Long) = getRequest<OasysLifestyle> {
    path = "/$assessmentPk/section/section7"
  }

  fun getPsychiatric(assessmentPk: Long) = getRequest<OasysPsychiatric> {
    path = "/$assessmentPk/section/section10"
  }

  fun getBehaviour(assessmentPk: Long) = getRequest<OasysBehaviour> {
    path = "/$assessmentPk/section/section11"
  }

  fun getAttitude(assessmentPk: Long) = getRequest<OasysAttitude> {
    path = "/$assessmentPk/section/section12"
  }

  fun getHealth(assessmentPk: Long) = getRequest<OasysHealth> {
    path = "/$assessmentPk/section/section13"
  }

  fun getRoshFull(assessmentPk: Long) = getRequest<OasysRoshFull> {
    path = "/$assessmentPk/section/sectionroshfull"
  }

  fun getRoshSummary(assessmentPk: Long) = getRequest<OasysRoshSummary> {
    path = "/$assessmentPk/section/sectionroshsumm"
  }
}
