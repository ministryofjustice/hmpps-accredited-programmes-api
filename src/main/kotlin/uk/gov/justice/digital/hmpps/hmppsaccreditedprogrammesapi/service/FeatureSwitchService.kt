package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class FeatureSwitchService(@Value("\${feature-switch.case-notes-enabled}") val caseNotesEnabled: Boolean) {
  fun isCaseNotesEnabled(): Boolean = caseNotesEnabled
}
