package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Alert
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Attitude
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Behaviour
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Health
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.LearningNeeds
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Lifestyle
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.OffenceDetail
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Psychiatric
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Relationships
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Risks
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.RoshAnalysis
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.arnsApi.model.ArnsScores
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.arnsApi.model.ArnsSummary
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

fun OasysOffenceDetail.toModel(): OffenceDetail {
  return OffenceDetail(
    offenceAnalysis,
    whatOccurred?.contains(WhatOccurred.TARGETING.desc),
    whatOccurred?.contains(WhatOccurred.RACIAL.desc),
    whatOccurred?.contains(WhatOccurred.REVENGE.desc),
    whatOccurred?.contains(WhatOccurred.DOMESTIC_VIOLENCE.desc),
    whatOccurred?.contains(WhatOccurred.VICTIMISATION.desc),
    whatOccurred?.contains(WhatOccurred.STRANGER.desc),
    whatOccurred?.contains(WhatOccurred.STALKING.desc),
    recognisesImpact == YES,
    numberOfOthersInvolved,
    othersInvolved,
    peerGroupInfluences,
    offenceMotivation,
    acceptsResponsibility == YES,
    patternOffending,
  )
}

fun OasysRelationships.toModel(): Relationships {
  return Relationships(
    prevOrCurrentDomesticAbuse == YES,
    victimOfPartner == YES,
    victimOfFamily == YES,
    perpAgainstFamily == YES,
    perpAgainstPartner == YES,
    relIssuesDetails,
  )
}

fun OasysRoshFull.toModel(): RoshAnalysis {
  return RoshAnalysis(
    currentOffenceDetails,
    currentWhereAndWhen,
    currentHowDone,
    currentWhoVictims,
    currentAnyoneElsePresent,
    currentWhyDone,
    currentSources,
  )
}

fun OasysLifestyle.toModel(): Lifestyle {
  return Lifestyle(
    regActivitiesEncourageOffending,
    lifestyleIssuesDetails,
  )
}

fun OasysBehaviour.toModel(): Behaviour {
  return Behaviour(
    temperControl,
    problemSolvingSkills,
    awarenessOfConsequences,
    achieveGoals,
    understandsViewsOfOthers,
    concreteAbstractThinking,
  )
}

fun OasysPsychiatric.toModel(): Psychiatric {
  return Psychiatric(
    currPsychiatricProblems,
  )
}

fun OasysHealth.toModel(): Health {
  return Health(
    generalHealth == YES,
    generalHeathSpecify,
  )
}

fun OasysAttitude.toModel(): Attitude {
  return Attitude(
    proCriminalAttitudes,
    motivationToAddressBehaviour,
  )
}

fun learningNeeds(oasysAccommodation: OasysAccommodation?, oasysLearning: OasysLearning?) = LearningNeeds(
  oasysAccommodation?.noFixedAbodeOrTransient == YES,
  oasysLearning?.workRelatedSkills,
  oasysLearning?.problemsReadWriteNum,
  oasysLearning?.learningDifficulties,
  oasysLearning?.qualifications,
  oasysLearning?.basicSkillsScore,
)

fun risks(
  oasysOffendingInfo: OasysOffendingInfo?,
  oasysRelationships: OasysRelationships?,
  oasysRoshSummary: OasysRoshSummary?,
  oasysArnsSummary: ArnsSummary?,
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

  ospcScore = oasysOffendingInfo?.ospCRisk,
  ospiScore = oasysOffendingInfo?.ospIRisk,

  riskPrisonersCustody = oasysRoshSummary?.riskPrisonersCustody,
  riskStaffCustody = oasysRoshSummary?.riskStaffCustody,
  riskStaffCommunity = oasysRoshSummary?.riskStaffCommunity,
  riskKnownAdultCustody = oasysRoshSummary?.riskKnownAdultCustody,
  riskKnownAdultCommunity = oasysRoshSummary?.riskKnownAdultCommunity,
  riskPublicCustody = oasysRoshSummary?.riskPublicCustody,
  riskPublicCommunity = oasysRoshSummary?.riskPublicCommunity,
  riskChildrenCustody = oasysRoshSummary?.riskChildrenCustody,
  riskChildrenCommunity = oasysRoshSummary?.riskChildrenCommunity,

  imminentRiskOfViolenceTowardsOthers = oasysRelationships?.sara?.imminentRiskOfViolenceTowardsOthers,
  imminentRiskOfViolenceTowardsPartner = oasysRelationships?.sara?.imminentRiskOfViolenceTowardsPartner,

  overallRoshLevel = oasysArnsSummary?.overallRiskLevel?.fixCase(),
  alerts = activeAlerts?.map { Alert(it.alertTypeDescription) },
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
