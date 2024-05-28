ALTER TABLE referral
    ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT false;


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
       r.deleted,
    CASE
           WHEN c.list_display_name IS NOT NULL THEN c.list_display_name
           ELSE c.name
    END AS list_display_name
FROM referral r
         LEFT OUTER JOIN person p ON r.prison_number = p.prison_number
         LEFT OUTER JOIN offering o ON o.offering_id = r.offering_id
         LEFT OUTER JOIN course c ON c.course_id = o.course_id
         LEFT OUTER JOIN organisation org ON org.code = o.organisation_id
         LEFT OUTER JOIN referral_status rs ON rs.code = r.status;