package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.repositories

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseParticipation
import java.util.UUID

@Repository
interface JpaCourseParticipationRepository : JpaRepository<CourseParticipation, UUID> {
  fun findByPrisonNumber(prisonNumber: String): List<CourseParticipation>
}