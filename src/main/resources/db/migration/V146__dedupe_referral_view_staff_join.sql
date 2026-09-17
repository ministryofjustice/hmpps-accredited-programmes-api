-- APG-2679: dedupe the `staff` join in `referral_view` so a POM with
-- multiple staff rows sharing the same `staff_id` no longer causes the
-- view (and therefore the Custody caselist) to return the same referral
-- more than once.
--
-- Background
-- ----------
-- V121 defined the view with a plain
--   LEFT OUTER JOIN staff st ON st.staff_id = r.primary_pom_staff_id
-- Production data is known to contain multiple staff rows sharing the
-- same `staff_id` (see V144 for the same finding on the surname
-- projections). Because `staff_id` is not unique, every referral whose
-- POM has N duplicates was returning N rows from the view. Hibernate
-- returns a `List<ReferralViewEntity>` and does not dedupe by `@Id`, so
-- the same referral appeared N times on the caselist. INC4684438 (A2519CZ)
-- reproduced this: the prisoner's POM has 2 rows in `staff`, and both
-- referrals for that prisoner rendered twice on the Assess caselist.
--
-- Fix
-- ---
-- Replace the fan-out-prone join with a LATERAL subquery that returns
-- at most one row per referral, ordered by the staff `id` so the
-- winner is deterministic (same rule the application code uses in
-- `StaffRepository.findFirstByStaffIdOrderByIdAsc`, sibling PR
-- APG-2679/fix-staff-lookup-500-on-duplicate-staff-id).
--
-- Nothing else about the view changes: same columns, same types, same
-- order, same `where r.deleted = false` filter.

DROP VIEW IF EXISTS referral_view;

CREATE OR REPLACE VIEW referral_view AS
SELECT r.referral_id,
       r.prison_number,
       p.forename,
       p.surname,
       p.conditional_release_date,
       p.parole_eligibility_date,
       p.tariff_expiry_date,
       p.earliest_release_date,
       p.earliest_release_date_type,
       p.non_dto_release_date_type,
       p.location,
       o.organisation_id,
       org.name AS organisation_name,
       r.status,
       rs.description AS status_description,
       rs.colour AS status_colour,
       r.referrer_username,
       c.name AS course_name,
       c.audience,
       r.submitted_on,
       p.sentence_type,
       st.username AS primary_pom_username,
       r.has_ldc,
       CASE
           WHEN c.list_display_name IS NOT NULL THEN c.list_display_name
           ELSE c.name
           END AS list_display_name

FROM referral r
         LEFT OUTER JOIN person p ON r.prison_number = p.prison_number
         LEFT OUTER JOIN offering o ON o.offering_id = r.offering_id
         LEFT OUTER JOIN course c ON c.course_id = o.course_id
         LEFT OUTER JOIN organisation org ON org.code = o.organisation_id
         LEFT OUTER JOIN referral_status rs ON rs.code = r.status
         LEFT JOIN LATERAL (
             SELECT username
             FROM staff
             WHERE staff_id = r.primary_pom_staff_id
             ORDER BY id
             LIMIT 1
             ) st ON TRUE
WHERE r.deleted = false;

