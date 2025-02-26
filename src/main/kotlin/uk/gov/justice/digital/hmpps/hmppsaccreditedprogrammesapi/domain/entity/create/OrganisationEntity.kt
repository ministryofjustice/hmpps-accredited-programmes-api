package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "organisation")
data class OrganisationEntity(

  //  var id: UUID = UUID.randomUUID(),
  @Id
  @GeneratedValue
  @Column(name = "organisation_id")
  var id: UUID? = null,

  @Column
  var code: String,

  @Column
  var name: String,

  @Column
  var gender: String,

)
