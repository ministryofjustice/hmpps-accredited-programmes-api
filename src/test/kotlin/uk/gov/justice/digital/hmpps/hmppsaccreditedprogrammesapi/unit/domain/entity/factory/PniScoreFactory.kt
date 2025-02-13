package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.NeedsScore
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.PniScore
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.RiskScore

class PniScoreFactory {
  private var prisonNumber: String = "A1234BC"
  private var crn: String? = "D602550"
  private var assessmentId: Long = 2512235167
  private var programmePathway: String = "HIGH_INTENSITY_BC"
  private var needsScore: NeedsScore = NeedsScoreFactory().produce()
  private var riskScore: RiskScore = RiskScoreFactory().produce()
  private var validationErrors: List<String> = listOf("impulsivity is missing")

  fun withPrisonNumber(prisonNumber: String) = apply { this.prisonNumber = prisonNumber }
  fun withCrn(crn: String?) = apply { this.crn = crn }
  fun withAssessmentId(assessmentId: Long) = apply { this.assessmentId = assessmentId }
  fun withProgrammePathway(programmePathway: String) = apply { this.programmePathway = programmePathway }
  fun withNeedsScore(needsScore: NeedsScore) = apply { this.needsScore = needsScore }
  fun withRiskScore(riskScore: RiskScore) = apply { this.riskScore = riskScore }
  fun withValidationErrors(validationErrors: List<String>) = apply { this.validationErrors = validationErrors }

  fun produce() = PniScore(
    prisonNumber = prisonNumber,
    crn = crn,
    assessmentId = assessmentId,
    programmePathway = programmePathway,
    needsScore = needsScore,
    riskScore = riskScore,
    validationErrors = validationErrors,
  )
}
