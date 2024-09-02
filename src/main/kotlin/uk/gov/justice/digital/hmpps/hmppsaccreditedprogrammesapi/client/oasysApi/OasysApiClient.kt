package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.BaseHMPPSClient
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysAccommodation
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysAlcoholDetail
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysAssessmentTimeline
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysAttitude
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysBehaviour
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysDrugDetail
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysHealth
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysLearning
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysLifestyle
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysOffenceDetail
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysOffendingInfo
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysPsychiatric
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysRelationships
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysRiskPredictorScores
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysRoshFull
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysRoshSummary

private const val OASYS_API = "Oasys API"

@Component
class OasysApiClient(
  @Qualifier("oasysApiWebClient")
  webClient: WebClient,
) : BaseHMPPSClient(webClient, jacksonObjectMapper()) {

  fun getAssessments(prisonerNumber: String) = getRequest<OasysAssessmentTimeline>(OASYS_API) {
    path = "/assessments/timeline/$prisonerNumber"
  }

  fun getOffendingInfo(assessmentPk: Long) = getRequest<OasysOffendingInfo>(OASYS_API) {
    path = "/assessments/$assessmentPk/section/section1"
  }

  fun getOffenceDetail(assessmentPk: Long) = getRequest<OasysOffenceDetail>(OASYS_API) {
    path = "/assessments/$assessmentPk/section/section2"
  }

  fun getAccommodation(assessmentPk: Long) = getRequest<OasysAccommodation>(OASYS_API) {
    path = "/assessments/$assessmentPk/section/section3"
  }

  fun getLearning(assessmentPk: Long) = getRequest<OasysLearning>(OASYS_API) {
    path = "/assessments/$assessmentPk/section/section4"
  }

  fun getRelationships(assessmentPk: Long) = getRequest<OasysRelationships>(OASYS_API) {
    path = "/assessments/$assessmentPk/section/section6"
  }

  fun getLifestyle(assessmentPk: Long) = getRequest<OasysLifestyle>(OASYS_API) {
    path = "/assessments/$assessmentPk/section/section7"
  }

  fun getDrugDetail(assessmentPk: Long) = getRequest<OasysDrugDetail>(OASYS_API) {
    path = "/assessments/$assessmentPk/section/section8"
  }

  fun getAlcoholDetail(assessmentPk: Long) = getRequest<OasysAlcoholDetail>(OASYS_API) {
    path = "/assessments/$assessmentPk/section/section9"
  }

  fun getPsychiatric(assessmentPk: Long) = getRequest<OasysPsychiatric>(OASYS_API) {
    path = "/assessments/$assessmentPk/section/section10"
  }

  fun getBehaviour(assessmentPk: Long) = getRequest<OasysBehaviour>(OASYS_API) {
    path = "/assessments/$assessmentPk/section/section11"
  }

  fun getAttitude(assessmentPk: Long) = getRequest<OasysAttitude>(OASYS_API) {
    path = "/assessments/$assessmentPk/section/section12"
  }

  fun getHealth(assessmentPk: Long) = getRequest<OasysHealth>(OASYS_API) {
    path = "/assessments/$assessmentPk/section/section13"
  }

  fun getRoshFull(assessmentPk: Long) = getRequest<OasysRoshFull>(OASYS_API) {
    path = "/assessments/$assessmentPk/section/sectionroshfull"
  }

  fun getRoshSummary(assessmentPk: Long) = getRequest<OasysRoshSummary>(OASYS_API) {
    path = "/assessments/$assessmentPk/section/sectionroshsumm"
  }

  fun getRiskPredictors(assessmentPk: Long) = getRequest<OasysRiskPredictorScores>(OASYS_API) {
    path = "/assessments/$assessmentPk/risk-predictors"
  }
}
