package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer

import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AllPredictorVersionedDto
import uk.gov.justice.digital.hmpps.assessrisksandneeds.api.model.AllPredictorVersionedLegacyDto
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.arnsApi.model.AllPredictorVersioned
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysAttitude
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysBehaviour
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysHealth
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysLifestyle
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysOffenceDetail
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysOffendingInfo
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysPsychiatric
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysRelationships
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysRoshFull
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysRoshSummary
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.RiskSummary
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonerAlertsApi.model.Alert
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Attitude
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Behaviour
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Health
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Lifestyle
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.OGRS4Risks
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.OffenceDetail
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Psychiatric
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Relationships
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Risks
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.RoshAnalysis

fun OasysOffenceDetail.toModel() = OffenceDetail(
  offenceDetails = offenceAnalysis,
  contactTargeting = whatOccurred?.contains(WhatOccurred.TARGETING.desc),
  raciallyMotivated = whatOccurred?.contains(WhatOccurred.RACIAL.desc),
  revenge = whatOccurred?.contains(WhatOccurred.REVENGE.desc),
  domesticViolence = whatOccurred?.contains(WhatOccurred.DOMESTIC_VIOLENCE.desc),
  repeatVictimisation = whatOccurred?.contains(WhatOccurred.VICTIMISATION.desc),
  victimWasStranger = whatOccurred?.contains(WhatOccurred.STRANGER.desc),
  stalking = whatOccurred?.contains(WhatOccurred.STALKING.desc),
  recognisesImpact = recognisesImpact == YES,
  numberOfOthersInvolved = numberOfOthersInvolved,
  othersInvolvedDetail = othersInvolved,
  peerGroupInfluences = peerGroupInfluences,
  motivationAndTriggers = offenceMotivation,
  acceptsResponsibility = acceptsResponsibility == YES,
  acceptsResponsibilityDetail = patternOffending,
)

fun OasysRelationships.toModel() = Relationships(
  dvEvidence = prevOrCurrentDomesticAbuse == YES,
  victimFormerPartner = victimOfPartner == YES,
  victimFamilyMember = victimOfFamily == YES,
  victimOfPartnerFamily = perpAgainstFamily == YES,
  perpOfPartnerOrFamily = perpAgainstPartner == YES,
  relIssuesDetails = relIssuesDetails,
  relCloseFamily = relCloseFamily,
  relCurrRelationshipStatus = relCurrRelationshipStatus,
  prevCloseRelationships = prevCloseRelationships,
  emotionalCongruence = emotionalCongruence,
  relationshipWithPartner = relationshipWithPartner,
  prevOrCurrentDomesticAbuse = prevOrCurrentDomesticAbuse,
)

fun OasysRoshFull.toModel() = RoshAnalysis(
  offenceDetails = currentOffenceDetails,
  whereAndWhen = currentWhereAndWhen,
  howDone = currentHowDone,
  whoVictims = currentWhoVictims,
  anyoneElsePresent = currentAnyoneElsePresent,
  whyDone = currentWhyDone,
  sources = currentSources,
  identifyBehavioursIncidents = identifyBehavioursIncidents,
  analysisSuicideSelfHarm = analysisSuicideSelfHarm,
  analysisCoping = analysisCoping,
  analysisVulnerabilities = analysisVulnerabilities,
  analysisEscapeAbscond = analysisEscapeAbscond,
  analysisControlBehaveTrust = analysisControlBehaveTrust,
  analysisBehavioursIncidents = analysisBehavioursIncidents,
)

fun OasysLifestyle.toModel() = Lifestyle(
  activitiesEncourageOffending = regActivitiesEncourageOffending,
  lifestyleIssues = lifestyleIssuesDetails,
  easilyInfluenced = easilyInfluenced,
)

fun OasysBehaviour.toModel(): Behaviour = Behaviour(
  temperControl = temperControl,
  problemSolvingSkills = problemSolvingSkills,
  awarenessOfConsequences = awarenessOfConsequences,
  achieveGoals = achieveGoals,
  understandsViewsOfOthers = understandsViewsOfOthers,
  concreteAbstractThinking = concreteAbstractThinking,
  sexualPreOccupation = sexualPreOccupation,
  offenceRelatedSexualInterests = offenceRelatedSexualInterests,
  aggressiveControllingBehaviour = aggressiveControllingBehavour,
  impulsivity = impulsivity,
)

fun OasysPsychiatric.toModel() = Psychiatric(
  description = currPsychiatricProblems,
  difficultiesCoping = difficultiesCoping,
  currPsychologicalProblems = currPsychologicalProblems,
  selfHarmSuicidal = selfHarmSuicidal,
)

fun OasysHealth.toModel() = Health(
  anyHealthConditions = generalHealth == YES,
  description = generalHeathSpecify,
)

fun OasysAttitude.toModel() = Attitude(
  proCriminalAttitudes = proCriminalAttitudes,
  motivationToAddressBehaviour = motivationToAddressBehaviour,
  hostileOrientation = hostileOrientation,
)

fun buildRisks(
  oasysOffendingInfo: OasysOffendingInfo?,
  oasysRelationships: OasysRelationships?,
  oasysRoshSummary: OasysRoshSummary?,
  oasysRiskSummary: RiskSummary?,
  allPredictorVersioned: AllPredictorVersioned<Any>?,
  activeAlerts: List<Alert>?,
): Risks {
  // Map common fields to Risks model
  val risks = Risks(
    // SARA
    imminentRiskOfViolenceTowardsOthers = oasysRelationships?.sara?.imminentRiskOfViolenceTowardsOthers,
    imminentRiskOfViolenceTowardsPartner = oasysRelationships?.sara?.imminentRiskOfViolenceTowardsPartner,

    // ROSH
    overallRoshLevel = oasysRiskSummary?.overallRiskLevel?.fixCase(),
    riskPrisonersCustody = oasysRoshSummary?.riskPrisonersCustody?.type,
    riskStaffCustody = oasysRoshSummary?.riskStaffCustody?.type,
    riskStaffCommunity = oasysRoshSummary?.riskStaffCommunity?.type,
    riskKnownAdultCustody = oasysRoshSummary?.riskKnownAdultCustody?.type,
    riskKnownAdultCommunity = oasysRoshSummary?.riskKnownAdultCommunity?.type,
    riskPublicCustody = oasysRoshSummary?.riskPublicCustody?.type,
    riskPublicCommunity = oasysRoshSummary?.riskPublicCommunity?.type,
    riskChildrenCustody = oasysRoshSummary?.riskChildrenCustody?.type,
    riskChildrenCommunity = oasysRoshSummary?.riskChildrenCommunity?.type,

    // Alerts
    alerts = activeAlerts?.map {
      uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Alert(
        description = it.alertCode.description,
        alertType = it.alertCode.alertTypeDescription,
        dateCreated = it.createdAt.toLocalDate(),
      )
    }?.distinctBy {
      Triple(it.description, it.alertType, it.dateCreated)
    },
  )
  // For pre-OGRS4 assessments add the "legacy" fields
  return if (allPredictorVersioned is AllPredictorVersionedLegacyDto) {
    val allPredictorVersionedLegacyDto = allPredictorVersioned as AllPredictorVersionedLegacyDto?
    risks.copy(
      isLegacy = true,
      ogrsYear1 = allPredictorVersionedLegacyDto?.output?.groupReconvictionScore?.oneYear,
      ogrsYear2 = allPredictorVersionedLegacyDto?.output?.groupReconvictionScore?.twoYears,
      ogrsRisk = allPredictorVersionedLegacyDto?.output?.groupReconvictionScore?.scoreLevel?.type,
      ovpYear1 = allPredictorVersionedLegacyDto?.output?.violencePredictorScore?.oneYear,
      ovpYear2 = allPredictorVersionedLegacyDto?.output?.violencePredictorScore?.twoYears,
      ovpRisk = allPredictorVersionedLegacyDto?.output?.violencePredictorScore?.ovpRisk?.type,
      rsrScore = allPredictorVersionedLegacyDto?.output?.riskOfSeriousRecidivismScore?.percentageScore,
      rsrRisk = allPredictorVersionedLegacyDto?.output?.riskOfSeriousRecidivismScore?.scoreLevel?.type,

      ospcScore = oasysOffendingInfo?.ospDCRisk ?: oasysOffendingInfo?.ospCRisk,
      ospiScore = oasysOffendingInfo?.ospIICRisk ?: oasysOffendingInfo?.ospIRisk,
    )
  } else { // For post-OGRS4 assessments add the new fields
    val allPredictorVersionedDto = allPredictorVersioned as? AllPredictorVersionedDto
    risks.apply {
      isLegacy = false
      ogrs4Risks = OGRS4Risks(
        allReoffendingScoreType = allPredictorVersionedDto?.output?.allReoffendingPredictor?.staticOrDynamic?.type,
        allReoffendingScore = allPredictorVersionedDto?.output?.allReoffendingPredictor?.score,
        allReoffendingBand = allPredictorVersionedDto?.output?.allReoffendingPredictor?.band?.type,

        violentReoffendingScoreType = allPredictorVersionedDto?.output?.violentReoffendingPredictor?.staticOrDynamic?.type,
        violentReoffendingScore = allPredictorVersionedDto?.output?.violentReoffendingPredictor?.score,
        violentReoffendingBand = allPredictorVersionedDto?.output?.violentReoffendingPredictor?.band?.type,

        seriousViolentReoffendingScoreType = allPredictorVersionedDto?.output?.seriousViolentReoffendingPredictor?.staticOrDynamic?.type,
        seriousViolentReoffendingScore = allPredictorVersionedDto?.output?.seriousViolentReoffendingPredictor?.score,
        seriousViolentReoffendingBand = allPredictorVersionedDto?.output?.seriousViolentReoffendingPredictor?.band?.type,

        directContactSexualReoffendingScore = allPredictorVersionedDto?.output?.directContactSexualReoffendingPredictor?.score,
        directContactSexualReoffendingBand = allPredictorVersionedDto?.output?.directContactSexualReoffendingPredictor?.band?.type,

        indirectImageContactSexualReoffendingScore = allPredictorVersionedDto?.output?.indirectImageContactSexualReoffendingPredictor?.score,
        indirectImageContactSexualReoffendingBand = allPredictorVersionedDto?.output?.indirectImageContactSexualReoffendingPredictor?.band?.type,

        combinedSeriousReoffendingScoreType = allPredictorVersionedDto?.output?.combinedSeriousReoffendingPredictor?.staticOrDynamic?.type,
        combinedSeriousReoffendingScore = allPredictorVersionedDto?.output?.combinedSeriousReoffendingPredictor?.score,
        combinedSeriousReoffendingBand = allPredictorVersionedDto?.output?.combinedSeriousReoffendingPredictor?.band?.type,
      )
    }
  }
}

fun String.fixCase(): String = this.lowercase().replaceFirstChar(Char::titlecase)

enum class WhatOccurred(val desc: String) {
  TARGETING("Were there any direct victim(s) eg contact targeting"),
  RACIAL("Were any of the victim(s) targeted because of racial motivation or hatred of other identifiable group"),
  REVENGE("Response to a specific victim (eg revenge, settling grudges)"),
  DOMESTIC_VIOLENCE("Physical violence towards partner"),
  VICTIMISATION("Repeat victimisation of the same person"),
  STRANGER("Were the victim(s) stranger(s) to the offender"),
  STALKING("Stalking"),
}

private const val YES = "Yes"
