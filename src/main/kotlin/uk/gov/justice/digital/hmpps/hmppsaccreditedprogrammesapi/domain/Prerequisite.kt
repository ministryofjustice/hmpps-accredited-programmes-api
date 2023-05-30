package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain

import jakarta.persistence.Embeddable

@Embeddable
@org.hibernate.annotations.Immutable
data class Prerequisite(
  val name: String,
  val description: String,
)
