package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.type

enum class SaraRisk(private val score: Int) {
  NOT_APPLICABLE(0),
  LOW(10),
  MEDIUM(20),
  HIGH(30),
  VERY_HIGH(40),
  ;

  companion object {
    fun fromString(value: String?): SaraRisk {
      return SaraRisk.entries.find { it.name.equals(value, ignoreCase = true) }
        ?: NOT_APPLICABLE
    }

    fun highestRisk(risk1: SaraRisk, risk2: SaraRisk): SaraRisk {
      return if (risk1.score > risk2.score) risk1 else risk2
    }
  }
}
