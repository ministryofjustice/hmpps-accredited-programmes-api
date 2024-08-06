package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.KeyDate
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.api.model.Sentence

/**
 *
 * @param sentences
 * @param keyDates
 */
data class SentenceDetails(

  @Schema(example = "null", description = "")
  @get:JsonProperty("sentences") val sentences: kotlin.collections.List<Sentence>? = null,

  @Schema(example = "null", description = "")
  @get:JsonProperty("keyDates") val keyDates: kotlin.collections.List<KeyDate>? = null,
)
