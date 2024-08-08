package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import com.fasterxml.jackson.annotation.JsonProperty
import io.swagger.v3.oas.annotations.media.Schema

/**
 * warnings and errors for a line in an uploaded CSV file.
 * @param lineNumber The number of the line in the CSV file that was rejected. The header line is lineNumber 1, the first line of CSV data is lineNumber 2.
 * @param level One of 'Error' or 'Warning'.  If a line has an Error then the data was not added. If it is a Warning then the line was added but there was a problem that should be corrected.
 * @param message Useful information about the Error or Warning.
 */
data class LineMessage(

  @Schema(example = "20", description = "The number of the line in the CSV file that was rejected. The header line is lineNumber 1, the first line of CSV data is lineNumber 2.")
  @get:JsonProperty("lineNumber") val lineNumber: Int? = null,

  @Schema(example = "null", description = "One of 'Error' or 'Warning'.  If a line has an Error then the data was not added. If it is a Warning then the line was added but there was a problem that should be corrected.")
  @get:JsonProperty("level") val level: LineMessage.Level? = null,

  @Schema(example = "No match for course 'Kaizen', prisonId 'BWI'", description = "Useful information about the Error or Warning.")
  @get:JsonProperty("message") val message: String? = null,
) {

  /**
   * One of 'Error' or 'Warning'.  If a line has an Error then the data was not added. If it is a Warning then the line was added but there was a problem that should be corrected.
   * Values: Warning,Error
   */
  enum class Level(val value: String) {

    @JsonProperty("Warning")
    Warning("Warning"),

    @JsonProperty("Error")
    Error("Error"),
  }
}
