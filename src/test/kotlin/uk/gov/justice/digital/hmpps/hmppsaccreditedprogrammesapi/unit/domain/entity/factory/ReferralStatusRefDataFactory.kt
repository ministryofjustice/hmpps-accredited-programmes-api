package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ReferralStatusRefData

class ReferralStatusRefDataFactory {

  private var code: String = "ASSESSMENT_STARTED"
  private var description: String = "Assessment started"
  private var colour: String = "blue"
  private var hintText: String? = "This person is being assessed by the programme team for suitability."
  private var confirmationText: String? = "You can give more details about this status update."
  private var hasNotes: Boolean? = false
  private var hasConfirmation: Boolean? = false
  private var closed: Boolean? = false
  private var draft: Boolean? = false
  private var hold: Boolean? = false
  private var release: Boolean? = true
  private var deselectAndKeepOpen: Boolean? = false
  private var defaultOrder: Int? = 60
  private var notesOptional: Boolean? = true

  fun withCode(code: String) = apply { this.code = code }
  fun withDescription(description: String) = apply { this.description = description }
  fun withColour(colour: String) = apply { this.colour = colour }
  fun withHintText(hintText: String?) = apply { this.hintText = hintText }
  fun withConfirmationText(confirmationText: String?) = apply { this.confirmationText = confirmationText }
  fun withHasNotes(hasNotes: Boolean?) = apply { this.hasNotes = hasNotes }
  fun withHasConfirmation(hasConfirmation: Boolean?) = apply { this.hasConfirmation = hasConfirmation }
  fun withClosed(closed: Boolean?) = apply { this.closed = closed }
  fun withDraft(draft: Boolean?) = apply { this.draft = draft }
  fun withHold(hold: Boolean?) = apply { this.hold = hold }
  fun withRelease(release: Boolean?) = apply { this.release = release }
  fun withDeselectAndKeepOpen(deselectAndKeepOpen: Boolean?) = apply { this.deselectAndKeepOpen = deselectAndKeepOpen }
  fun withDefaultOrder(defaultOrder: Int?) = apply { this.defaultOrder = defaultOrder }
  fun withNotesOptional(notesOptional: Boolean?) = apply { this.notesOptional = notesOptional }

  fun produce() = ReferralStatusRefData(
    code = code,
    description = description,
    colour = colour,
    hintText = hintText,
    confirmationText = confirmationText,
    hasNotes = hasNotes,
    hasConfirmation = hasConfirmation,
    closed = closed,
    draft = draft,
    hold = hold,
    release = release,
    deselectAndKeepOpen = deselectAndKeepOpen,
    defaultOrder = defaultOrder,
    notesOptional = notesOptional,
  )
}
