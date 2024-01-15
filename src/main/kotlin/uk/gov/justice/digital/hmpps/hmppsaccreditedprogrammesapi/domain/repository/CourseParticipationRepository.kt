package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseParticipationEntity
import java.util.UUID

@Repository
interface CourseParticipationRepository : JpaRepository<CourseParticipationEntity, UUID> {
  fun findByPrisonNumber(prisonNumber: String): List<CourseParticipationEntity>
}
