package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "audience")
data class AudienceEntity(
  @Id
  @GeneratedValue
  @Column(name = "audience_id")
  val id: UUID? = null,

  var name: String,
  var colour: String?,
)
