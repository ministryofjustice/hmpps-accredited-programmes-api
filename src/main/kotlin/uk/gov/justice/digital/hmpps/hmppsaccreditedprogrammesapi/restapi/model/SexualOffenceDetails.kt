package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.util.*

data class SexualOffenceDetails(
  @Schema(example = "67ea1478-e4c3-46be-8b7f-86833fb87540", description = "The unique sexual offence identifier")
  @get:JsonProperty("id") val id: UUID,
  @Schema(example = "INCLUDES_VIOLENCE_FORCE_HUMILIATION", description = "The code of the offence category")
  @get:JsonProperty("categoryCode") val categoryCode: String,
  @Schema(example = "Other types of sexual offending", description = "The description of the offence category")
  @get:JsonProperty("categoryDescription") val categoryDescription: String,
  @Schema(example = "67ea1478-e4c3-46be-8b7f-86833fb87540", description = "The description of the offence")
  @get:JsonProperty("description") val description: String,
  @Schema(example = "Any use of additional violence or force not necessary to gain victim compliance", description = "Further elaborations of the offence")
  @get:JsonProperty("hintText") val hintText: String? = null,
  @Schema(example = "1", description = "The score associated with the offence")
  @get:JsonProperty("score") val score: Int,
)
