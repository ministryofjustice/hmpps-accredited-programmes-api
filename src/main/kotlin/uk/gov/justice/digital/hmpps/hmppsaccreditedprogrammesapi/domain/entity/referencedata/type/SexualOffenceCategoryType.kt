package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.referencedata.type

enum class SexualOffenceCategoryType(val description: String) {
  AGAINST_MINORS("Sexual offence against somebody aged under 18"),
  INCLUDES_VIOLENCE_FORCE_HUMILIATION("Sexual offences that include violence, force or humiliation"),
  OTHER("Other types of sexual offending"),
}
