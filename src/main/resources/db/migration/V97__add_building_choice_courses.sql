
ALTER TABLE course
    ADD COLUMN display_on_pgmdir BOOLEAN NOT NULL DEFAULT FALSE;
UPDATE course set display_on_pgmdir = true;

-- High Intensity building choices
INSERT INTO course (course_id, name, description, identifier, withdrawn, audience, audience_colour, list_display_name, display_on_pgmdir)
VALUES ('f925d6c5-d1be-4246-88ba-91bf77dbd101', 'Building Choices: high intensity', 'Building Choices helps people to develop skills for change and future-focused goals. It is a cognitive-behavioural programme, combining group and one-to-one sessions. It develops skills such as emotion management, healthy thinking, healthy relationships, sense of purpose and healthy sex. This is the high intensity pathway, including Getting Ready.', 'BCH-1', false,
        'Sexual offence', 'orange', 'Building Choices: high intensity', true);

INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email, referable, withdrawn) VALUES
        ('09a684c5-ebb1-43b3-ac63-b53c1da20630', 'f925d6c5-d1be-4246-88ba-91bf77dbd101', 'WTI', 'whattonprogrammes@justice.gov.uk', NULL, true, false),
        ('09a684c5-ebb1-43b3-ac63-b53c1da20631', 'f925d6c5-d1be-4246-88ba-91bf77dbd101', 'WMI', 'programmeswymott@justice.gov.uk', NULL, true, false),
        ('09a684c5-ebb1-43b3-ac63-b53c1da20632', 'f925d6c5-d1be-4246-88ba-91bf77dbd101', 'SFI', 'Programmesadmin.stafford@justice.gov.uk', NULL, true, false),
        ('09a684c5-ebb1-43b3-ac63-b53c1da20633', 'f925d6c5-d1be-4246-88ba-91bf77dbd101', 'SNI', 'Programmes.Swinfen@justice.gov.uk', NULL, true, false),
        ('09a684c5-ebb1-43b3-ac63-b53c1da20634', 'f925d6c5-d1be-4246-88ba-91bf77dbd101', 'FKI', 'Psychologyandprogrammes.frankland@justice.gov.uk', NULL, true, false),
        ('09a684c5-ebb1-43b3-ac63-b53c1da20635', 'f925d6c5-d1be-4246-88ba-91bf77dbd101', 'LPI', 'programmes.liverpool@justice.gov.uk', NULL, true, false),
        ('09a684c5-ebb1-43b3-ac63-b53c1da20636', 'f925d6c5-d1be-4246-88ba-91bf77dbd101', 'RSI', 'programmesrisley@justice.gov.uk', NULL, true, false);

INSERT INTO prerequisite(course_id, name, description)
VALUES ('f925d6c5-d1be-4246-88ba-91bf77dbd101', 'Setting', 'Custody or community'),
       ('f925d6c5-d1be-4246-88ba-91bf77dbd101', 'Gender', 'Any, depending on prison requirements'),
       ('f925d6c5-d1be-4246-88ba-91bf77dbd101', 'Risk criteria', 'High or very high in at least one of these areas:'),
       ('f925d6c5-d1be-4246-88ba-91bf77dbd101', 'Risk criteria', 'OGRS3 (Offender Group Reconviction Scale 3)'),
       ('f925d6c5-d1be-4246-88ba-91bf77dbd101', 'Risk criteria', 'OVP (OASys Violence Predictor)'),
       ('f925d6c5-d1be-4246-88ba-91bf77dbd101', 'Risk criteria', 'OSP-DC or OSP-IIC (OASys Sexual Reconviction Predictor – Contact or Indecent Imagery)'),
       ('f925d6c5-d1be-4246-88ba-91bf77dbd101', 'Risk criteria', 'RSR (Risk of Serious Recidivism)'),
       ('f925d6c5-d1be-4246-88ba-91bf77dbd101', 'Risk criteria', 'SARA (Spousal Assault Risk Assessment)'),
       ('f925d6c5-d1be-4246-88ba-91bf77dbd101', 'Needs criteria', 'High need, based on their programme needs identifier score'),
       ('f925d6c5-d1be-4246-88ba-91bf77dbd101', 'Suitable for people with learning disabilities or challenges (LDC)', 'Yes'),
       ('f925d6c5-d1be-4246-88ba-91bf77dbd101', 'Time to complete', 'Around 17 to 25 weeks (2 to 3 sessions a week)');

INSERT INTO course (course_id, name, description, identifier, withdrawn, audience, audience_colour, list_display_name, display_on_pgmdir)
VALUES ('f925d6c5-d1be-4246-88ba-91bf77dbd102', 'Building Choices: high intensity', 'Building Choices helps people to develop skills for change and future-focused goals. It is a cognitive-behavioural programme, combining group and one-to-one sessions. It develops skills such as emotion management, healthy thinking, healthy relationships, sense of purpose and healthy sex. This is the high intensity pathway, including Getting Ready.', 'BCH-2', false,
        'General offence', 'light-blue', 'Building Choices: high intensity', false);


INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email, referable, withdrawn) VALUES
        ('09a684c5-ebb1-43b3-ac63-b53c1da20671', 'f925d6c5-d1be-4246-88ba-91bf77dbd102', 'WMI', 'programmeswymott@justice.gov.uk', NULL, true, false),
        ('09a684c5-ebb1-43b3-ac63-b53c1da20672', 'f925d6c5-d1be-4246-88ba-91bf77dbd102', 'SNI', 'Programmes.Swinfen@justice.gov.uk', NULL, true, false),
        ('09a684c5-ebb1-43b3-ac63-b53c1da20673', 'f925d6c5-d1be-4246-88ba-91bf77dbd102', 'FKI', 'Psychologyandprogrammes.frankland@justice.gov.uk', NULL, true, false),
        ('09a684c5-ebb1-43b3-ac63-b53c1da20674', 'f925d6c5-d1be-4246-88ba-91bf77dbd102', 'BCI', 'programmesbuckleyhall@justice.gov.uk', NULL, true, false),
        ('09a684c5-ebb1-43b3-ac63-b53c1da20675', 'f925d6c5-d1be-4246-88ba-91bf77dbd102', 'HII', 'programmeshindley@justice.gov.uk', NULL, true, false),
        ('09a684c5-ebb1-43b3-ac63-b53c1da20676', 'f925d6c5-d1be-4246-88ba-91bf77dbd102', 'LPI', 'programmes.liverpool@justice.gov.uk', NULL, true, false),
        ('09a684c5-ebb1-43b3-ac63-b53c1da20677', 'f925d6c5-d1be-4246-88ba-91bf77dbd102', 'BWI', 'interventions.berwyn@justice.gov.uk', NULL, true, false),
        ('09a684c5-ebb1-43b3-ac63-b53c1da20678', 'f925d6c5-d1be-4246-88ba-91bf77dbd102', 'RSI', 'programmesrisley@justice.gov.uk', NULL, true, false),
        ('09a684c5-ebb1-43b3-ac63-b53c1da20679', 'f925d6c5-d1be-4246-88ba-91bf77dbd102', 'FBI', 'programmes@sodexogov.co.uk', NULL, true, false),
        ('09a684c5-ebb1-43b3-ac63-b53c1da20680', 'f925d6c5-d1be-4246-88ba-91bf77dbd102', 'ACI', 'AC.AccreditedInterventions@sodexogov.co.uk', NULL, true, false),
        ('09a684c5-ebb1-43b3-ac63-b53c1da20681', 'f925d6c5-d1be-4246-88ba-91bf77dbd102', 'LFI', 'programmes.lancasterfarms@justice.gov.uk', NULL, true, false),
        ('09a684c5-ebb1-43b3-ac63-b53c1da20682', 'f925d6c5-d1be-4246-88ba-91bf77dbd102', 'DHI', 'TBC', NULL, true, false),
        ('09a684c5-ebb1-43b3-ac63-b53c1da20683', 'f925d6c5-d1be-4246-88ba-91bf77dbd102', 'NHI', 'Newhall.programmes@justice.gov.uk', NULL, true, false),
        ('09a684c5-ebb1-43b3-ac63-b53c1da20684', 'f925d6c5-d1be-4246-88ba-91bf77dbd102', 'LNI', 'programmes.lownewton@justice.gov.uk', NULL, true, false);


INSERT INTO prerequisite(course_id, name, description)
VALUES ('f925d6c5-d1be-4246-88ba-91bf77dbd102', 'Setting', 'Custody or community'),
       ('f925d6c5-d1be-4246-88ba-91bf77dbd102', 'Gender', 'Any, depending on prison requirements'),
       ('f925d6c5-d1be-4246-88ba-91bf77dbd102', 'Risk criteria', 'High or very high in at least one of these areas:'),
       ('f925d6c5-d1be-4246-88ba-91bf77dbd102', 'Risk criteria', 'OGRS3 (Offender Group Reconviction Scale 3)'),
       ('f925d6c5-d1be-4246-88ba-91bf77dbd102', 'Risk criteria', 'OVP (OASys Violence Predictor)'),
       ('f925d6c5-d1be-4246-88ba-91bf77dbd102', 'Risk criteria', 'OSP-DC or OSP-IIC (OASys Sexual Reconviction Predictor – Contact or Indecent Imagery)'),
       ('f925d6c5-d1be-4246-88ba-91bf77dbd102', 'Risk criteria', 'RSR (Risk of Serious Recidivism)'),
       ('f925d6c5-d1be-4246-88ba-91bf77dbd102', 'Risk criteria', 'SARA (Spousal Assault Risk Assessment)'),
       ('f925d6c5-d1be-4246-88ba-91bf77dbd102', 'Needs criteria', 'High need, based on their programme needs identifier score'),
       ('f925d6c5-d1be-4246-88ba-91bf77dbd102', 'Suitable for people with learning disabilities or challenges (LDC)', 'Yes'),
       ('f925d6c5-d1be-4246-88ba-91bf77dbd102', 'Time to complete', 'Around 17 to 25 weeks (2 to 3 sessions a week)');


-- Moderate Intensity building choices

INSERT INTO course (course_id, name, description, identifier, withdrawn, audience, audience_colour, list_display_name, display_on_pgmdir)
VALUES ('f925d6c5-d1be-4246-88ba-91bf77dbd201', 'Building Choices: moderate intensity', 'Building Choices helps people to develop skills for change and future-focused goals. It is a cognitive-behavioural programme, combining group and one-to-one sessions. It develops skills such as emotion management, healthy thinking, healthy relationships, sense of purpose and healthy sex. This is the moderate intensity pathway.', 'BCM-1', false,
        'Sexual offence', 'orange', 'Building Choices: moderate intensity', true);


INSERT INTO prerequisite(course_id, name, description)
VALUES ('f925d6c5-d1be-4246-88ba-91bf77dbd201', 'Setting', 'Custody or community'),
       ('f925d6c5-d1be-4246-88ba-91bf77dbd201', 'Gender', 'Any, depending on prison requirements'),
       ('f925d6c5-d1be-4246-88ba-91bf77dbd201', 'Risk criteria', 'Medium in at least one of these areas:'),
       ('f925d6c5-d1be-4246-88ba-91bf77dbd201', 'Risk criteria', 'OGRS3 (Offender Group Reconviction Scale 3)'),
       ('f925d6c5-d1be-4246-88ba-91bf77dbd201', 'Risk criteria', 'OVP (OASys Violence Predictor)'),
       ('f925d6c5-d1be-4246-88ba-91bf77dbd201', 'Risk criteria', 'OSP-DC or OSP-IIC (OASys Sexual Reconviction Predictor – Contact or Indecent Imagery)'),
       ('f925d6c5-d1be-4246-88ba-91bf77dbd201', 'Risk criteria', 'RSR (Risk of Serious Recidivism)'),
       ('f925d6c5-d1be-4246-88ba-91bf77dbd201', 'Risk criteria', 'SARA (Spousal Assault Risk Assessment)'),
       ('f925d6c5-d1be-4246-88ba-91bf77dbd201', 'Risk criteria', 'The person should not score high in any risk areas for the moderate intensity pathway.'),
       ('f925d6c5-d1be-4246-88ba-91bf77dbd201', 'Needs criteria', 'Medium need for OGRS, OVP, OSP and SNSV. At least low need for SARA. This is based on their programme needs identifier score.'),
       ('f925d6c5-d1be-4246-88ba-91bf77dbd201', 'Suitable for people with learning disabilities or challenges (LDC)', 'Yes'),
       ('f925d6c5-d1be-4246-88ba-91bf77dbd201', 'Time to complete', 'Around 10 to 13 weeks (2 to 3 sessions a week)');

INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email, referable, withdrawn) VALUES
        ('09a684c5-ebb1-43b3-ac63-b53c1da20640', 'f925d6c5-d1be-4246-88ba-91bf77dbd201', 'WTI', 'whattonprogrammes@justice.gov.uk', NULL, true, false),
        ('09a684c5-ebb1-43b3-ac63-b53c1da20641', 'f925d6c5-d1be-4246-88ba-91bf77dbd201', 'WMI', 'programmeswymott@justice.gov.uk', NULL, true, false),
        ('09a684c5-ebb1-43b3-ac63-b53c1da20642', 'f925d6c5-d1be-4246-88ba-91bf77dbd201', 'SFI', 'Programmesadmin.stafford@justice.gov.uk', NULL, true, false),
        ('09a684c5-ebb1-43b3-ac63-b53c1da20643', 'f925d6c5-d1be-4246-88ba-91bf77dbd201', 'SNI', 'Programmes.Swinfen@justice.gov.uk', NULL, true, false),
        ('09a684c5-ebb1-43b3-ac63-b53c1da20644', 'f925d6c5-d1be-4246-88ba-91bf77dbd201', 'FKI', 'Psychologyandprogrammes.frankland@justice.gov.uk', NULL, true, false),
        ('09a684c5-ebb1-43b3-ac63-b53c1da20645', 'f925d6c5-d1be-4246-88ba-91bf77dbd201', 'LPI', 'programmes.liverpool@justice.gov.uk', NULL, true, false),
        ('09a684c5-ebb1-43b3-ac63-b53c1da20646', 'f925d6c5-d1be-4246-88ba-91bf77dbd201', 'RSI', 'programmesrisley@justice.gov.uk', NULL, true, false);

INSERT INTO course (course_id, name, description, identifier, withdrawn, audience, audience_colour, list_display_name, display_on_pgmdir)
VALUES ('f925d6c5-d1be-4246-88ba-91bf77dbd202', 'Building Choices: moderate intensity', 'Building Choices helps people to develop skills for change and future-focused goals. It is a cognitive-behavioural programme, combining group and one-to-one sessions. It develops skills such as emotion management, healthy thinking, healthy relationships, sense of purpose and healthy sex. This is the moderate intensity pathway.', 'BCM-2', false,
        'General offence', 'light-blue', 'Building Choices: moderate intensity', false);


INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email, referable, withdrawn) VALUES
        ('09a684c5-ebb1-43b3-ac63-b53c1da20651', 'f925d6c5-d1be-4246-88ba-91bf77dbd202', 'WMI', 'programmeswymott@justice.gov.uk', NULL, true, false),
        ('09a684c5-ebb1-43b3-ac63-b53c1da20653', 'f925d6c5-d1be-4246-88ba-91bf77dbd202', 'SNI', 'Programmes.Swinfen@justice.gov.uk', NULL, true, false),
        ('09a684c5-ebb1-43b3-ac63-b53c1da20654', 'f925d6c5-d1be-4246-88ba-91bf77dbd202', 'FKI', 'Psychologyandprogrammes.frankland@justice.gov.uk', NULL, true, false),
        ('09a684c5-ebb1-43b3-ac63-b53c1da20655', 'f925d6c5-d1be-4246-88ba-91bf77dbd202', 'BCI', 'programmesbuckleyhall@justice.gov.uk', NULL, true, false),
        ('09a684c5-ebb1-43b3-ac63-b53c1da20656', 'f925d6c5-d1be-4246-88ba-91bf77dbd202', 'HII', 'programmeshindley@justice.gov.uk', NULL, true, false),
        ('09a684c5-ebb1-43b3-ac63-b53c1da20657', 'f925d6c5-d1be-4246-88ba-91bf77dbd202', 'LPI', 'programmes.liverpool@justice.gov.uk', NULL, true, false),
        ('09a684c5-ebb1-43b3-ac63-b53c1da20658', 'f925d6c5-d1be-4246-88ba-91bf77dbd202', 'BWI', 'interventions.berwyn@justice.gov.uk', NULL, true, false),
        ('09a684c5-ebb1-43b3-ac63-b53c1da20659', 'f925d6c5-d1be-4246-88ba-91bf77dbd202', 'RSI', 'programmesrisley@justice.gov.uk', NULL, true, false),
        ('09a684c5-ebb1-43b3-ac63-b53c1da20660', 'f925d6c5-d1be-4246-88ba-91bf77dbd202', 'FBI', 'programmes@sodexogov.co.uk', NULL, true, false),
        ('09a684c5-ebb1-43b3-ac63-b53c1da20661', 'f925d6c5-d1be-4246-88ba-91bf77dbd202', 'ACI', 'AC.AccreditedInterventions@sodexogov.co.uk', NULL, true, false),
        ('09a684c5-ebb1-43b3-ac63-b53c1da20662', 'f925d6c5-d1be-4246-88ba-91bf77dbd202', 'LFI', 'programmes.lancasterfarms@justice.gov.uk', NULL, true, false),
        ('09a684c5-ebb1-43b3-ac63-b53c1da20663', 'f925d6c5-d1be-4246-88ba-91bf77dbd202', 'DHI', 'TBC', NULL, true, false),
        ('09a684c5-ebb1-43b3-ac63-b53c1da20664', 'f925d6c5-d1be-4246-88ba-91bf77dbd202', 'NHI', 'Newhall.programmes@justice.gov.uk', NULL, true, false),
        ('09a684c5-ebb1-43b3-ac63-b53c1da20665', 'f925d6c5-d1be-4246-88ba-91bf77dbd202', 'LNI', 'programmes.lownewton@justice.gov.uk', NULL, true, false);


INSERT INTO prerequisite(course_id, name, description)
VALUES ('f925d6c5-d1be-4246-88ba-91bf77dbd202', 'Setting', 'Custody or community'),
       ('f925d6c5-d1be-4246-88ba-91bf77dbd202', 'Gender', 'Any, depending on prison requirements'),
       ('f925d6c5-d1be-4246-88ba-91bf77dbd202', 'Risk criteria', 'Medium in at least one of these areas:'),
       ('f925d6c5-d1be-4246-88ba-91bf77dbd202', 'Risk criteria', 'OGRS3 (Offender Group Reconviction Scale 3)'),
       ('f925d6c5-d1be-4246-88ba-91bf77dbd202', 'Risk criteria', 'OVP (OASys Violence Predictor)'),
       ('f925d6c5-d1be-4246-88ba-91bf77dbd202', 'Risk criteria', 'OSP-DC or OSP-IIC (OASys Sexual Reconviction Predictor – Contact or Indecent Imagery)'),
       ('f925d6c5-d1be-4246-88ba-91bf77dbd202', 'Risk criteria', 'RSR (Risk of Serious Recidivism)'),
       ('f925d6c5-d1be-4246-88ba-91bf77dbd202', 'Risk criteria', 'SARA (Spousal Assault Risk Assessment)'),
       ('f925d6c5-d1be-4246-88ba-91bf77dbd202', 'Risk criteria', 'The person should not score high in any risk areas for the moderate intensity pathway.'),
       ('f925d6c5-d1be-4246-88ba-91bf77dbd202', 'Needs criteria', 'Medium need for OGRS, OVP, OSP and SNSV. At least low need for SARA. This is based on their programme needs identifier score.'),
       ('f925d6c5-d1be-4246-88ba-91bf77dbd202', 'Suitable for people with learning disabilities or challenges (LDC)', 'Yes'),
       ('f925d6c5-d1be-4246-88ba-91bf77dbd202', 'Time to complete', 'Around 10 to 13 weeks (2 to 3 sessions a week)');

-- create course variant

CREATE TABLE IF NOT EXISTS course_variant
(
    id UUID NOT NULL,
    course_id UUID NOT NULL REFERENCES course(course_id),
    variant_course_id UUID NOT NULL REFERENCES course(course_id),
    CONSTRAINT course_variant_courseid_fk FOREIGN KEY (course_id) REFERENCES course(course_id),
    CONSTRAINT course_variant_variant_courseid_fk FOREIGN KEY (variant_course_id) REFERENCES course(course_id)
);

INSERT INTO course_variant(id, course_id, variant_course_id) VALUES ('55104cd1-27e9-404a-82ab-ffd96e3dd37a', 'f925d6c5-d1be-4246-88ba-91bf77dbd101', 'f925d6c5-d1be-4246-88ba-91bf77dbd102');
INSERT INTO course_variant(id, course_id, variant_course_id) VALUES ('55104cd1-27e9-404a-82ab-ffd96e3dd37b', 'f925d6c5-d1be-4246-88ba-91bf77dbd201', 'f925d6c5-d1be-4246-88ba-91bf77dbd202');

-- link offering with organisation

ALTER TABLE organisation ADD CONSTRAINT organisation_code UNIQUE(code);

