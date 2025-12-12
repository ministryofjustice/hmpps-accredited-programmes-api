package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.arnsApi.model

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.arnsApi.model.type.RsrScoreSource
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.arnsApi.model.type.ScoreLevel
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.arnsApi.model.type.ScoreType
import java.math.BigDecimal

data class RsrScoreDto(
  val percentageScore: BigDecimal? = null,
  val staticOrDynamic: ScoreType? = null,
  val source: RsrScoreSource,
  val algorithmVersion: String? = null,
  val scoreLevel: ScoreLevel? = null,
)
