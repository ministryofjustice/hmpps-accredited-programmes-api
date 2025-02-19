package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.StatisticsRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ReportStatusCount
import java.time.LocalDate
import java.util.UUID

@Service
class StatisticsService(
  val statisticsRepository: StatisticsRepository,
) {
  fun getReferralStatusCountByProgramme(
    startDate: LocalDate,
    endDate: LocalDate,
    locations: List<String>?,
    courseId: UUID,
  ): List<ReportStatusCount> = statisticsRepository.findReferralCountByCourseId(startDate, endDate, locations, courseId)
    ?.map(ReportStatusCount::from) ?: emptyList()
}
