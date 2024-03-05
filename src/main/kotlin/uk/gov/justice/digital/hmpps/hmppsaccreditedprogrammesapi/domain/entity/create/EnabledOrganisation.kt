package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create

import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "enabled_organisation")
data class EnabledOrganisation(
  @Id
  val code: String,
  val description: String,
)
