
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
where r.deleted = false;
