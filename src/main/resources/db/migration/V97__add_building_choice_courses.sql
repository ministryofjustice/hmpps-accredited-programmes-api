
ALTER TABLE course
    ADD COLUMN display_on_pgmdir BOOLEAN NOT NULL DEFAULT FALSE;
UPDATE course set display_on_pgmdir = true;

-- High Intensity building choices
INSERT INTO course (course_id, name, description, identifier, withdrawn, audience, audience_colour, list_display_name, display_on_pgmdir)
VALUES ('f925d6c5-d1be-4246-88ba-91bf77dbd101', 'Building Choices: high intensity', 'Building Choices helps people to develop skills for change and future-focused goals. It is a cognitive-behavioural programme, combining group and one-to-one sessions. It develops skills such as emotion management, healthy thinking, healthy relationships, sense of purpose and healthy sex. This is the high intensity pathway, including Getting Ready.', 'BCH-1', false,
        'Sexual offence', 'orange', 'Building Choices: high intensity', true);

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
        'All offences', 'light-blue', 'Building Choices: high intensity', false);


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

INSERT INTO course (course_id, name, description, identifier, withdrawn, audience, audience_colour, list_display_name, display_on_pgmdir)
VALUES ('f925d6c5-d1be-4246-88ba-91bf77dbd202', 'Building Choices: moderate intensity', 'Building Choices helps people to develop skills for change and future-focused goals. It is a cognitive-behavioural programme, combining group and one-to-one sessions. It develops skills such as emotion management, healthy thinking, healthy relationships, sense of purpose and healthy sex. This is the moderate intensity pathway.', 'BCM-2', false,
        'All offences', 'light-blue', 'Building Choices: moderate intensity', false);


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
ALTER TABLE offering ADD CONSTRAINT offering_organisation_fk FOREIGN KEY (organisation_id) REFERENCES organisation(code);

