package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.SexualOffenceDetailsEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.type.SexualOffenceCategoryType
import java.util.UUID

class SexualOffenceDetailsEntityFactory {
  private var id: UUID = UUID.randomUUID()
  private var category: SexualOffenceCategoryType = SexualOffenceCategoryType.AGAINST_MINORS
  private var description: String = "An offence"
  private var hintText: String? = null
  private var score: Int = 1

  fun withId(id: UUID) = apply { this.id = id }
  fun withCategory(category: SexualOffenceCategoryType) = apply { this.category = category }
  fun withDescription(description: String) = apply { this.description = description }
  fun withHintText(hintText: String?) = apply { this.hintText = hintText }
  fun withScore(score: Int) = apply { this.score = score }

  fun produce() = SexualOffenceDetailsEntity(
    id = this.id,
    category = this.category,
    description = this.description,
    hintText = this.hintText,
    score = this.score,
  )
}
