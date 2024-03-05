package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory

import io.github.bluegroundltd.kfactory.Yielded
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.randomLowercaseString
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.randomUppercaseString
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.EnabledOrganisation

class EnabledOrganisationEntityFactory {

  private var code: Yielded<String> = { randomLowercaseString() }
  private var description: Yielded<String> = { randomUppercaseString() }

  fun code(code: String) = apply { this.code = { code } }
  fun description(description: String) = apply { this.description = { description } }

  fun produce() = EnabledOrganisation(
    code = this.code(),
    description = this.description(),
  )
}
