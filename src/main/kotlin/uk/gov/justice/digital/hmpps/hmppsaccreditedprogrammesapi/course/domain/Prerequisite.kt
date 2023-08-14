package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.course.domain

import jakarta.persistence.Embeddable

@Embeddable
@org.hibernate.annotations.Immutable
data class Prerequisite(
  val name: String,
  val description: String,
)
