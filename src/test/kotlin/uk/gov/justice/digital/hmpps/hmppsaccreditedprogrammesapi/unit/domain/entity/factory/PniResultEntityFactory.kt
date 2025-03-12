package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.view.PniResultEntity
import java.time.LocalDateTime
import java.util.UUID

class PniResultEntityFactory {
  private var prisonNumber: String = "A1234BC"
  private var crn: String? = null
  private var referralId: UUID? = null
  private var oasysAssessmentId: Long? = null
  private var oasysAssessmentCompletedDate: LocalDateTime? = null
  private var programmePathway: String? = null
  private var needsClassification: String? = null
  private var overallNeedsScore: Int? = null
  private var riskClassification: String? = null
  private var pniAssessmentDate: LocalDateTime? = null
  private var pniValid: Boolean = true
  private var pniResultJson: String? = null
  private var basicSkillsScore: Int? = null

  fun withPrisonNumber(prisonNumber: String) = apply { this.prisonNumber = prisonNumber }
  fun withCrn(crn: String?) = apply { this.crn = crn }
  fun withReferralId(referralId: UUID?) = apply { this.referralId = referralId }
  fun withOasysAssessmentId(oasysAssessmentId: Long?) = apply { this.oasysAssessmentId = oasysAssessmentId }
  fun withOasysAssessmentCompletedDate(oasysAssessmentCompletedDate: LocalDateTime?) = apply { this.oasysAssessmentCompletedDate = oasysAssessmentCompletedDate }
  fun withProgrammePathway(programmePathway: String?) = apply { this.programmePathway = programmePathway }
  fun withNeedsClassification(needsClassification: String?) = apply { this.needsClassification = needsClassification }
  fun withOverallNeedsScore(overallNeedsScore: Int?) = apply { this.overallNeedsScore = overallNeedsScore }
  fun withRiskClassification(riskClassification: String?) = apply { this.riskClassification = riskClassification }
  fun withPniAssessmentDate(pniAssessmentDate: LocalDateTime?) = apply { this.pniAssessmentDate = pniAssessmentDate }
  fun withPniValid(pniValid: Boolean) = apply { this.pniValid = pniValid }
  fun withPniResultJson(pniResultJson: String?) = apply { this.pniResultJson = pniResultJson }
  fun withBasicSkillsScore(basicSkillsScore: Int?) = apply { this.basicSkillsScore = basicSkillsScore }

  fun produce() = PniResultEntity(
    prisonNumber = this.prisonNumber,
    crn = this.crn,
    referralId = this.referralId,
    oasysAssessmentId = this.oasysAssessmentId,
    oasysAssessmentCompletedDate = this.oasysAssessmentCompletedDate,
    programmePathway = this.programmePathway,
    needsClassification = this.needsClassification,
    overallNeedsScore = this.overallNeedsScore,
    riskClassification = this.riskClassification,
    pniAssessmentDate = this.pniAssessmentDate,
    pniValid = this.pniValid,
    pniResultJson = this.pniResultJson,
    basicSkillsScore = this.basicSkillsScore,
  )
}
