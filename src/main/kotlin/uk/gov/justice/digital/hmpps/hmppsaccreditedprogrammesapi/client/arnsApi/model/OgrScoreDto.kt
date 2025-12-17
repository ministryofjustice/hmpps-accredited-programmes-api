package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.arnsApi.model

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.arnsApi.model.type.ScoreLevel
import java.math.BigDecimal

data class OgrScoreDto(
  val oneYear: BigDecimal? = null,
  val twoYears: BigDecimal? = null,
  val scoreLevel: ScoreLevel? = null,
)
