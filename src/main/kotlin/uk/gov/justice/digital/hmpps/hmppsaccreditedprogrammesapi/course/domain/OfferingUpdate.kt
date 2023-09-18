package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.domain

data class OfferingUpdate(
  val prisonId: String,
  val identifier: String,
  val contactEmail: String? = null,
  val secondaryContactEmail: String? = null,
)
