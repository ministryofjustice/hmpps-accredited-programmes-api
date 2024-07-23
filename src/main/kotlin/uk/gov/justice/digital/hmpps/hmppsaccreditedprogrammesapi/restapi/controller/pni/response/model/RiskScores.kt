package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.pni.response.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema
import java.math.BigDecimal

/**
 * 
 * @param ogrs3 
 * @param ovp 
 * @param ospDc 
 * @param ospIic 
 * @param rsr 
 * @param sara 
 */
data class RiskScores(

    @Schema(example = "null", description = "")
    @get:JsonProperty("ogrs3") val ogrs3: BigDecimal? = null,

    @Schema(example = "null", description = "")
    @get:JsonProperty("ovp") val ovp: BigDecimal? = null,

    @Schema(example = "null", description = "")
    @get:JsonProperty("ospDc") val ospDc: BigDecimal? = null,

    @Schema(example = "null", description = "")
    @get:JsonProperty("ospIic") val ospIic: BigDecimal? = null,

    @Schema(example = "null", description = "")
    @get:JsonProperty("rsr") val rsr: BigDecimal? = null,

    @Schema(example = "null", description = "")
    @get:JsonProperty("sara") val sara: BigDecimal? = null
) {

}

