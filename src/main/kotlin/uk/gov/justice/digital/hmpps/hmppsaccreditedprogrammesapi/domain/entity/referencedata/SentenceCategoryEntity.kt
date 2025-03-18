package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import jakarta.persistence.Table
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.type.SentenceCategoryType

@Entity
@Table(name = "sentence_category")
class SentenceCategoryEntity(
  @Id
  val description: String,
  @Enumerated(EnumType.STRING)
  val category: SentenceCategoryType,
)
