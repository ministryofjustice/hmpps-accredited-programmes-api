package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.prisonRegisterApi.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
class Prison(
  val prisonId: String,
  val prisonName: String,
  val active: Boolean = false,
  val male: Boolean = false,
  val female: Boolean = false,
  val contracted: Boolean = false,
  val types: List<PrisonType> = emptyList(),
  val categories: Set<String> = emptySet(),
  val addresses: List<Address> = emptyList(),
  val operators: List<PrisonOperator> = emptyList(),
)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PrisonType(val code: String, val description: String)

@JsonIgnoreProperties(ignoreUnknown = true)
data class Address(val addressLine1: String?, val addressLine2: String?, val town: String?, val county: String?, val postcode: String, val country: String)

@JsonIgnoreProperties(ignoreUnknown = true)
data class PrisonOperator(val name: String)
