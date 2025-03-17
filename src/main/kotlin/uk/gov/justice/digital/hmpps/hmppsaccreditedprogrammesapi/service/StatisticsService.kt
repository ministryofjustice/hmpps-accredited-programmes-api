package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.StatisticsRepository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.ReferralStatistics
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model.StatusCountByProgramme
import java.time.LocalDate

@Service
class StatisticsService(
  val statisticsRepository: StatisticsRepository,
) {
  fun getReferralStatusCountByProgramme(
    startDate: LocalDate,
    endDate: LocalDate,
    locations: List<String>?,
    courseName: String?,
  ): List<StatusCountByProgramme> = statisticsRepository.findReferralCountByCourseName(startDate, endDate, locations, courseName)
    ?.map(StatusCountByProgramme::from) ?: emptyList()

  fun getReferralStatistics(): ReferralStatistics = ReferralStatistics.from(statisticsRepository.getReferralStatistics())
}
