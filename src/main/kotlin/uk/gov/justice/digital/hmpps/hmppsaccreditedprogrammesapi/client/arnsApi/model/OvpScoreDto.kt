package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.arnsApi.model

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.arnsApi.model.type.ScoreLevel
import java.math.BigDecimal

data class OvpScoreDto(
  val ovpStaticWeightedScore: BigDecimal? = null,
  val ovpDynamicWeightedScore: BigDecimal? = null,
  val ovpTotalWeightedScore: BigDecimal? = null,
  val oneYear: BigDecimal? = null,
  val twoYears: BigDecimal? = null,
  val ovpRisk: ScoreLevel? = null,
)
