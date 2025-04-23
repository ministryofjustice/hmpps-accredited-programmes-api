package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller

import com.fasterxml.jackson.databind.ObjectMapper
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.tags.Tag
import org.slf4j.LoggerFactory
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.common.exception.BusinessException
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.StatisticsRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ReferralStatistics
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.StatusCountByProgramme
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.StatisticsService
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service.type.ReferralStatus
import java.time.LocalDate
import java.util.UUID

@RestController
@Tag(
  name = "Statistics",
  description = """
    A series of endpoints for generating accredited programme statistics.
    For more information see here:
    [Performance endpoints](https://dsdmoj.atlassian.net/wiki/spaces/IC/pages/5036638227/Performance+endpoints)
  """,
)
@RequestMapping("statistics")
class StatisticsController(

  private val statisticsRepository: StatisticsRepository,
  private val objectMapper: ObjectMapper,
  private val statisticsService: StatisticsService,
) {
  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }

  @GetMapping("/report/countByStatus", produces = ["application/json"])
  fun getReportTypes(
    @RequestParam startDate: LocalDate,
    @RequestParam endDate: LocalDate? = LocalDate.now().plusDays(1),
    @RequestParam locationCodes: List<String>? = listOf(),
  ) = statisticsRepository.referralCountByStatus(
    startDate,
    endDate!!,
    locationCodes,
  ).orEmpty()

  @GetMapping("/report/count-by-status-for-programme", produces = ["application/json"])
  fun getReportStatusCountsForProgramme(
    @RequestParam startDate: LocalDate,
    @RequestParam endDate: LocalDate? = LocalDate.now().plusDays(1),
    @RequestParam locationCodes: List<String>? = listOf(),
    @RequestParam courseName: String? = null,
  ): List<StatusCountByProgramme> = statisticsService.getReferralStatusCountByProgramme(
    startDate,
    endDate!!,
    locationCodes,
    courseName,
  )

  @Deprecated("This endpoint is deprecated and may be removed in the future")
  @GetMapping("/report/referral-statistics", produces = ["application/json"])
  fun getReferralStatistics(): ReferralStatistics {
    log.warn("Deprecated endpoint /report/referral-statistics was called")
    return statisticsService.getReferralStatistics()
  }

  @Deprecated("This endpoint is deprecated and may be removed in the future")
  @GetMapping("/report-types", produces = ["application/json"])
  fun getReportTypes(): ReportTypes {
    log.warn("Deprecated endpoint /report-types was called")
    return ReportTypes(ReportType.entries.map { it.name })
  }

  @GetMapping("/report/{reportType}")
  fun getStatistics(
    @PathVariable reportType: ReportType,
    @RequestParam startDate: LocalDate,
    @RequestParam endDate: LocalDate? = LocalDate.now().plusDays(1),
    @RequestParam locationCodes: List<String>? = listOf(),
    @RequestParam courseId: UUID?,
  ): ReportContent {
    val parameters = Parameters(startDate, endDate, locationCodes, courseId)
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
        ReferralStatus.PROGRAMME_COMPLETE.name,
        courseId,
      )

      ReportType.WITHDRAWN_COUNT -> statisticsRepository.finalStatusCodeCounts(
        startDate,
        endDate!!,
        locationCodes,
        ReferralStatus.WITHDRAWN.name,
        courseId,
      )

      ReportType.NOT_ELIGIBLE_COUNT -> statisticsRepository.finalStatusCodeCounts(
        startDate,
        endDate!!,
        locationCodes,
        ReferralStatus.NOT_ELIGIBLE.name,
        courseId,
      )

      ReportType.NOT_SUITABLE_COUNT -> statisticsRepository.finalStatusCodeCounts(
        startDate,
        endDate!!,
        locationCodes,
        ReferralStatus.NOT_SUITABLE.name,
        courseId,
      )

      ReportType.DESELECTED_COUNT -> statisticsRepository.finalStatusCodeCounts(
        startDate,
        endDate!!,
        locationCodes,
        ReferralStatus.DESELECTED.name,
        courseId,
      )

      ReportType.ON_PROGRAMME_COUNT -> statisticsRepository.finalStatusCodeCounts(
        startDate,
        endDate!!,
        locationCodes,
        ReferralStatus.ON_PROGRAMME.name,
        courseId,
      )

      ReportType.PNI_PATHWAY_COUNT -> statisticsRepository.pniPathwayCounts(
        startDate,
        endDate!!,
        locationCodes,
      )
    }
    return ReportContent(
      reportType = reportType.name,
      content = objectMapper.readValue(content, Content::class.java),
      parameters = parameters,
    )
  }

  /**
   * Returns the counts of referrals at various statuses for location codes.
   * Note that this endpoint will just return the number of referrals that have the
   * supplied status.
   *
   */
  @Deprecated("This endpoint is deprecated and may be removed in the future")
  @GetMapping("/current/status-counts")
  fun getCurrentStatusCounts(
    @RequestParam statuses: List<String> = listOf(),
    @RequestParam locationCodes: List<String>? = listOf(),
  ): CurrentCount {
    log.warn("Deprecated endpoint /current/status-counts was called")
    val content = statisticsRepository.currentCountsByStatus(statuses, locationCodes)

    return objectMapper.readValue(content, CurrentCount::class.java)
  }

  @GetMapping("/performance/status-duration")
  fun getAverageTimeSpentAtStatus(
    @RequestParam startDate: LocalDate,
    @RequestParam endDate: LocalDate? = LocalDate.now().plusDays(1),
    @RequestParam statuses: List<String> = listOf(),
    @RequestParam locationCodes: List<String>? = listOf(),
  ): Performance {
    if (statuses.isEmpty()) {
      throw BusinessException("This end point requires at least one status")
    }
    val content = statisticsRepository.averageTime(startDate, endDate!!, statuses, locationCodes)
    return objectMapper.readValue(content, Performance::class.java)
  }
}

data class ReportContent(val reportType: String, val parameters: Parameters, val content: Content)

data class Parameters(
  val startDate: LocalDate,
  val endDate: LocalDate?,
  val locationCodes: List<String>?,
  val courseId: UUID?,
)

/**
 * For each report Type the input params are start/end date (the date the referral was
 * submitted) and a list of 0 to many location codes.
 * If the end date is left blank it will default to the current date. And if location codes is
 * empty then it defaults to all locations (ie national)
 */
enum class ReportType {
  REFERRAL_COUNT_BY_COURSE, // Number of referrals broken down by course/audience
  REFERRAL_COUNT, // Number of referrals

  // Number of referrals at Various closed statuses broken down by course/audience
  PROGRAMME_COMPLETE_COUNT,
  WITHDRAWN_COUNT,
  NOT_ELIGIBLE_COUNT,
  NOT_SUITABLE_COUNT,
  DESELECTED_COUNT,
  PNI_PATHWAY_COUNT,
  ON_PROGRAMME_COUNT,
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

data class CurrentCount(val totalCount: Int?, val statusContent: List<StatusContent>?)

data class Performance(val performance: List<PerformanceStatistic>?)

data class PerformanceStatistic(
  val status: String,
  val averageDuration: String?,
  val minDuration: String?,
  val maxDuration: String?,
)

data class StatusContent(val status: String, val countAtStatus: Int, val courseCounts: List<CourseCount>?)
