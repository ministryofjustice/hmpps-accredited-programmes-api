package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.ReferralStatusRefData
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.ReferralStatusEntity

fun ReferralStatusEntity.toModel(altDescription: String?, altHintText: String?) =
  ReferralStatusRefData(
    code = code,
    description = altDescription ?: description,
    colour = colour,
    hintText = altHintText ?: hintText,
    confirmationText = confirmationText,
    closed = closed,
    draft = draft,
    hasNotes = hasNotes,
    hasConfirmation = hasConfirmation,
    hold = hold,
    release = release,
    deselectAndKeepOpen = false,
    notesOptional = notesOptional,
  )
