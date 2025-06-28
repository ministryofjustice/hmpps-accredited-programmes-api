package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.util.UUID

@Entity
@Table(name = "address")
class AddressEntity(

  @Id
  @Column(name = "id")
  var id: UUID? = null,

  @Column(name = "address_line_1")
  var addressLine1: String,

  @Column(name = "address_line_2")
  var addressLine2: String,

  @Column(name = "country")
  var country: String,

  @Column(name = "county")
  var county: String,

  @Column(name = "postal_code")
  var postalCode: String,

  @Column(name = "town")
  var town: String,
)
