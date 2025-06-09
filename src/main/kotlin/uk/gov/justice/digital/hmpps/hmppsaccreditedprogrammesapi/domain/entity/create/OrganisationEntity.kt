package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.OneToOne
import jakarta.persistence.Table
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.type.Gender
import java.util.UUID

@Entity
@Table(name = "organisation")
class OrganisationEntity(

  @Id
  @Column(name = "organisation_id")
  var id: UUID? = null,

  @Column(name = "code")
  var code: String,

  @Column(name = "name")
  var name: String,

  @Enumerated(EnumType.STRING)
  @Column(name = "gender")
  var gender: Gender,

  @Column(name = "is_national")
  var isNational: Boolean? = null,

  @OneToOne(cascade = [CascadeType.ALL])
  @JoinColumn(name = "address")
  var address: AddressEntity? = null,
)
