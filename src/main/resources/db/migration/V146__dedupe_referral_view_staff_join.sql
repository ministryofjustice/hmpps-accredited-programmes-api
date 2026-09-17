-- APG-2679: dedupe the `staff` join in `referral_view` so a POM with
-- multiple staff rows sharing the same `staff_id` no longer causes the
-- view (and therefore the Custody caselist) to return the same referral
-- more than once.
--
-- Background
-- ----------
-- V121 defined the view with a plain
--   left outer join staff st on st.staff_id = r.primary_pom_staff_id
-- Production data is known to contain multiple staff rows sharing the
-- same `staff_id` (see V144 for the same finding on the surname
-- projections). Because `staff_id` is not unique, every referral whose
-- POM has N duplicates was returning N rows from the view. Hibernate
-- returns a `List<ReferralViewEntity>` and does not dedupe by `@Id`,
-- so the same referral appeared N times on the caselist. INC4684438
-- (A2519CZ) reproduced this: the prisoner's POM has 2 rows in `staff`
-- and both referrals for that prisoner rendered twice on the Assess
-- caselist.
--
-- Fix
-- ---
-- Replace the fan-out-prone join with a LATERAL subquery that returns
-- at most one row per referral, ordered by the staff `id` so the
-- winner is deterministic (same rule the application code uses in
-- `StaffRepository.findFirstByStaffIdOrderByIdAsc`, sibling PR
-- APG-2679/fix-staff-lookup-500-on-duplicate-staff-id). V144's
-- `idx_staff_staff_id` makes each lookup an index scan.
--
-- Nothing else about the view changes: same columns, same types, same
-- order, same `where r.deleted = false` filter. The SELECT list is
-- kept character-identical to V121 (lowercase keywords) so the diff
-- between V121 and V146 highlights only the semantic change.
--
-- Positive side effect
-- --------------------
-- `Page<>.totalElements` for callers of
-- `ReferralViewRepository.getReferralsByOrganisationId` /
-- `getReferralsByUsername` now reports the true unique referral count
-- rather than a fan-out-inflated count. Pagination page counts and
-- next/prev button state therefore become correct for prisoners whose
-- POM has duplicate `staff_id` rows.

DROP VIEW IF EXISTS referral_view;

create or replace view referral_view as
select r.referral_id,
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
       org.name as organisation_name,
       r.status,
       rs.description as status_description,
       rs.colour as status_colour,
       r.referrer_username,
       c.name as course_name,
       c.audience,
       r.submitted_on,
       p.sentence_type,
       st.username as primary_pom_username,
       r.has_ldc,
       case
           when c.list_display_name is not null then c.list_display_name
           else c.name
           end as list_display_name

from referral r
         left outer join person p on r.prison_number = p.prison_number
         left outer join offering o on o.offering_id = r.offering_id
         left outer join course c on c.course_id = o.course_id
         left outer join organisation org on org.code = o.organisation_id
         left outer join referral_status rs on rs.code = r.status
         left join lateral (
             select username
             from staff
             where staff_id = r.primary_pom_staff_id
             order by id
             limit 1
             ) st on true
where r.deleted = false;

