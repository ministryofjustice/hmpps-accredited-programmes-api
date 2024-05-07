package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonRegisterApi.model

class Prison(
  val prisonId: String,
  val prisonName: String,
  val active: Boolean,
  val male: Boolean = false,
  val female: Boolean = false,
  val contracted: Boolean = false,
  val types: List<PrisonType>,
  val categories: Set<String>,
  val addresses: List<Address>,
  val operators: List<PrisonOperator>,
)
data class PrisonType(val code: String, val description: String)

data class Address(val id: Long?, val addressLine1: String, val addressLine2: String?, val town: String, val county: String?, val postcode: String, val country: String)

data class PrisonOperator(val name: String)
