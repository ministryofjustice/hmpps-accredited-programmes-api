package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty

/**
* Either Custody or Community.
* Values: custody,community
*/
enum class CourseParticipationSettingType(val value: kotlin.String) {

  @JsonProperty("custody")
  CUSTODY("custody"),

  @JsonProperty("community")
  COMMUNITY("community"),
}
