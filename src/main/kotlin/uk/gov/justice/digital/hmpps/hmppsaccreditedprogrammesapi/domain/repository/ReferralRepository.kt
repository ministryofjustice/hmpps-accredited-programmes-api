package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository

import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.projection.ReferralSummaryProjection
import java.util.*

@Repository
interface ReferralRepository : JpaRepository<ReferralEntity, UUID> {
  @Query(
    value = """
    SELECT NEW uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.projection.ReferralSummaryProjection(
      r.id AS referralId,
      c.name AS courseName, 
      a.value as audience,
      r.status AS status,
      r.submittedOn AS submittedOn,
      r.prisonNumber AS prisonNumber,
      ru.username AS referralUsername,
      o.organisationId AS organisationId
    ) FROM ReferralEntity r
    JOIN r.offering o
    JOIN o.course c
    JOIN c.audiences a
    JOIN r.referrer ru
    WHERE o.organisationId = :organisationId
      AND (:status IS NULL OR r.status IN :status)
      AND (:audience IS NULL OR :audience = '' OR a.value = :audience)
      AND (:courseName IS NULL OR :courseName = '' OR LOWER(c.name) LIKE LOWER(CONCAT('%', :courseName, '%')))
  """,
    countQuery = """
    SELECT COUNT(DISTINCT r.id)
    FROM ReferralEntity r
    JOIN r.offering o
    JOIN o.course c
    JOIN c.audiences a
    WHERE o.organisationId = :organisationId
      AND (:status IS NULL OR :status = '' OR r.status IN :status)
      AND (:audience IS NULL OR :audience = '' OR a.value = :audience)
      AND (:courseName IS NULL OR :courseName = '' OR LOWER(c.name) LIKE LOWER(CONCAT('%', :courseName, '%')))
  """,
    nativeQuery = false,
  )
  fun getReferralsByOrganisationId(
    organisationId: String,
    pageable: Pageable,
    status: List<ReferralEntity.ReferralStatus>?,
    audience: String?,
    courseName: String?,
  ): Page<ReferralSummaryProjection>

  @Query(
    value = """
    SELECT NEW uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.projection.ReferralSummaryProjection(
      r.id AS referralId,
      c.name AS courseName, 
      a.value as audience,
      r.status AS status,
      r.submittedOn AS submittedOn,
      r.prisonNumber AS prisonNumber,
      ru.username AS referralUsername,
      o.organisationId AS organisationId
    ) FROM ReferralEntity r
    JOIN r.offering o
    JOIN o.course c
    JOIN c.audiences a
    JOIN r.referrer ru
    WHERE ru.username = :username
      AND (:status IS NULL OR r.status IN :status)
      AND (:audience IS NULL OR :audience = '' OR a.value = :audience)
      AND (:courseName IS NULL OR :courseName = '' OR LOWER(c.name) LIKE LOWER(CONCAT('%', :courseName, '%')))
  """,
    countQuery = """
    SELECT COUNT(DISTINCT r.id)
    FROM ReferralEntity r
    JOIN r.offering o
    JOIN o.course c
    JOIN c.audiences a
    JOIN r.referrer ru
    WHERE ru.username = :username
      AND (:status IS NULL OR :status = '' OR r.status IN :status)
      AND (:audience IS NULL OR :audience = '' OR a.value = :audience)
      AND (:courseName IS NULL OR :courseName = '' OR LOWER(c.name) LIKE LOWER(CONCAT('%', :courseName, '%')))
  """,
    nativeQuery = false,
  )
  fun getReferralsByUsername(
    username: String,
    pageable: Pageable,
    status: List<ReferralEntity.ReferralStatus>?,
    audience: String?,
    courseName: String?,
  ): Page<ReferralSummaryProjection>

  @Query(
    value = """
    SELECT 
     DISTINCT r.prisonNumber AS prisonNumber
     FROM ReferralEntity r
  """,
    nativeQuery = false,
  )
  fun getDistinctPrisonNumbers(): List<String>
}
