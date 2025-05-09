package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.NeedLevel
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.RiskLevel
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.Timeline
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.Type
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.NotFoundException
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.AuditAction
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.view.PniResultEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.PNIResultEntityRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.DomainScore
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.IndividualRiskScores
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.NeedsScore
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.PniScore
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.RiskScore
import java.time.LocalDateTime
import java.util.UUID

private const val LEARNING_DISABILITIES_AND_CHALLENGES_THRESHOLD = 3

@Service
class PniService(
  private val oasysService: OasysService,
  private val auditService: AuditService,
  private val pniResultEntityRepository: PNIResultEntityRepository,
  private val objectMapper: ObjectMapper,
) {
  private val log = LoggerFactory.getLogger(this::class.java)

  fun savePni(pniScore: PniScore, referralId: UUID? = null) {
    log.info("Saving PNI score for prisonNumber: $pniScore.prisonNumber")

    val oasysAssessmentTimeline = oasysService.getAssessments(pniScore.prisonNumber)
    val completedAssessment = oasysService.getLatestCompletedLayerThreeAssessment(oasysAssessmentTimeline)
      ?: throw NotFoundException("No completed assessments found for $pniScore.prisonNumber")

    pniResultEntityRepository.save(buildEntity(pniScore, completedAssessment, referralId))
  }

  fun getOasysPniScore(prisonNumber: String): PniScore {
    log.info("Request received to process Oasys PNI for prisonNumber $prisonNumber")

    auditService.audit(prisonNumber = prisonNumber, auditAction = AuditAction.PNI.name)

    val pniResponse = oasysService.getPniCalculation(prisonNumber)
      ?: throw NotFoundException("No PNI information found for $prisonNumber")
    val learning = oasysService.getLearning(pniResponse.assessment?.id!!)

    // needs
    val needsScore = NeedsScore(
      overallNeedsScore = pniResponse.pniCalculation?.totalDomainScore,
      basicSkillsScore = pniResponse.assessment.ldc?.subTotal,
      classification = NeedLevel.fromLevel(pniResponse.pniCalculation?.needLevel).name,
      domainScore = DomainScore.from(pniResponse),
    )

    // risk
    val riskScore = RiskScore(
      classification = RiskLevel.fromLevel(pniResponse.pniCalculation?.riskLevel).name,
      individualRiskScores = IndividualRiskScores.from(pniResponse),
    )

    val pniScore = PniScore(
      prisonNumber = prisonNumber,
      crn = learning?.crn,
      assessmentId = pniResponse.assessment.id,
      programmePathway = Type.toPathway(pniResponse.pniCalculation?.pni).name,
      needsScore = needsScore,
      riskScore = riskScore,
      validationErrors = pniResponse.pniCalculation?.missingFields.orEmpty(),
    )
    return pniScore
  }

  private fun buildEntity(
    pniScore: PniScore,
    completedAssessment: Timeline?,
    referralId: UUID?,
  ): PniResultEntity = PniResultEntity(
    crn = pniScore.crn,
    prisonNumber = pniScore.prisonNumber,
    referralId = referralId,
    oasysAssessmentId = completedAssessment?.id,
    oasysAssessmentCompletedDate = completedAssessment?.completedAt,
    needsClassification = pniScore.needsScore.classification,
    riskClassification = pniScore.riskScore.classification,
    overallNeedsScore = pniScore.needsScore.overallNeedsScore,
    programmePathway = pniScore.programmePathway,
    pniValid = pniScore.validationErrors.isEmpty(),
    pniResultJson = objectMapper.writeValueAsString(pniScore),
    pniAssessmentDate = LocalDateTime.now(),
    basicSkillsScore = pniScore.needsScore.basicSkillsScore,
  )

  fun hasLDC(prisonNumber: String) = oasysService.getLDCScore(prisonNumber)?.let { it >= LEARNING_DISABILITIES_AND_CHALLENGES_THRESHOLD }

  fun deletePniData(referralIds: List<UUID>) {
    pniResultEntityRepository.deleteAllById(referralIds)
  }
}