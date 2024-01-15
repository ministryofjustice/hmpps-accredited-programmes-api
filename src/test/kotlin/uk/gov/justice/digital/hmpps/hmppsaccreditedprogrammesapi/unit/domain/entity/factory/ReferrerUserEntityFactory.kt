package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory

import io.github.bluegroundltd.kfactory.Factory
import io.github.bluegroundltd.kfactory.Yielded
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.randomUppercaseString
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferrerUserEntity

class ReferrerUserEntityFactory : Factory<ReferrerUserEntity> {
  private var username: Yielded<String> = { randomUppercaseString() }

  fun withUsername(username: String) = apply {
    this.username = { username }
  }

  override fun produce() = ReferrerUserEntity(
    username = username(),
  )
}
