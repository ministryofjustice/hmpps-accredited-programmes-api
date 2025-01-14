package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 * Information about where the course was held.
 * @param type
 * @param location
 */
data class CourseParticipationSetting(

  @Schema(example = "null", required = true, description = "")
  @get:JsonProperty("type", required = true) val type: CourseParticipationSettingType,

  @Schema(example = "null", description = "")
  @get:JsonProperty("location") val location: String? = null,
) {

  companion object {
    fun from(type: String?, location: String? = null): CourseParticipationSetting? {
      if (type == null || location == null) {
        return null
      }
      return CourseParticipationSetting(type = CourseParticipationSettingType.valueOf(type), location = location)
    }
  }
}
