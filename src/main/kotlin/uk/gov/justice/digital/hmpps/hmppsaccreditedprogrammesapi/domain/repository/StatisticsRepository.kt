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
                        to_char(justify_interval((avg_duration_at_this_status / 1000) * interval '1 second'), 'DD "days" HH24 "hours" MI "minutes" SS "seconds"'),
                        'minDuration',
                        to_char(justify_interval((min_duration / 1000) * interval '1 second'), 'DD "days" HH24 "hours" MI "minutes" SS "seconds"'),
                        'maxDuration',
                        to_char(justify_interval((max_duration / 1000) * interval '1 second'), 'DD "days" HH24 "hours" MI "minutes" SS "seconds"')
                )
                              )
       ) AS result
FROM (
         SELECT
             rsh.status AS status,
             AVG(rsh.duration_at_this_status) AS avg_duration_at_this_status,
             MIN(rsh.duration_at_this_status) AS min_duration,
             MAX(rsh.duration_at_this_status) AS max_duration
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
}
