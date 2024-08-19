package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.arnsApi.model.ArnsScores
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.arnsApi.model.RiskSummary
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysAccommodation
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
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonApi.model.NomisAlert
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Alert
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Attitude
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Behaviour
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Health
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.LearningNeeds
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Lifestyle
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
)

fun OasysRoshFull.toModel() = RoshAnalysis(
  offenceDetails = currentOffenceDetails,
  whereAndWhen = currentWhereAndWhen,
  howDone = currentHowDone,
  whoVictims = currentWhoVictims,
  anyoneElsePresent = currentAnyoneElsePresent,
  whyDone = currentWhyDone,
  sources = currentSources,
)

fun OasysLifestyle.toModel() = Lifestyle(
  activitiesEncourageOffending = regActivitiesEncourageOffending,
  lifestyleIssues = lifestyleIssuesDetails,
  easilyInfluenced = easilyInfluenced,
)

fun OasysBehaviour.toModel(): Behaviour {
  return Behaviour(
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
}

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

fun learningNeeds(oasysAccommodation: OasysAccommodation?, oasysLearning: OasysLearning?) = LearningNeeds(
  noFixedAbodeOrTransient = oasysAccommodation?.noFixedAbodeOrTransient == YES,
  workRelatedSkills = oasysLearning?.workRelatedSkills,
  problemsReadWriteNum = oasysLearning?.problemsReadWriteNum,
  learningDifficulties = oasysLearning?.learningDifficulties,
  qualifications = oasysLearning?.qualifications,
  basicSkillsScore = oasysLearning?.basicSkillsScore,
  basicSkillsScoreDescription = oasysLearning?.eTEIssuesDetails,
)

fun risks(
  oasysOffendingInfo: OasysOffendingInfo?,
  oasysRelationships: OasysRelationships?,
  oasysRoshSummary: OasysRoshSummary?,
  oasysArnsSummary: RiskSummary?,
  oasysArnsPredictor: ArnsScores?,
  activeAlerts: List<NomisAlert>?,
) = Risks(
  ogrsYear1 = oasysArnsPredictor?.groupReconvictionScore?.oneYear,
  ogrsYear2 = oasysArnsPredictor?.groupReconvictionScore?.twoYears,
  ogrsRisk = oasysArnsPredictor?.groupReconvictionScore?.scoreLevel?.fixCase(),

  ovpYear1 = oasysArnsPredictor?.violencePredictorScore?.oneYear,
  ovpYear2 = oasysArnsPredictor?.violencePredictorScore?.twoYears,
  ovpRisk = oasysArnsPredictor?.violencePredictorScore?.ovpRisk?.fixCase(),
  rsrScore = oasysArnsPredictor?.riskOfSeriousRecidivismScore?.percentageScore,
  rsrRisk = oasysArnsPredictor?.riskOfSeriousRecidivismScore?.scoreLevel?.fixCase(),

  ospcScore = oasysOffendingInfo?.ospDCRisk ?: oasysOffendingInfo?.ospCRisk,
  ospiScore = oasysOffendingInfo?.ospIICRisk ?: oasysOffendingInfo?.ospIRisk,

  riskPrisonersCustody = oasysRoshSummary?.riskPrisonersCustody?.type,
  riskStaffCustody = oasysRoshSummary?.riskStaffCustody?.type,
  riskStaffCommunity = oasysRoshSummary?.riskStaffCommunity?.type,
  riskKnownAdultCustody = oasysRoshSummary?.riskKnownAdultCustody?.type,
  riskKnownAdultCommunity = oasysRoshSummary?.riskKnownAdultCommunity?.type,
  riskPublicCustody = oasysRoshSummary?.riskPublicCustody?.type,
  riskPublicCommunity = oasysRoshSummary?.riskPublicCommunity?.type,
  riskChildrenCustody = oasysRoshSummary?.riskChildrenCustody?.type,
  riskChildrenCommunity = oasysRoshSummary?.riskChildrenCommunity?.type,

  imminentRiskOfViolenceTowardsOthers = oasysRelationships?.sara?.imminentRiskOfViolenceTowardsOthers,
  imminentRiskOfViolenceTowardsPartner = oasysRelationships?.sara?.imminentRiskOfViolenceTowardsPartner,

  overallRoshLevel = oasysArnsSummary?.overallRiskLevel?.fixCase(),
  alerts = activeAlerts?.map {
    Alert(
      description = it.alertCodeDescription,
      alertType = it.alertTypeDescription,
      dateCreated = it.dateCreated,
    )
  }?.distinctBy {
    Triple(it.description, it.alertType, it.dateCreated)
  },
)

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
