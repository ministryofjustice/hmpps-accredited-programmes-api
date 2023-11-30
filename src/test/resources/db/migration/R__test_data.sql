INSERT INTO course(course_id, identifier, name, description, alternate_name, referable)
VALUES ('d3abc217-75ee-46e9-a010-368f30282367', 'SC', 'Super Course', 'Sample description', 'SC++', true),
       ('28e47d30-30bf-4dab-a8eb-9fda3f6400e8', 'CC', 'Custom Course', 'Sample description', 'CC', true),
       ('1811faa6-d568-4fc4-83ce-41118b90242e', 'RC', 'RAPID Course', 'Sample description', 'RC', false);

INSERT INTO audience(audience_id, audience_value)
VALUES ('1f7fdb91-49b3-4eae-9815-dbf30ddd30a0', 'General offence');

INSERT INTO course_audience(course_id, audience_id)
VALUES ('d3abc217-75ee-46e9-a010-368f30282367', '1f7fdb91-49b3-4eae-9815-dbf30ddd30a0');

INSERT INTO course_participation (course_participation_id, prison_number, course_name, source, detail, location, type, outcome_status, year_started, year_completed, created_by_username, created_date_time, last_modified_by_username, last_modified_date_time)
VALUES ('0cff5da9-1e90-4ee2-a5cb-94dc49c4b004', 'A1234AA', 'Green Course', 'squirrel', 'Some detail', 'Schulist End', 'COMMUNITY', 'INCOMPLETE', 2023, NULL, 'Carmelo Conn', '2023-10-11T13:11:06', NULL, NULL),
       ('eb357e5d-5416-43bf-a8d2-0dc8fd92162e', 'A1234AA', 'Red Course', 'deaden', 'Some detail', 'Schulist End', 'CUSTODY', 'INCOMPLETE', 2023, NULL, 'Joanne Hamill', '2023-09-21T23:45:12', NULL, NULL),
       ('882a5a16-bcb8-4d8b-9692-a3006dcecffb', 'B2345BB', 'Marzipan Course', 'Reader''s Digest', 'This participation will be deleted', 'Schulist End', 'CUSTODY', 'INCOMPLETE', 2023, NULL, 'Adele Chiellini', '2023-11-26T10:20:45', NULL, NULL),
       ('cc8eb19e-050a-4aa9-92e0-c654e5cfe281', 'C1234CC', 'Orange Course', 'squirrel', 'This participation will be updated', 'Schulist End', 'COMMUNITY', 'INCOMPLETE', 2023, NULL, 'Carmelo Conn', '2023-10-11T13:11:06', NULL, NULL);

INSERT INTO offering(offering_id, course_id, organisation_id, contact_email, secondary_contact_email)
VALUES ('790a2dfe-7de5-4504-bb9c-83e6e53a6537', 'd3abc217-75ee-46e9-a010-368f30282367', 'BWN', 'nobody-bwn@digital.justice.gov.uk', 'nobody2-bwn@digital.justice.gov.uk'),
       ('7fffcc6a-11f8-4713-be35-cf5ff1aee517', 'd3abc217-75ee-46e9-a010-368f30282367', 'MDI', 'nobody-mdi@digital.justice.gov.uk', 'nobody2-mdi@digital.justice.gov.uk');

INSERT INTO referral (referral_id, offering_id, prison_number, referrer_id, additional_information, oasys_confirmed, has_reviewed_programme_history, status, submitted_on)
VALUES ('0c46ed09-170b-4c0f-aee8-a24eeaeeddaa', '7fffcc6a-11f8-4713-be35-cf5ff1aee517', 'B2345BB', '123456', 'This referral will be updated', false, false, 'REFERRAL_STARTED', NULL),
       ('fae2ed00-057e-4179-9e55-f6a4f4874cf0', '790a2dfe-7de5-4504-bb9c-83e6e53a6537','C3456CC', '234567', 'more information', true, true, 'REFERRAL_SUBMITTED', '2023-11-12T19:11:00'),
       ('153383a4-b250-46a8-9950-43eb358c2805', '790a2dfe-7de5-4504-bb9c-83e6e53a6537','D3456DD', '234567', 'more information', true, true, 'REFERRAL_SUBMITTED', '2023-11-13T19:11:00');
