package uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.hmppsaccreditedprogrammesapi.domain.entity.create.ReferralEntity
import java.math.BigInteger
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
        and r.submitted_on < :endDate
        and ( :locationCodes is null OR o.organisation_id in :locationCodes )
        and r.deleted = false;
          """,
    nativeQuery = true,
  )
  fun referralCount(
    startDate: LocalDate,
    endDate: LocalDate,
    locationCodes: List<String>?,
  ): String

  @Query(
    """
        SELECT json_build_object(
        'count', sum(count),
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
      AND r.submitted_on < :endDate
      and ( :locationCodes is null OR o.organisation_id in :locationCodes )
      AND r.deleted = FALSE
    GROUP BY c.name, c.audience
) AS subquery;
          """,
    nativeQuery = true,
  )
  fun referralCountByCourse(
    startDate: LocalDate,
    endDate: LocalDate,
    locationCodes: List<String>?,
  ): String

  @Query(
    """
       SELECT json_build_object(
              'count', sum(count),
              'courseCounts', json_agg(json_build_object(
              'name', name,
              'audience', audience,
              'count', count))
       ) AS result
        FROM (
         SELECT
             c.name as name,
             c.audience as audience,
             COUNT(*) AS count
         FROM referral r
                  JOIN offering o ON r.offering_id = o.offering_id
                  JOIN course c ON o.course_id = c.course_id
                  JOIN referral_status_history rsh ON rsh.referral_id = r.referral_id
         WHERE r.deleted = FALSE
           AND rsh.status = :statusCode
           AND rsh.status_start_date >= :startDate
           AND rsh.status_start_date < :endDate
           AND ( :locationCodes is null OR o.organisation_id in :locationCodes )
         GROUP BY c.name, c.audience
     ) AS subquery;
          """,
    nativeQuery = true,
  )
  fun finalStatusCodeCounts(
    startDate: LocalDate,
    endDate: LocalDate,
    locationCodes: List<String>?,
    statusCode: String,
  ): String

  @Query(
    """
        WITH aggregated_courses AS (
    SELECT
        c.name AS name,
        c.audience AS audience,
        r.status AS status,
        COUNT(*) AS count,
        COUNT(r.referral_id) AS referral_count
    FROM
        referral r
            JOIN offering o ON r.offering_id = o.offering_id
            JOIN course c ON o.course_id = c.course_id
    WHERE
        r.status IN :statusCodes
      AND ( :locationCodes is null OR o.organisation_id in :locationCodes )
      AND r.deleted = FALSE
    GROUP BY
        c.name, c.audience, r.status
),
     aggregated_statuses AS (
         SELECT
             status,
             json_agg(json_build_object(
                     'name', name,
                     'audience', audience,
                     'count', count
                      )) AS courseCounts,
             SUM(count) AS total_count,
             SUM(referral_count) AS referral_count
         FROM
             aggregated_courses
         GROUP BY
             status
     )
SELECT
    json_build_object(
            'totalCount', SUM(referral_count),
            'statusContent', json_agg(json_build_object(
            'status', status,
            'countAtStatus', referral_count,
            'courseCounts', courseCounts
             ))
    ) AS result
FROM
    aggregated_statuses;

          """,
    nativeQuery = true,
  )
  fun currentCountsByStatus(
    statusCodes: List<String>,
    locationCodes: List<String>?,
  ): String

  @Query(
    """
    SELECT json_build_object(
                             'performance', json_agg(
                        json_build_object(
                        'status', status,
                        'averageDuration',
                        avg_days || ' days ' || avg_hours || ' hours ' || avg_minutes || ' minutes ' || avg_seconds || ' seconds',
                        'minDuration',
                        min_days || ' days ' || min_hours || ' hours ' || min_minutes || ' minutes ' || min_seconds || ' seconds',
                        'maxDuration',
                        max_days || ' days ' || max_hours || ' hours ' || max_minutes || ' minutes ' || max_seconds || ' seconds'
                )
            )
       ) AS result
FROM (
         SELECT
            rsh.status AS status,
             FLOOR(AVG(rsh.duration_at_this_status) / 86400000) AS avg_days,
             FLOOR((AVG(rsh.duration_at_this_status) % 86400000) / 3600000) AS avg_hours,
             FLOOR((AVG(rsh.duration_at_this_status) % 3600000) / 60000) AS avg_minutes,
             FLOOR((AVG(rsh.duration_at_this_status) % 60000) / 1000) AS avg_seconds,

             FLOOR(MIN(rsh.duration_at_this_status) / 86400000) AS min_days,
             FLOOR((MIN(rsh.duration_at_this_status) % 86400000) / 3600000) AS min_hours,
             FLOOR((MIN(rsh.duration_at_this_status) % 3600000) / 60000) AS min_minutes,
             FLOOR((MIN(rsh.duration_at_this_status) % 60000) / 1000) AS min_seconds,

             FLOOR(MAX(rsh.duration_at_this_status) / 86400000) AS max_days,
             FLOOR((MAX(rsh.duration_at_this_status) % 86400000) / 3600000) AS max_hours,
             FLOOR((MAX(rsh.duration_at_this_status) % 3600000) / 60000) AS max_minutes,
             FLOOR((MAX(rsh.duration_at_this_status) % 60000) / 1000) AS max_seconds
         FROM referral r
                  JOIN offering o ON r.offering_id = o.offering_id
                  JOIN course c ON o.course_id = c.course_id
                  JOIN referral_status_history rsh ON rsh.referral_id = r.referral_id
         WHERE r.deleted = FALSE
           AND rsh.status IN :statusCodes
           AND ( :locationCodes is null OR o.organisation_id in :locationCodes )
           AND rsh.status_end_date >= :startDate
           AND rsh.status_end_date < :endDate
           AND rsh.duration_at_this_status IS NOT NULL
         GROUP BY rsh.status
     ) AS subquery;
          """,
    nativeQuery = true,
  )
  fun averageTime(
    startDate: LocalDate,
    endDate: LocalDate,
    statusCodes: List<String>,
    locationCodes: List<String>?,
  ): String

  @Query(
    """
 SELECT json_build_object(
               'count', sum(count),
               'pniCounts', json_agg(json_build_object(
                'pathway', pathway,
                'count', count))
       ) AS result
FROM (
         SELECT
             pni.programme_pathway as pathway,
             COUNT(*) AS count
         FROM referral r
                  JOIN offering o ON r.offering_id = o.offering_id
                  JOIN course c ON o.course_id = c.course_id
                  JOIN pni_result pni ON pni.referral_id = r.referral_id
         WHERE r.deleted = FALSE
           AND pni.pni_assessment_date >= :startDate
           AND pni.pni_assessment_date < :endDate
           AND ( :locationCodes is null OR o.organisation_id in :locationCodes )
         GROUP BY pni.programme_pathway
     ) AS subquery;
          """,
    nativeQuery = true,
  )
  fun pniPathwayCounts(startDate: LocalDate, endDate: LocalDate, locationCodes: List<String>?): String?

  @Query(
    """
       SELECT
         COUNT(*) AS count,
         rsh.status AS status,
         o.organisation_id AS orgId
       FROM referral r
       JOIN offering o ON r.offering_id = o.offering_id
       JOIN course c ON o.course_id = c.course_id
       JOIN referral_status_history rsh ON rsh.referral_id = r.referral_id
       WHERE r.deleted = FALSE
         AND rsh.status_start_date >= :startDate
         AND rsh.status_start_date < :endDate
         AND ( :locationCodes is null OR o.organisation_id in :locationCodes )
       GROUP BY rsh.status, o.organisation_id
       ORDER BY o.organisation_id, rsh.status
          """,
    nativeQuery = true,
  )
  fun referralCountByStatus(
    startDate: LocalDate,
    endDate: LocalDate,
    locationCodes: List<String>?,
  ): List<ReportStatusCountProjection>?

  interface ReportStatusCountProjection {
    fun getCount(): BigInteger
    fun getStatus(): String
    fun getOrgId(): String
  }
}
