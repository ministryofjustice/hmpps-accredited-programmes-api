package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.unit.domain.entity.factory

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.randomLowercaseString
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.util.randomUppercaseString
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.EnabledOrganisation

class EnabledOrganisationEntityFactory {
  private var code: String = randomLowercaseString()
  private var description: String = randomUppercaseString()

  fun withCode(code: String) = apply { this.code = code }
  fun withDescription(description: String) = apply { this.description = description }

  fun produce() = EnabledOrganisation(
    code = this.code,
    description = this.description,
  )
}
