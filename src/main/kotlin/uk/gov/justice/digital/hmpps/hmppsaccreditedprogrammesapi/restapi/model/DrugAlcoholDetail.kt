package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 *
 * @param drug
 * @param alcohol
 */
data class DrugAlcoholDetail(

  @Schema(example = "null", description = "")
  @get:JsonProperty("drug") val drug: OasysDrugDetail? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("alcohol") val alcohol: OasysAlcoholDetail? = null,
)
