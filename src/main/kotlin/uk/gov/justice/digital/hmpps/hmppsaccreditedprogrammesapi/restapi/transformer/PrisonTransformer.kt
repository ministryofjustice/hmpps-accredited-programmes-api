package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.transformer

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonRegisterApi.model.Prison
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Address
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.Category
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.PrisonOperator
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.PrisonSearchResponse
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.PrisonType

fun Prison.toPrisonSearchResponse() = PrisonSearchResponse(
  prisonId = this.prisonId,
  prisonName = this.prisonName,
  active = this.active,
  male = this.male,
  female = this.female,
  contracted = this.contracted,
  types = this.types.map {
    PrisonType(
      it.code,
      it.description,
    )
  },
  categories = this.categories.map { Category(it) },
  addresses = this.addresses.map {
    Address(
      addressLine1 = it.addressLine1.orEmpty(),
      addressLine2 = it.addressLine2,
      county = it.county,
      town = it.town.orEmpty(),
      postcode = it.postcode,
      country = it.country,
    )
  },
  operators = this.operators.map {
    PrisonOperator(
      name = it.name,
    )
  },
)
