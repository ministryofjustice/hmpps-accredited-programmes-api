package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "referrer_user")
data class ReferrerUserEntity(
  @Id
  @Column(name = "referrer_username", nullable = false)
  var username: String,
)
