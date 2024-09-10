package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.randomUppercaseString
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferrerUserEntity

class ReferrerUserEntityFactory {
  private var username = randomUppercaseString()

  fun withUsername(username: String) = apply { this.username = username }

  fun produce() = ReferrerUserEntity(
    username = username,
  )
}
