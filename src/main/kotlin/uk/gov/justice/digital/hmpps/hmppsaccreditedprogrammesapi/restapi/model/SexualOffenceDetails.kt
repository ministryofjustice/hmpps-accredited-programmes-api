package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.type.SexualOffenceCategoryType
import java.util.*

data class SexualOffenceDetails(
  @Schema(example = "67ea1478-e4c3-46be-8b7f-86833fb87540", description = "The unique sexual offence identifier")
  @get:JsonProperty("id") val id: UUID,

  @Schema(example = "Evidence of ritualism in the offence", description = "A human-friendly description of the Offence itself")
  @get:JsonProperty("description") val description: String,

  @Schema(example = "Fixed actions or words, performed in a specific way", description = "Further elaborations of the offence")
  @get:JsonProperty("hintText") val hintText: String? = null,

  @Schema(example = "INCLUDES_VIOLENCE_FORCE_HUMILIATION", description = "A computer-friendly identifier for the Offence's category")
  @get:JsonProperty("categoryCode") val categoryCode: SexualOffenceCategoryType,

  @Schema(example = "Sexual offences that include violence, force or humiliation", description = "A human-friendly description of the Offence's category (i.e. the categoryCode)")
  @get:JsonProperty("categoryDescription") val categoryDescription: String,

  @Schema(example = "1", description = "The score associated with the offence, between 1 and 3.")
  @get:JsonProperty("score") val score: Int,
)
