package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.update

import java.util.UUID

class AuditUpdate(
  var referralId: UUID? = null,
  var prisonNumber: String,
  var prisonerLocation: String? = null,
  var referrerUsername: String? = null,
  var referralStatusFrom: String? = null,
  var referralStatusTo: String? = null,
  var courseName: String? = null,
  var courseLocation: String? = null,
  var auditAction: String,
)
