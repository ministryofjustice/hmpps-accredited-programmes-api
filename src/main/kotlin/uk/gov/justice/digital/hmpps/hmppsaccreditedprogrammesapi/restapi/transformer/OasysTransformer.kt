package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Lifestyle
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.OffenceDetail
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Relationships
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.RoshAnalysis
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysLifestyle
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysOffenceDetail
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysRelationships
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model.OasysRoshFull

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
