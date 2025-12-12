package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.client.arnsApi.model.ogrs4

data class RsrPredictorDto(
  val seriousViolentReoffendingPredictor: StaticOrDynamicPredictorDto? = null,
  val directContactSexualReoffendingPredictor: BasePredictorDto? = null,
  val indirectImageContactSexualReoffendingPredictor: BasePredictorDto? = null,
  val combinedSeriousReoffendingPredictor: VersionedStaticOrDynamicPredictorDto? = null,
)
