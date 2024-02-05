package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory

import io.github.bluegroundltd.kfactory.Factory
import io.github.bluegroundltd.kfactory.Yielded
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.randomAlphanumericString
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.OrganisationEntity
import java.util.UUID

class OrganisationEntityFactory : Factory<OrganisationEntity> {
  private var id: Yielded<UUID> = { UUID.randomUUID() }
  private var code: Yielded<String> = { randomAlphanumericString() }
  private var name: Yielded<String> = { randomAlphanumericString() }

  override fun produce() = OrganisationEntity(
    id = id(),
    code = this.code(),
    name = this.name(),
  )

  fun withId(id: UUID) = apply {
    this.id = { id }
  }

  fun withCode(code: String) = apply {
    this.code = { code }
  }

  fun withName(name: String) = apply {
    this.name = { name }
  }
}
