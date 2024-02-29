package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralStatusRefData
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.ReferralStatusEntity

fun ReferralStatusEntity.toModel() =
  ReferralStatusRefData(code = code, description = description, colour = colour, hintText = hintText, confirmationText = confirmationText, closed = closed, draft = draft, hasNotes = hasNotes, hasConfirmation = hasConfirmation)
