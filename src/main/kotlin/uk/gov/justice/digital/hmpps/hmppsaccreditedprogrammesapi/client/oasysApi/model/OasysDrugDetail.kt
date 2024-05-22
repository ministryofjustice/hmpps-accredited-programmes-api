package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.oasysApi.model

import com.fasterxml.jackson.annotation.JsonAlias
import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class OasysDrugDetail(
  @JsonAlias("levelOfUseOfMainDrug")
  val LevelOfUseOfMainDrug: String?,
  @JsonAlias("drugsMajorActivity")
  val DrugsMajorActivity: String?,
)
