package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.type

enum class SentenceCategoryType(val description: String) {

  DETERMINATE("Determinate"),
  DETERMINATE_RECALL("Determinate and Recall"),
  INDETERMINATE("Indeterminate"),
  INDETERMINATE_RECALL("Indeterminate and Recall"),
  DETERMINATE_INDETERMINATE("Determinate and Indeterminate"),
  DETERMINATE_INDETERMINATE_RECALL("Determinate and Indeterminate and Recall"),
  UNKNOWN("Unknown"),
  RECALL("Recall"),
  NO_ACTIVE_SENTENCES("No active sentences"),
  ;

  companion object {
    fun determineOverallCategory(sentenceCategoryList: List<SentenceCategoryType>): SentenceCategoryType = when {
      sentenceCategoryList.containsAll(listOf(DETERMINATE, INDETERMINATE)) -> DETERMINATE_INDETERMINATE
      sentenceCategoryList.containsAll(listOf(DETERMINATE_RECALL, INDETERMINATE)) -> DETERMINATE_INDETERMINATE_RECALL
      sentenceCategoryList.containsAll(listOf(DETERMINATE, INDETERMINATE_RECALL)) -> DETERMINATE_INDETERMINATE_RECALL
      sentenceCategoryList.contains(DETERMINATE_RECALL) -> DETERMINATE_RECALL
      sentenceCategoryList.contains(INDETERMINATE_RECALL) -> INDETERMINATE_RECALL
      sentenceCategoryList.contains(DETERMINATE) -> DETERMINATE
      sentenceCategoryList.contains(INDETERMINATE) -> INDETERMINATE
      sentenceCategoryList.contains(RECALL) -> RECALL
      else -> UNKNOWN
    }
  }
}
