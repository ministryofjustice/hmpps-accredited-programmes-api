package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.StatisticsRepository.ReportStatusCountProjection

data class ReportStatusCount(
  val count: Long,
  val status: String,
  val organisationCode: String,
) {
  companion object {
    fun from(projection: ReportStatusCountProjection) = ReportStatusCount(
      count = projection.getCount().toLong(),
      status = projection.getStatus(),
      organisationCode = projection.getOrgId(),
    )
  }
}
