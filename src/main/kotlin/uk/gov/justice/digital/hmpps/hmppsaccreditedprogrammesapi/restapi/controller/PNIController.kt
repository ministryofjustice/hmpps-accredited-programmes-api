package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.DomainScore
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ErrorResponse
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.IndividualCognitiveScores
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.IndividualRelationshipScores
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.IndividualRiskScores
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.IndividualSelfManagementScores
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.IndividualSexScores
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.NeedsScore
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.PniScore
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.RelationshipDomainScore
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.RiskScore
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Sara
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.SelfManagementDomainScore
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.SexDomainScore
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ThinkingDomainScore
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.PniService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.type.SaraRisk
import java.util.UUID

@RestController
@Tag(name = "PNI")
class PNIController(
  private val pniService: PniService,
) {

  @Operation(
    tags = ["PNI"],
    summary = "Get needs and risk data for prisoner",
    operationId = "getPNIByPrisonNumber",
    description = """Get needs (sex, cognitive, relationships & Self Management) and risk data for given prisoner""",
    responses = [
      ApiResponse(responseCode = "200", description = "successful operation", content = [Content(schema = Schema(implementation = PniScore::class))]),
      ApiResponse(responseCode = "401", description = "Unauthorised. The request was unauthorised.", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
      ApiResponse(responseCode = "403", description = "Forbidden.  The client is not authorised to access person.", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
      ApiResponse(responseCode = "404", description = "Invalid prison number", content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
    ],
  )
  @RequestMapping(
    method = [RequestMethod.GET],
    value = ["/PNI/{prisonNumber}"],
    produces = ["application/json"],
  )
  fun getPNIByPrisonNumber(
    @Parameter(description = "Prison nomis identifier", required = true) @PathVariable("prisonNumber") prisonNumber: String,
    @Parameter(description = "Gender of the prisoner", required = false) @RequestParam("gender", required = false) gender: String?,
    @Parameter(description = "save pni result to DB", required = false) @RequestParam("savePNI", required = false) savePNI: Boolean = false,
    @Parameter(description = "referral id", required = false) @RequestParam("referralId", required = false) referralId: UUID?,
  ): ResponseEntity<PniScore> {
    if (prisonNumber == "A4128EA") {
      return ResponseEntity.ok(
        PniScore(
          prisonNumber = prisonNumber,
          crn = "X739590",
          assessmentId = 2114584,
          programmePathway = "ALTERNATIVE_PATHWAY",
          needsScore = NeedsScore(
            overallNeedsScore = 6,
            basicSkillsScore = 33,
            classification = "LOW_NEED",
            domainScore = DomainScore(
              sexDomainScore = SexDomainScore(
                overAllSexDomainScore = 2,
                individualSexScores = IndividualSexScores(
                  sexualPreOccupation = 2,
                  offenceRelatedSexualInterests = 2,
                  emotionalCongruence = 0,
                ),
              ),
              thinkingDomainScore = ThinkingDomainScore(
                overallThinkingDomainScore = 1,
                individualThinkingScores = IndividualCognitiveScores(
                  proCriminalAttitudes = 1,
                  hostileOrientation = 1,
                ),
              ),
              relationshipDomainScore = RelationshipDomainScore(
                overallRelationshipDomainScore = 1,
                individualRelationshipScores = IndividualRelationshipScores(
                  curRelCloseFamily = 0,
                  prevExpCloseRel = 2,
                  easilyInfluenced = 1,
                  aggressiveControllingBehaviour = 1,
                ),
              ),
              selfManagementDomainScore = SelfManagementDomainScore(
                overallSelfManagementDomainScore = 2,
                individualSelfManagementScores = IndividualSelfManagementScores(
                  impulsivity = 1,
                  temperControl = 4,
                  problemSolvingSkills = 2,
                  difficultiesCoping = 2,
                ),
              ),
            ),
          ),
          validationErrors = listOf(),
          riskScore = RiskScore(
            classification = "HIGH_RISK",
            individualRiskScores = IndividualRiskScores(
              ogrs3 = "15.00".toBigDecimal(),
              ovp = "15.00".toBigDecimal(),
              ospDc = "High",
              ospIic = "Medium",
              rsr = 1.46.toBigDecimal(),
              sara = Sara(
                overallResult = SaraRisk.HIGH,
                saraRiskOfViolenceTowardsOthers = "High",
                saraRiskOfViolenceTowardsPartner = "High",
                saraAssessmentId = 2114999,
              ),
            ),
          ),
        ),
      )
    }

    if (prisonNumber == "A9574EA") {
      return ResponseEntity.ok(
        PniScore(
          prisonNumber = prisonNumber,
          crn = "X739590",
          assessmentId = 2114584,
          programmePathway = "MODERATE_INTENSITY_BC",
          needsScore = NeedsScore(
            overallNeedsScore = 6,
            basicSkillsScore = 33,
            classification = "MEDIUM_NEED",
            domainScore = DomainScore(
              sexDomainScore = SexDomainScore(
                overAllSexDomainScore = 2,
                individualSexScores = IndividualSexScores(
                  sexualPreOccupation = 2,
                  offenceRelatedSexualInterests = 2,
                  emotionalCongruence = 0,
                ),
              ),
              thinkingDomainScore = ThinkingDomainScore(
                overallThinkingDomainScore = 1,
                individualThinkingScores = IndividualCognitiveScores(
                  proCriminalAttitudes = 1,
                  hostileOrientation = 1,
                ),
              ),
              relationshipDomainScore = RelationshipDomainScore(
                overallRelationshipDomainScore = 1,
                individualRelationshipScores = IndividualRelationshipScores(
                  curRelCloseFamily = 0,
                  prevExpCloseRel = 2,
                  easilyInfluenced = 1,
                  aggressiveControllingBehaviour = 1,
                ),
              ),
              selfManagementDomainScore = SelfManagementDomainScore(
                overallSelfManagementDomainScore = 2,
                individualSelfManagementScores = IndividualSelfManagementScores(
                  impulsivity = 1,
                  temperControl = 4,
                  problemSolvingSkills = 2,
                  difficultiesCoping = 2,
                ),
              ),
            ),
          ),
          validationErrors = listOf(),
          riskScore = RiskScore(
            classification = "HIGH_RISK",
            individualRiskScores = IndividualRiskScores(
              ogrs3 = "15.00".toBigDecimal(),
              ovp = "15.00".toBigDecimal(),
              ospDc = "High",
              ospIic = "Medium",
              rsr = 1.46.toBigDecimal(),
              sara = Sara(
                overallResult = SaraRisk.HIGH,
                saraRiskOfViolenceTowardsOthers = "High",
                saraRiskOfViolenceTowardsPartner = "High",
                saraAssessmentId = 2114999,
              ),
            ),
          ),
        ),
      )
    }

    if (prisonNumber == "A4433DZ") {
      return ResponseEntity.ok(
        PniScore(
          prisonNumber = prisonNumber,
          crn = "X739590",
          assessmentId = 2114584,
          programmePathway = "HIGH_INTENSITY_BC",
          needsScore = NeedsScore(
            overallNeedsScore = 6,
            basicSkillsScore = 33,
            classification = "HIGH_NEED",
            domainScore = DomainScore(
              sexDomainScore = SexDomainScore(
                overAllSexDomainScore = 2,
                individualSexScores = IndividualSexScores(
                  sexualPreOccupation = 2,
                  offenceRelatedSexualInterests = 2,
                  emotionalCongruence = 0,
                ),
              ),
              thinkingDomainScore = ThinkingDomainScore(
                overallThinkingDomainScore = 1,
                individualThinkingScores = IndividualCognitiveScores(
                  proCriminalAttitudes = 1,
                  hostileOrientation = 1,
                ),
              ),
              relationshipDomainScore = RelationshipDomainScore(
                overallRelationshipDomainScore = 1,
                individualRelationshipScores = IndividualRelationshipScores(
                  curRelCloseFamily = 0,
                  prevExpCloseRel = 2,
                  easilyInfluenced = 1,
                  aggressiveControllingBehaviour = 1,
                ),
              ),
              selfManagementDomainScore = SelfManagementDomainScore(
                overallSelfManagementDomainScore = 2,
                individualSelfManagementScores = IndividualSelfManagementScores(
                  impulsivity = 1,
                  temperControl = 4,
                  problemSolvingSkills = 2,
                  difficultiesCoping = 2,
                ),
              ),
            ),
          ),
          validationErrors = listOf(),
          riskScore = RiskScore(
            classification = "HIGH_RISK",
            individualRiskScores = IndividualRiskScores(
              ogrs3 = "15.00".toBigDecimal(),
              ovp = "15.00".toBigDecimal(),
              ospDc = "High",
              ospIic = "Medium",
              rsr = 1.46.toBigDecimal(),
              sara = Sara(
                overallResult = SaraRisk.HIGH,
                saraRiskOfViolenceTowardsOthers = "High",
                saraRiskOfViolenceTowardsPartner = "High",
                saraAssessmentId = 2114999,
              ),
            ),
          ),
        ),
      )
    }

    if (prisonNumber == "A5666EA") {
      return ResponseEntity.ok(
        PniScore(
          prisonNumber = prisonNumber,
          crn = "X739590",
          assessmentId = 2114584,
          programmePathway = "MISSING_INFORMATION",
          needsScore = NeedsScore(
            overallNeedsScore = 6,
            basicSkillsScore = 33,
            classification = "HIGH_NEED",
            domainScore = DomainScore(
              sexDomainScore = SexDomainScore(
                overAllSexDomainScore = 2,
                individualSexScores = IndividualSexScores(
                  sexualPreOccupation = 2,
                  offenceRelatedSexualInterests = 2,
                  emotionalCongruence = 0,
                ),
              ),
              thinkingDomainScore = ThinkingDomainScore(
                overallThinkingDomainScore = 1,
                individualThinkingScores = IndividualCognitiveScores(
                  proCriminalAttitudes = 1,
                  hostileOrientation = 1,
                ),
              ),
              relationshipDomainScore = RelationshipDomainScore(
                overallRelationshipDomainScore = 1,
                individualRelationshipScores = IndividualRelationshipScores(
                  curRelCloseFamily = 0,
                  prevExpCloseRel = 2,
                  easilyInfluenced = 1,
                  aggressiveControllingBehaviour = 1,
                ),
              ),
              selfManagementDomainScore = SelfManagementDomainScore(
                overallSelfManagementDomainScore = 2,
                individualSelfManagementScores = IndividualSelfManagementScores(
                  impulsivity = 1,
                  temperControl = 4,
                  problemSolvingSkills = 2,
                  difficultiesCoping = null,
                ),
              ),
            ),
          ),
          validationErrors = listOf("difficultiesCoping in SelfManagementScores is null"),
          riskScore = RiskScore(
            classification = "HIGH_RISK",
            individualRiskScores = IndividualRiskScores(
              ogrs3 = "15.00".toBigDecimal(),
              ovp = "15.00".toBigDecimal(),
              ospDc = "High",
              ospIic = "Medium",
              rsr = 1.46.toBigDecimal(),
              sara = Sara(
                overallResult = SaraRisk.HIGH,
                saraRiskOfViolenceTowardsOthers = "High",
                saraRiskOfViolenceTowardsPartner = "High",
                saraAssessmentId = 2114999,
              ),
            ),
          ),
        ),
      )
    }

    return ResponseEntity.ok(
      pniService.getPniScore(
        prisonNumber = prisonNumber,
        gender = gender,
        savePni = savePNI,
        referralId = referralId,
      ),
    )
  }
}
