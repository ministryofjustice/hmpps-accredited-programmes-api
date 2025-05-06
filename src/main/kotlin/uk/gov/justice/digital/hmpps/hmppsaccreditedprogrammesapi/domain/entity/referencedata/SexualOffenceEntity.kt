package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.type.SexualOffenceCategoryType
import java.util.*

@Entity
@Table(name = "sexual_offence")
class SexualOffenceEntity(

  @Id
  @Column(name = "id")
  var id: UUID? = null,

  @Enumerated(EnumType.STRING)
  @Column(name = "category")
  val category: SexualOffenceCategoryType,

  @Column(name = "description")
  val description: String,

  @Column(name = "hint_text")
  val hintText: String,

  @Column(name = "score")
  val score: Int,
)
