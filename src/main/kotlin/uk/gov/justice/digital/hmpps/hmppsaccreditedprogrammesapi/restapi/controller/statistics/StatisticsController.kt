package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.statistics

import com.fasterxml.jackson.databind.ObjectMapper
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.StatisticsRepository
import java.time.LocalDate

@RestController
@Tag(name = "Statistics")
@RequestMapping("statistics")
class StatisticsController(
  private val statisticsRepository: StatisticsRepository,
  private val objectMapper: ObjectMapper,
) {

  @GetMapping("/report-types", produces = ["application/json"])
  fun getReportTypes(): ReportTypes {
    return ReportTypes(ReportType.entries.map { it.name })
  }

  @GetMapping("/report/{reportType}")
  fun getStatistics(
    @PathVariable reportType: ReportType,
    @RequestParam startDate: LocalDate,
    @RequestParam endDate: LocalDate? = LocalDate.now().plusDays(1),
    @RequestParam locationCodes: List<String>? = listOf(),
  ): ReportContent {
    val parameters = Parameters(startDate, endDate, locationCodes)
    val content = when (reportType) {
      ReportType.REFERRAL_COUNT_BY_COURSE -> statisticsRepository.referralCountByCourse(
        startDate,
        endDate!!,
        locationCodes,
      )

      ReportType.REFERRAL_COUNT -> statisticsRepository.referralCount(
        startDate,
        endDate!!,
        locationCodes,
      )

      ReportType.PROGRAMME_COMPLETE_COUNT -> statisticsRepository.finalStatusCodeCounts(
        startDate,
        endDate!!,
        locationCodes,
        "PROGRAMME_COMPLETE",
      )

      ReportType.WITHDRAWN_COUNT -> statisticsRepository.finalStatusCodeCounts(
        startDate,
        endDate!!,
        locationCodes,
        "WITHDRAWN",
      )

      ReportType.NOT_ELIGIBLE_COUNT -> statisticsRepository.finalStatusCodeCounts(
        startDate,
        endDate!!,
        locationCodes,
        "NOT_ELIGIBLE",
      )

      ReportType.NOT_SUITABLE_COUNT -> statisticsRepository.finalStatusCodeCounts(
        startDate,
        endDate!!,
        locationCodes,
        "NOT_SUITABLE",
      )

      ReportType.DESELECTED_COUNT -> statisticsRepository.finalStatusCodeCounts(
        startDate,
        endDate!!,
        locationCodes,
        "DESELECTED",
      )
    }

    return ReportContent(
      reportType = reportType.name,
      content = objectMapper.readValue(content, Content::class.java),
      parameters = parameters,
    )
  }
}

data class ReportContent(val reportType: String, val parameters: Parameters, val content: Content)

data class Parameters(
  val startDate: LocalDate,
  val endDate: LocalDate?,
  val locationCodes: List<String>?,
)

enum class ReportType {
  REFERRAL_COUNT_BY_COURSE,
  REFERRAL_COUNT,
  PROGRAMME_COMPLETE_COUNT,
  WITHDRAWN_COUNT,
  NOT_ELIGIBLE_COUNT,
  NOT_SUITABLE_COUNT,
  DESELECTED_COUNT,
}

data class ReportTypes(val types: List<String>)

@Schema(
  description = "The result of the statistics query",
)
data class Content(
  @Schema(description = "count will be present for simple queries where there is only a count returned.")
  val count: Int?,
  @Schema(description = "A list of counts will be returned when the query has more than one count returned")
  val courseCounts: List<CourseCount>?,
)

data class CourseCount(
  val name: String,
  val audience: String,
  val count: Int?,
)
