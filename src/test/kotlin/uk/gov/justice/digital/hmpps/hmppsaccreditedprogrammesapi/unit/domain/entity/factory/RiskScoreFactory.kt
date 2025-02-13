package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.IndividualRiskScores
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.RiskScore
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Sara
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.type.SaraRisk
import java.math.BigDecimal

class RiskScoreFactory {
  private var classification: String = "High Risk"
  private var ogrs3: BigDecimal = BigDecimal("1")
  private var ovp: BigDecimal = BigDecimal("2")
  private var ospDc: String = "0"
  private var ospIic: String = "1"
  private var rsr: BigDecimal = BigDecimal("5")
  private var sara: Sara = SaraFactory().produce()

  fun withClassification(classification: String) = apply { this.classification = classification }
  fun withOgrs3(ogrs3: BigDecimal) = apply { this.ogrs3 = ogrs3 }
  fun withOvp(ovp: BigDecimal) = apply { this.ovp = ovp }
  fun withOspDc(ospDc: String) = apply { this.ospDc = ospDc }
  fun withOspIic(ospIic: String) = apply { this.ospIic = ospIic }
  fun withRsr(rsr: BigDecimal) = apply { this.rsr = rsr }
  fun withSara(sara: Sara) = apply { this.sara = sara }

  fun produce() = RiskScore(
    classification = classification,
    individualRiskScores = IndividualRiskScores(
      ogrs3 = ogrs3,
      ovp = ovp,
      ospDc = ospDc,
      ospIic = ospIic,
      rsr = rsr,
      sara = sara,
    ),
  )

  class SaraFactory {
    private var overallResult: SaraRisk? = SaraRisk.LOW
    private var saraRiskOfViolenceTowardsPartner: String? = "LOW"
    private var saraRiskOfViolenceTowardsOthers: String? = "LOW"
    private var saraAssessmentId: Long? = 2512235167

    fun withOverallResult(overallResult: SaraRisk?) = apply { this.overallResult = overallResult }
    fun withSaraRiskOfViolenceTowardsPartner(saraRiskOfViolenceTowardsPartner: String?) =
      apply { this.saraRiskOfViolenceTowardsPartner = saraRiskOfViolenceTowardsPartner }

    fun withSaraRiskOfViolenceTowardsOthers(saraRiskOfViolenceTowardsOthers: String?) =
      apply { this.saraRiskOfViolenceTowardsOthers = saraRiskOfViolenceTowardsOthers }

    fun withSaraAssessmentId(saraAssessmentId: Long?) = apply { this.saraAssessmentId = saraAssessmentId }

    fun produce() = Sara(
      overallResult = overallResult,
      saraRiskOfViolenceTowardsPartner = saraRiskOfViolenceTowardsPartner,
      saraRiskOfViolenceTowardsOthers = saraRiskOfViolenceTowardsOthers,
      saraAssessmentId = saraAssessmentId,
    )
  }
}
