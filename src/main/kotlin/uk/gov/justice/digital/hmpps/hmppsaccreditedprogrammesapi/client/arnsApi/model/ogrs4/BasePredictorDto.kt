package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.arnsApi.model.ogrs4

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.arnsApi.model.type.ScoreLevel
import java.math.BigDecimal

open class BasePredictorDto(
  val score: BigDecimal? = null,
  val band: ScoreLevel? = null,
)
