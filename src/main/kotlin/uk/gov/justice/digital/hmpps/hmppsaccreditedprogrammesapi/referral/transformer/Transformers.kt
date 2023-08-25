package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.referral.transformer

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Referral as ApiReferral
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.referral.domain.Referral as DomainReferral

fun DomainReferral.toApi(): ApiReferral = ApiReferral(
  id = id!!,
  offeringId = offeringId,
  prisonNumber = prisonNumber,
  referrerId = referrerId,
  oasysConfirmed = false,
  reason = null,
  status = ApiReferral.Status.rEFERRALSTARTED,
)
