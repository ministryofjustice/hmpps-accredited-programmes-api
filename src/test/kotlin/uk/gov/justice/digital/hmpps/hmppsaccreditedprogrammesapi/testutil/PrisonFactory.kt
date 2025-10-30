package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.testutil

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonRegisterApi.model.Address
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonRegisterApi.model.Prison
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonRegisterApi.model.PrisonOperator
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonRegisterApi.model.PrisonType

class PrisonFactory {
  private var prisonId: String = "MDI"
  private var prisonName: String = "Moorland (HMP & YOI)"
  private var active: Boolean = true
  private var male: Boolean = true
  private var female: Boolean = false
  private var contracted: Boolean = false
  private var types: List<PrisonType> = listOf(PrisonType("HMP", "Her Majesty's Prison"))
  private var categories: Set<String> = setOf("C")
  private var addresses: List<Address> = listOf(
    Address(
      addressLine1 = "Bawtry Road",
      addressLine2 = null,
      town = "Hatfield Woodhouse",
      county = "South Yorkshire",
      postcode = "DN7 6BW",
      country = "England",
    ),
  )
  private var operators: List<PrisonOperator> = listOf(PrisonOperator("Her Majesty's Prison Service"))

  fun withPrisonId(prisonId: String) = apply { this.prisonId = prisonId }
  fun withPrisonName(prisonName: String) = apply { this.prisonName = prisonName }
  fun withActive(active: Boolean) = apply { this.active = active }
  fun withMale(male: Boolean) = apply { this.male = male }
  fun withFemale(female: Boolean) = apply { this.female = female }
  fun withContracted(contracted: Boolean) = apply { this.contracted = contracted }
  fun withTypes(types: List<PrisonType>) = apply { this.types = types }
  fun withCategories(categories: Set<String>) = apply { this.categories = categories }
  fun withAddresses(addresses: List<Address>) = apply { this.addresses = addresses }
  fun withOperators(operators: List<PrisonOperator>) = apply { this.operators = operators }

  fun produce() = Prison(
    prisonId = prisonId,
    prisonName = prisonName,
    active = active,
    male = male,
    female = female,
    contracted = contracted,
    types = types,
    categories = categories,
    addresses = addresses,
    operators = operators,
  )
}
