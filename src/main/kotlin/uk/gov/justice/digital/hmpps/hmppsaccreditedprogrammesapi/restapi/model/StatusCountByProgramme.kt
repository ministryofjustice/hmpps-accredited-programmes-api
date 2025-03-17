package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.restapi.model

import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.StatisticsRepository.StatusCountByProgrammeProjection
import java.math.BigInteger

data class StatusCountByProgramme(
  val courseName: String,
  val audience: String,
  val count: BigInteger,
  val status: String,
  val organisationCode: String,
) {
  companion object {
    fun from(projection: StatusCountByProgrammeProjection): StatusCountByProgramme = StatusCountByProgramme(
      courseName = projection.getCourseName(),
      audience = projection.getAudience(),
      count = projection.getCount(),
      status = projection.getStatus(),
      organisationCode = projection.getOrgId(),
    )
  }
}
