package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.type

enum class KeyDateType(val mapping: String, val code: String, val description: String, val order: Int = 1) {
  ACTUAL_PAROLE_DATE("actualParoleDate", "APD", "Approved parole date", 10),
  AUTOMATIC_RELEASE_DATE("automaticReleaseDate", "ARD", "Automatic release date", 20),
  CONDITIONAL_RELEASE_DATE("conditionalReleaseDate", "CRD", "Conditional release date", 30),
  EARLY_REMOVAL_SCHEME_ELIGIBILITY_DATE(
    "earlyRemovalSchemeEligibilityDate",
    "ERSED",
    "Early removal scheme eligibility date",
    40,
  ),
  HOME_DETENTION_CURFEW_ACTUAL_DATE(
    "homeDetentionCurfewActualDate",
    "HDCAD",
    "Home detention curfew approved date",
    50,
  ),
  HOME_DETENTION_CURFEW_ELIGIBILITY_DATE(
    "homeDetentionCurfewEligibilityDate",
    "HDCED",
    "Home detention curfew eligibility date",
    60,
  ),
  LICENCE_END_DATE("licenceExpiryDate", "LED", "Licence end date", 65),
  MID_TERM_DATE("midTermDate", "MTD", "Mid term date", 70),
  NON_PAROLE_DATE("nonParoleDate", "NPD", "Non-parole date", 80),
  TARIFF_EARLY_REMOVAL_SCHEME_ELIGIBILITY_DATE(
    "tariffEarlyRemovalSchemeEligibilityDate",
    "TERSED",
    "Tariff expired release scheme eligibility date",
    90,
  ),
  TARIFF_DATE("tariffDate", "TED", "Tariff end date", 100),
  PAROLE_ELIGIBILITY_DATE("paroleEligibilityDate", "PED", "Parole eligibility date", 110),
  POST_RECALL_RELEASE_DATE("postRecallReleaseDate", "PRRD", "Post recall release date", 120),
  RELEASE_DATE("releaseDate", "RD", "Confirmed release date", 130),
  SENTENCE_END_DATE("sentenceExpiryDate", "SED", "Sentence end date", 140),
  ;

  companion object {
    private val mappingToEnum: Map<String, KeyDateType> = entries.associateBy { it.mapping }

    fun fromMapping(mapping: String): KeyDateType? = mappingToEnum[mapping]

    private val relevantDatesForEarliestReleaseDateCalculation =
      listOf<KeyDateType>(
        TARIFF_DATE,
        CONDITIONAL_RELEASE_DATE,
        RELEASE_DATE,
        PAROLE_ELIGIBILITY_DATE,
        POST_RECALL_RELEASE_DATE,
      )

    val relevantDatesForEarliestReleaseDateCalculationCodesForCaseList =
      relevantDatesForEarliestReleaseDateCalculation.map { it.code }.toSet()
  }
}
