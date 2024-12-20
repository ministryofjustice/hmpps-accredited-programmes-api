package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseParticipationEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseStatus
import java.util.UUID

@Repository
interface CourseParticipationRepository : JpaRepository<CourseParticipationEntity, UUID> {

  @Modifying
  @Query(
    """
        UPDATE CourseParticipationEntity cp
        SET cp.isDraft = false
        WHERE cp.referralId = :referralId
        """,
  )
  fun updateDraftStatusByReferralId(@Param("referralId") referralId: UUID)

  fun deleteByReferralId(referralId: UUID)

  fun findByPrisonNumber(prisonNumber: String): List<CourseParticipationEntity>

  fun findByPrisonNumberAndOutcomeStatusIn(prisonNumber: String, outcomes: List<CourseStatus>): List<CourseParticipationEntity>

  fun findByReferralId(referralId: UUID): List<CourseParticipationEntity>

  @Query(
    """
        SELECT cp FROM CourseParticipationEntity cp
        WHERE cp.prisonNumber = :prisonerNumber
        """,
  )
  fun getSarParticipations(
    @Param("prisonerNumber") prisonerNumber: String,
  ): List<CourseParticipationEntity>
}
