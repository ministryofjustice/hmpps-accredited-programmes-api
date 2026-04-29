package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.REFERRAL_SUBMITTED
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.REFERRAL_SUBMITTED_COLOUR
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.REFERRAL_SUBMITTED_DESCRIPTION
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.ReferralStatusEntity

class ReferralStatusEntityFactory {
  private var code: String = REFERRAL_SUBMITTED
  private var description: String = REFERRAL_SUBMITTED_DESCRIPTION
  private var hintText: String = "Hint"
  private var colour: String = REFERRAL_SUBMITTED_COLOUR
  private var hasNotes: Boolean = false
  private var hasConfirmation: Boolean = false
  private var confirmationText: String = "Confirmation"
  private var active: Boolean = true
  private var draft: Boolean = false
  private var closed: Boolean = false
  private var hold: Boolean = false
  private var release: Boolean = false
  private var defaultOrder: Int = 1
  private var notesOptional: Boolean = true
  private var caseNotesSubtype: String = "Subtype"
  private var caseNotesMessage: String = "Message"

  fun withCode(code: String) = apply { this.code = code }
  fun withDescription(description: String) = apply { this.description = description }
  fun withHintText(hintText: String) = apply { this.hintText = hintText }
  fun withColour(colour: String) = apply { this.colour = colour }
  fun withHasNotes(hasNotes: Boolean) = apply { this.hasNotes = hasNotes }
  fun withHasConfirmation(hasConfirmation: Boolean) = apply { this.hasConfirmation = hasConfirmation }
  fun withConfirmationText(confirmationText: String) = apply { this.confirmationText = confirmationText }
  fun withActive(active: Boolean) = apply { this.active = active }
  fun withDraft(draft: Boolean) = apply { this.draft = draft }
  fun withClosed(closed: Boolean) = apply { this.closed = closed }
  fun withHold(hold: Boolean) = apply { this.hold = hold }
  fun withRelease(release: Boolean) = apply { this.release = release }
  fun withDefaultOrder(defaultOrder: Int) = apply { this.defaultOrder = defaultOrder }
  fun withNotesOptional(notesOptional: Boolean) = apply { this.notesOptional = notesOptional }
  fun withCaseNotesSubtype(caseNotesSubtype: String) = apply { this.caseNotesSubtype = caseNotesSubtype }
  fun withCaseNotesMessage(caseNotesMessage: String) = apply { this.caseNotesMessage = caseNotesMessage }

  fun produce() = ReferralStatusEntity(
    code = this.code,
    description = this.description,
    hintText = this.hintText,
    colour = this.colour,
    hasNotes = this.hasNotes,
    hasConfirmation = this.hasConfirmation,
    confirmationText = this.confirmationText,
    active = this.active,
    draft = this.draft,
    closed = this.closed,
    hold = this.hold,
    release = this.release,
    defaultOrder = this.defaultOrder,
    notesOptional = this.notesOptional,
    caseNotesSubtype = this.caseNotesSubtype,
    caseNotesMessage = this.caseNotesMessage,
  )
}
