-- Add a new national UK wide organisation
INSERT INTO ORGANISATION (organisation_id, code, name, gender, is_national)
SELECT '814dbc8a-cab6-4ba8-a210-cb8e8f7a4484', 'NAT', 'United Kingdom', 'ANY', true
WHERE NOT EXISTS (SELECT 1
                  FROM ORGANISATION
                  WHERE code = 'NAT');

-- Add a new offering for the HSP-SO course linked to the national organisation
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT '06d6d856-dd50-4ce0-a25f-cafc8c37c79b',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'NAT',
       'NationalHSP@justice.gov.uk',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'NAT');
