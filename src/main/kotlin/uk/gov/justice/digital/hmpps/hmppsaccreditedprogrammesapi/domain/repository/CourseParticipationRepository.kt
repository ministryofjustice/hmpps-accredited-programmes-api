package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseParticipationEntity
import java.time.LocalDateTime
import java.util.UUID

@Repository
interface CourseParticipationRepository : JpaRepository<CourseParticipationEntity, UUID> {
  fun findByPrisonNumber(prisonNumber: String): List<CourseParticipationEntity>

  @Query(
    """
        SELECT cp FROM CourseParticipationEntity cp
        WHERE cp.prisonNumber = :prisonerNumber
        AND (:fromDate IS NULL OR (cp.lastModifiedDateTime IS NOT NULL AND cp.lastModifiedDateTime >= :fromDate) OR (cp.lastModifiedDateTime IS NULL AND cp.createdDateTime >= :fromDate))
        AND (:toDate IS NULL OR (cp.lastModifiedDateTime IS NOT NULL AND cp.lastModifiedDateTime <= :toDate) OR (cp.lastModifiedDateTime IS NULL AND cp.createdDateTime <= :toDate))
        """,
  )
  fun getSarParticipations(prisonerNumber: String, fromDate: LocalDateTime?, toDate: LocalDateTime?): List<CourseParticipationEntity>
}
