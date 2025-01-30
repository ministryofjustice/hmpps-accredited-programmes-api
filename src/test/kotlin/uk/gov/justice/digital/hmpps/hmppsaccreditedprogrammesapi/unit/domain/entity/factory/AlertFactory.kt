package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonerAlertsApi.model.Alert
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonerAlertsApi.model.AlertCodeSummary
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.randomLowercaseString
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class AlertFactory {
  private var alertUuid: UUID? = UUID.randomUUID()
  private var prisonNumber: String = randomLowercaseString()
  private var alertCode: AlertCodeSummary? = createAlertSummary()
  private var description: String? = randomLowercaseString()
  private var authorisedBy: String? = randomLowercaseString()
  private var activeFrom: LocalDate? = LocalDateTime.now().minusDays(1).toLocalDate()
  private var activeTo: LocalDate? = LocalDateTime.now().plusDays(1).toLocalDate()
  private var isActive: Boolean = true
  private var createdAt: LocalDateTime? = LocalDateTime.now()

  private fun createAlertSummary(): AlertCodeSummary =
    AlertCodeSummary(
      alertTypeCode = randomLowercaseString(),
      alertTypeDescription = randomLowercaseString(),
      code = randomLowercaseString(),
      description = randomLowercaseString(),
    )

  fun withAlertUuid(alertUuid: UUID) = apply { this.alertUuid = alertUuid }
  fun withPrisonNumber(prisonNumber: String) = apply { this.prisonNumber = prisonNumber }
  fun withAlertCode(alertCode: AlertCodeSummary) = apply { this.alertCode = alertCode }
  fun withDescription(description: String?) = apply { this.description = description }
  fun withAuthorisedBy(authorisedBy: String?) = apply { this.authorisedBy = authorisedBy }
  fun withActiveFrom(activeFrom: LocalDate) = apply { this.activeFrom = activeFrom }
  fun withActiveTo(activeTo: LocalDate?) = apply { this.activeTo = activeTo }
  fun withIsActive(isActive: Boolean) = apply { this.isActive = isActive }
  fun withCreatedAt(createdAt: LocalDateTime) = apply { this.createdAt = createdAt }

  fun build() = Alert(
    alertUuid = this.alertUuid!!,
    prisonNumber = this.prisonNumber,
    alertCode = this.alertCode!!,
    description = this.description,
    authorisedBy = this.authorisedBy,
    activeFrom = this.activeFrom!!,
    activeTo = this.activeTo,
    isActive = this.isActive,
    createdAt = this.createdAt!!,
  )
}
