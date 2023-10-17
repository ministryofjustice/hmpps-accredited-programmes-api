package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.update

data class PrerequisiteUpdate(
  val name: String,
  val description: String? = null,
  val identifier: String,
)
