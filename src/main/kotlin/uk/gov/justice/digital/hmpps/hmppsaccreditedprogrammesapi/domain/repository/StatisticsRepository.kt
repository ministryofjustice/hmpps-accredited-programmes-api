package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity
import java.time.LocalDate
import java.util.UUID

@Repository
interface StatisticsRepository : JpaRepository<ReferralEntity, UUID> {

  @Query(
    """
        select json_build_object(
        'count', count(*)
        ) as result
        from referral r
      join offering o on r.offering_id = o.offering_id
      where r.submitted_on >= :startDate 
        and r.submitted_on <= :endDate
        and ( :locationCode is null OR o.organisation_id = :locationCode )
        and r.deleted = false;
          """,
    nativeQuery = true,
  )
  fun referralCount(
    startDate: LocalDate,
    endDate: LocalDate,
    locationCode: String?,
  ): String

  @Query(
    """
        SELECT json_build_object(
        'courseCounts', json_agg(json_build_object(
                'name', name,
                'audience', audience,
                'count', count
        ))
    ) AS result
FROM (
    SELECT 
        c.name as name,
        c.audience as audience,
        COUNT(*) AS count
    FROM referral r
    JOIN offering o ON r.offering_id = o.offering_id
    JOIN course c ON o.course_id = c.course_id
    WHERE r.submitted_on >= :startDate 
      AND r.submitted_on <= :endDate
      AND (:locationCode IS NULL OR o.organisation_id = :locationCode)
      AND r.deleted = FALSE
    GROUP BY c.name, c.audience
) AS subquery;
          """,
    nativeQuery = true,
  )
  fun referralCountByCourse(
    startDate: LocalDate,
    endDate: LocalDate,
    locationCode: String?,
  ): String
}
