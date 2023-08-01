package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain

data class CourseUpdate(
  val name: String,
  val description: String,
  val identifier: String,
  val audience: String,
  val alternateName: String? = null,
) {
  val audienceStrings: Set<String> by lazy {
    audience
      .split(',')
      .map(String::trim)
      .filterNot(String::isEmpty)
      .toSet()
  }
}
