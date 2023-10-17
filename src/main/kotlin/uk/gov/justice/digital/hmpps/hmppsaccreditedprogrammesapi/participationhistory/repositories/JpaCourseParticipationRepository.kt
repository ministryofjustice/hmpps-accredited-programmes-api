package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.repositories

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.participationhistory.domain.CourseParticipationEntity
import java.util.UUID

@Repository
interface JpaCourseParticipationRepository : JpaRepository<CourseParticipationEntity, UUID> {
  fun findByPrisonNumber(prisonNumber: String): List<CourseParticipationEntity>
}
