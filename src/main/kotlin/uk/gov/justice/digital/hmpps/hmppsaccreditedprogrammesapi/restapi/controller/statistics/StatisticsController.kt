package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.controller.statistics

import com.fasterxml.jackson.databind.ObjectMapper
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
    @RequestParam endDate: LocalDate? = LocalDate.now(),
    @RequestParam locationCode: String?,
  ): ReportContent {
    val parameters = Parameters(startDate, endDate, locationCode)

    val content = when (reportType) {
      ReportType.REFERRAL_COUNT_BY_COURSE -> statisticsRepository.referralCountByCourse(
        startDate,
        endDate!!,
        locationCode,
      )

      ReportType.REFERRAL_COUNT -> statisticsRepository.referralCount(
        startDate,
        endDate!!,
        locationCode,
      )
    }
    return ReportContent(
      reportType = reportType.name,
      content = objectMapper.readTree(content),
      parameters = parameters,
    )
  }
}

data class ReportContent(val reportType: String, val parameters: Parameters, val content: Any)

data class Parameters(
  val startDate: LocalDate,
  val endDate: LocalDate?,
  val locationCode: String?,
)

enum class ReportType {
  REFERRAL_COUNT_BY_COURSE,
  REFERRAL_COUNT,
}

data class ReportTypes(val types: List<String>)
