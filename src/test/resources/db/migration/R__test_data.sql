

INSERT INTO course(course_id, identifier, name, description, alternate_name)
VALUES ('d3abc217-75ee-46e9-a010-368f30282367', 'LC', 'Lime Course', 'Explicabo exercitationem non asperiores corrupti accusamus quidem autem amet modi. Mollitia tenetur fugiat quo aperiam quasi error consectetur. Fugit neque rerum velit rem laboriosam. Atque nostrum quam aspernatur excepturi laborum harum officia eveniet porro.', 'LC'),
       ('1811faa6-d568-4fc4-83ce-41118b902421', 'SC', 'Super Course', 'corona volva conturbo suscipit civis.', 'AC++'),
       ('d3abc217-75ee-46e9-a010-368f30282368', 'RC', 'RAPID Course', 'corona volva conturbo suscipit civis.', NULL),
       ('d3abc217-75ee-46e9-a010-368f30282369', 'CC', 'Custom Course', 'corona custom suscipit civis.', 'AC++'),
       ('d3abc217-75ee-46e9-a010-368f30282370', 'TC', 'Temp Course', 'Temp custom suscipit civis.', 'AC++');

INSERT INTO course_participation (course_participation_id, prison_number, course_name, source, detail, location, type, outcome_status, year_started, year_completed, created_by_username, created_date_time, last_modified_by_username, last_modified_date_time)
VALUES
    ('1c0fbebe-7768-4dbe-ae58-6036183dbeff', 'CpzK5Kx', 'Silver Course', NULL, '', 'Schulist End', 'COMMUNITY', 'INCOMPLETE', 2021, NULL, 'Celia Feest', '2023-10-05T12:48:20', NULL, NULL),
    ('9372880f-95ba-4cde-a71a-01e2a06a0644', 'A1234AA', 'Orchid Course', 'deaden', 'Xiphias ustilo solutio crux. Velociter corrumpo una usitas.', 'Schulist End', 'COMMUNITY', 'INCOMPLETE', 2021, NULL, 'Germaine Luettgen', '2023-10-19T09:40:39', NULL, NULL),
    ('0cff5da9-1e90-4ee2-a5cb-94dc49c4b004', 'A1234AA', 'Green Course', 'squirrel', 'Cimentarius tactus vitium armarium adnuo tibi tricesimus volo. Decet ad adimpleo. Alienus tantillus tibi caecus. Utilis quia beatae velociter quos comitatus caelestis. Currus canis decimus comparo veniam.', 'Schulist End', 'COMMUNITY', 'INCOMPLETE', 2021, 1985, 'Carmelo Conn', '2023-10-11T13:11:06', NULL, NULL),
    ('eb357e5d-5416-43bf-a8d2-0dc8fd92162e', 'A1234AA', 'Red Course', 'deaden', 'Absum calamitas capio. Aro defero vester temporibus venia utique vomica vito suppono. Acervus armarium officia confido speciosus vere texo laborum corrigo excepturi. Succurro deorsum utpote peior subito theologus. Tersus solvo suadeo vomica turbo causa cursus tolero celo quam.', 'Schulist End', 'CUSTODY', 'INCOMPLETE', 2021, NULL, 'Joanne Hamill', '2023-09-21 23:45:12', NULL, NULL),
    ('eb357e5d-5416-43bf-a8d2-0dc8fd92162f', 'B1234BB', 'Red Course', 'deaden', 'Absum calamitas capio. Aro defero vester temporibus venia utique vomica vito suppono. Acervus armarium officia confido speciosus vere texo laborum corrigo excepturi. Succurro deorsum utpote peior subito theologus. Tersus solvo suadeo vomica turbo causa cursus tolero celo quam.', 'Schulist End', 'CUSTODY', 'INCOMPLETE', 2021, 1985, 'Joanne Hamill', '2023-09-21 23:45:12', NULL, NULL);

INSERT INTO offering(offering_id, course_id, organisation_id, contact_email, secondary_contact_email)
VALUES
        ('790a2dfe-7de5-4504-bb9c-83e6e53a6537', 'd3abc217-75ee-46e9-a010-368f30282367', 'BWN', 'nobody-bwn@digital.justice.gov.uk', null),
        ('39b77a2f-7398-4d5f-b744-cdcefca12671', 'd3abc217-75ee-46e9-a010-368f30282367', 'BXI', 'nobody-bxi@digital.justice.gov.uk', null),

       ('d460428c-5cb8-4d73-a3ae-b7ac37b65fbc', '1811faa6-d568-4fc4-83ce-41118b902421', 'BWN', 'nobody-bwn@digital.justice.gov.uk', null),
       ('7fffcc6a-11f8-4713-be35-cf5ff1aee517', 'd3abc217-75ee-46e9-a010-368f30282367', 'MDI', 'nobody-mdi@digital.justice.gov.uk', 'nobody2-mdi@digital.justice.gov.uk'),
       ('7f98826a-616c-4414-a278-525fc02505a0', 'd3abc217-75ee-46e9-a010-368f30282368', 'MDI', 'nobody-mdi@digital.justice.gov.uk', 'nobody2-mdi@digital.justice.gov.uk'),
       ('fee62dde-87f5-4dfd-9a44-e80d48f64be9', 'd3abc217-75ee-46e9-a010-368f30282369', 'MDI', 'nobody-mdi@digital.justice.gov.uk', 'nobody2-mdi@digital.justice.gov.uk'),
       ('be1d407c-3cb5-4c7e-bfee-d104bc79213f', 'd3abc217-75ee-46e9-a010-368f30282370', 'MDI', 'nobody-mdi@digital.justice.gov.uk', 'nobody2-mdi@digital.justice.gov.uk');

INSERT INTO referral (referral_id, offering_id, prison_number, referrer_id, additional_information, oasys_confirmed, has_reviewed_programme_history, status, submitted_on)
VALUES
    ('0c46ed09-170b-4c0f-aee8-a24eeaeeddaa', 'd460428c-5cb8-4d73-a3ae-b7ac37b65fbc','1uXTfdH','038019','more information',false,false,'REFERRAL_STARTED',null),
    ('fae2ed00-057e-4179-9e55-f6a4f4874cf0', 'd460428c-5cb8-4d73-a3ae-b7ac37b65fbc','1uXTfdH','038019','more information',false,false,'REFERRAL_STARTED',null);