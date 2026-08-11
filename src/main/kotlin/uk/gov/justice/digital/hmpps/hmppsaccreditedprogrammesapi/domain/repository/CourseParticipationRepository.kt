package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseParticipationEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.CourseStatus
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository.projection.CourseParticipationProjection
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

  fun deleteCourseParticipationEntitiesByReferralIdIsIn(referralIds: List<UUID>)

  fun findByPrisonNumber(prisonNumber: String): List<CourseParticipationEntity>

  fun findByPrisonNumberAndOutcomeStatusIn(prisonNumber: String, outcomes: List<CourseStatus>): List<CourseParticipationEntity>

  fun findByReferralId(referralId: UUID): List<CourseParticipationEntity>

  @Query(
    value = """
        SELECT 
            cp.prison_number AS prisonNumber,
            cp.course_participation_id AS id,
            cp.referral_id AS referralId,
            cp.year_started as yearStarted,
            cp.year_completed as yearCompleted,
            cp.outcome_status AS outcomeStatus,
            cp.source AS source,
            cp.course_name AS courseName,
            cp.type AS type,
            cp.detail AS detail,
            cp.location AS location,
            cp.is_draft AS isDraft,
            cp.created_date_time AS createdAt,
            cp.created_by_username AS addedBy,
            r.status AS referralStatus
        FROM course_participation cp
        INNER JOIN referral r ON cp.referral_id = r.referral_id
        WHERE cp.referral_id = :referralId
    """,
    nativeQuery = true,
  )
  fun findCourseParticipationByReferralId(@Param("referralId") referralId: UUID): List<CourseParticipationProjection>

  /**
   * Batch load of every course-participation row for [prisonerNumber], used by
   * the SAR (Subject Access Request) custody report.
   *
   * `ORDER BY cp.createdDateTime NULLS LAST, cp.id` guarantees deterministic
   * ordering across runs so the SAR contract-test golden snapshot doesn't
   * flake when a subject has more than one participation row (Postgres
   * otherwise returns join order unspecified). Chronological ascending is the
   * natural read for a vettor; `cp.id` is the primary key tie-break for
   * same-timestamp rows.
   */
  @Query(
    """
        SELECT cp FROM CourseParticipationEntity cp
        WHERE cp.prisonNumber = :prisonerNumber
        ORDER BY cp.createdDateTime NULLS LAST, cp.id
        """,
  )
  fun getSarParticipations(
    @Param("prisonerNumber") prisonerNumber: String,
  ): List<CourseParticipationEntity>
}
