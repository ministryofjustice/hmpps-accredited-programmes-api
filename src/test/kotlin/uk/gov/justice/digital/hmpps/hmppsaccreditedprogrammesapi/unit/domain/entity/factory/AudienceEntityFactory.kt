package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory

import io.github.bluegroundltd.kfactory.Factory
import io.github.bluegroundltd.kfactory.Yielded
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.randomLowercaseString
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.AudienceEntity
import java.util.*

class AudienceEntityFactory : Factory<AudienceEntity> {
  private var id: Yielded<UUID?> = { UUID.randomUUID() }
  private var value: Yielded<String> = { randomLowercaseString() }

  fun withValue(prisonNumber: String) = apply {
    this.value = { prisonNumber }
  }

  override fun produce() = AudienceEntity(
    id = this.id(),
    value = this.value(),
  )
}
