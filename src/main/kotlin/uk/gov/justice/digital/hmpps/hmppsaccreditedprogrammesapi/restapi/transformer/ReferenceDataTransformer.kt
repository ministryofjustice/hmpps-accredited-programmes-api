package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.ReferralStatusEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.ReferralStatusReasonProjection
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.SexualOffenceDetailsEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ReferralStatusReason
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ReferralStatusRefData
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.SexualOffenceDetails

fun ReferralStatusEntity.toModel(altDescription: String?, altHintText: String?) = ReferralStatusRefData(
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

fun ReferralStatusReasonProjection.toModel() = ReferralStatusReason(
  code = getCode(),
  description = getDescription(),
  referralCategoryCode = getReferralCategoryCode(),
  categoryDescription = getCategoryDescription(),
)

fun SexualOffenceDetailsEntity.toModel() = SexualOffenceDetails(
  id = id!!,
  description = description,
  categoryCode = category.name,
  categoryDescription = category.description,
  hintText = hintText,
  score = score,
)
