package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 *
 * @param sentences
 * @param keyDates
 */
data class SentenceDetails(

  @Schema(example = "null", description = "")
  @get:JsonProperty("sentences") val sentences: List<Sentence>? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("keyDates") val keyDates: List<KeyDate>? = null,
)
