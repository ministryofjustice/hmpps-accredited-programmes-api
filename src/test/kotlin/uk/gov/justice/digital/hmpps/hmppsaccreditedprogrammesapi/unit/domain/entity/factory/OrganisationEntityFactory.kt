package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.randomAlphanumericString
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.OrganisationEntity
import java.util.UUID

class OrganisationEntityFactory {
  private var id: UUID = UUID.randomUUID()
  private var code: String = randomAlphanumericString()
  private var name: String = randomAlphanumericString()
  private var gender: String = listOf("M", "F").asSequence().shuffled().first()

  fun withId(id: UUID) = apply { this.id = id }
  fun withCode(code: String) = apply { this.code = code }
  fun withName(name: String) = apply { this.name = name }
  fun withGender(gender: String) = apply { this.gender = gender }

  fun produce() = OrganisationEntity(
    id = this.id,
    code = this.code,
    name = this.name,
    gender = this.gender,
  )
}
