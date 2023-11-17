DELETE from course_audience;
DELETE from offering;
DELETE from prerequisite;
DELETE from audience;
DELETE from course;
DELETE from referral;

INSERT INTO audience(audience_id, audience_value)
VALUES ('7fffcc6a-11f8-4713-be35-cf5ff1aee517', 'Sexual offence'),
       ('7fffcc6a-11f8-4713-be35-cf5ff1aee518', 'Extremism offence'),
       ('7fffcc6a-11f8-4713-be35-cf5ff1aee519', 'Gang offence'),
       ('7fffcc6a-11f8-4713-be35-cf5ff1aee510', 'General violence offence'),
       ('7fffcc6a-11f8-4713-be35-cf5ff1aee511', 'Intimate partner violence offence'),
       ('7fffcc6a-11f8-4713-be35-cf5ff1aee512', 'General offence');

INSERT INTO course(course_id, name, identifier, description, alternate_name, referable)
VALUES ('d3abc217-75ee-46e9-a010-368f30282367', 'Lime Course', 'LC', 'Explicabo exercitationem non asperiores corrupti accusamus quidem autem amet modi. Mollitia tenetur fugiat quo aperiam quasi error consectetur. Fugit neque rerum velit rem laboriosam. Atque nostrum quam aspernatur excepturi laborum harum officia eveniet porro.', 'LC', true),
       ('28e47d30-30bf-4dab-a8eb-9fda3f6400e8', 'Azure Course', 'AC', 'Similique laborum incidunt sequi rem quidem incidunt incidunt dignissimos iusto. Explicabo nihil atque quod culpa animi quia aspernatur dolorem consequuntur.', 'AC++', true),
       ('1811faa6-d568-4fc4-83ce-41118b90242e', 'Violet Course', 'VC', 'Tenetur a quisquam facilis amet illum voluptas error. Eaque eum sunt odit dolor voluptatibus eius sint impedit. Illo voluptatem similique quod voluptate laudantium. Ratione suscipit tempore amet autem quam dolorum. Necessitatibus tenetur recusandae aliquam recusandae temporibus voluptate velit similique fuga. Id tempora doloremque.', null, true),
       ('2280539a-9b98-44e0-bf7b-f0fc8baf1524', 'Lemon Chiffon Course', 'LCC', 'Quis aute iure reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint obcaecat cupiditat non proident.', 'LeCCy', false);

INSERT INTO prerequisite(course_id, name, description)
VALUES ('d3abc217-75ee-46e9-a010-368f30282367', 'Setting', 'Custody'),
       ('d3abc217-75ee-46e9-a010-368f30282367', 'Risk criteria', 'High ESARA/SARA/OVP, High OGRS'),
       ('d3abc217-75ee-46e9-a010-368f30282367', 'Criminogenic needs', 'Relationships, Thinking and Behaviour, Attitudes, Lifestyle'),
       ('28e47d30-30bf-4dab-a8eb-9fda3f6400e8', 'Setting', 'Custody'),
       ('28e47d30-30bf-4dab-a8eb-9fda3f6400e8', 'Risk criteria', 'High ESARA/SARA/OVP, High OGRS'),
       ('28e47d30-30bf-4dab-a8eb-9fda3f6400e8', 'Criminogenic needs', 'Relationships, Thinking and Behaviour, Attitudes, Lifestyle'),
       ('1811faa6-d568-4fc4-83ce-41118b90242e', 'Setting', 'Custody'),
       ('1811faa6-d568-4fc4-83ce-41118b90242e', 'Risk criteria', 'High ESARA/SARA/OVP, High OGRS'),
       ('1811faa6-d568-4fc4-83ce-41118b90242e', 'Criminogenic needs', 'Relationships, Thinking and Behaviour, Attitudes, Lifestyle'),
       ('2280539a-9b98-44e0-bf7b-f0fc8baf1524', 'Setting', 'Custody'),
       ('2280539a-9b98-44e0-bf7b-f0fc8baf1524', 'Risk criteria', 'High ESARA/SARA/OVP, High OGRS'),
       ('2280539a-9b98-44e0-bf7b-f0fc8baf1524', 'Criminogenic needs', 'Relationships, Thinking and Behaviour, Attitudes, Lifestyle');

INSERT INTO offering(offering_id, course_id, organisation_id, contact_email, secondary_contact_email)
VALUES ('7fffcc6a-11f8-4713-be35-cf5ff1aee517', 'd3abc217-75ee-46e9-a010-368f30282367', 'MDI', 'nobody-mdi@digital.justice.gov.uk', null),
       ('790a2dfe-7de5-4504-bb9c-83e6e53a6537', 'd3abc217-75ee-46e9-a010-368f30282367', 'BMI', 'nobody-bmi@digital.justice.gov.uk', 'nobody2-bmi@digital.justice.gov.uk'),
       ('39b77a2f-7398-4d5f-b744-cdcefca12671', 'd3abc217-75ee-46e9-a010-368f30282367', 'BXI', 'nobody-bxi@digital.justice.gov.uk', null),
       ('20f3abc8-dd92-43ae-b88e-5797a0ad3f4b', '28e47d30-30bf-4dab-a8eb-9fda3f6400e8', 'MRI', 'nobody-mri@digital.justice.gov.uk', 'nobody2-mri@digital.justice.gov.uk'),
       ('b328ebc8-1f7b-4236-b4ac-30f50b43a92d', '1811faa6-d568-4fc4-83ce-41118b90242e', 'DNI', 'nobody-dni@digital.justice.gov.uk', null),
       ('dc23b226-6dc9-45b3-8f35-c76d95782f6f', '2280539a-9b98-44e0-bf7b-f0fc8baf1524', 'DNI', 'nobody-dni@digital.justice.gov.uk', null),
       ('1f754f7a-d908-4c23-a77a-5f0145018c43', '2280539a-9b98-44e0-bf7b-f0fc8baf1524', 'BXI', 'nobody-bxi@digital.justice.gov.uk', null);

INSERT INTO course_audience (course_id, audience_id)
VALUES ('28e47d30-30bf-4dab-a8eb-9fda3f6400e8', '7fffcc6a-11f8-4713-be35-cf5ff1aee517'),
       ('28e47d30-30bf-4dab-a8eb-9fda3f6400e8', '7fffcc6a-11f8-4713-be35-cf5ff1aee511'),
       ('28e47d30-30bf-4dab-a8eb-9fda3f6400e8', '7fffcc6a-11f8-4713-be35-cf5ff1aee512'),
       ('d3abc217-75ee-46e9-a010-368f30282367', '7fffcc6a-11f8-4713-be35-cf5ff1aee518'),
       ('d3abc217-75ee-46e9-a010-368f30282367', '7fffcc6a-11f8-4713-be35-cf5ff1aee519'),
       ('d3abc217-75ee-46e9-a010-368f30282367', '7fffcc6a-11f8-4713-be35-cf5ff1aee510'),
       ('1811faa6-d568-4fc4-83ce-41118b90242e', '7fffcc6a-11f8-4713-be35-cf5ff1aee518'),
       ('1811faa6-d568-4fc4-83ce-41118b90242e', '7fffcc6a-11f8-4713-be35-cf5ff1aee519'),
       ('2280539a-9b98-44e0-bf7b-f0fc8baf1524', '7fffcc6a-11f8-4713-be35-cf5ff1aee510');

INSERT INTO referral (referral_id, offering_id, prison_number, referrer_id, additional_information, oasys_confirmed, has_reviewed_programme_history, status, submitted_on)
VALUES ('c11fab18-dc8d-420c-9c82-d0edd373732d', '20f3abc8-dd92-43ae-b88e-5797a0ad3f4b', 'ABC1234', '5105a589-75b3-4ca0-9433-b96228c1c8f3', '', false, false, 'REFERRAL_STARTED', null);
VALUES ('9d38204c-bfe9-42e6-b751-f46cec067e57', '20f3abc8-dd92-43ae-b88e-5797a0ad3f4b', 'ABC1234', '5105a589-75b3-4ca0-9433-b96228c1c8f3', 'Loren ipsun dolor sit anet, consectetur adipisci elit, sed eiusnod tenpor incidunt ut labore et dolore nagna aliqua. Ut enin ad ninin venian, quis nostrun exercitationen ullan corporis suscipit laboriosan, nisi ut aliquid ex ea connodi consequatur. Quis aute iure reprehenderit in voluptate velit esse cillun dolore eu fugiat nulla pariatur. Excepteur sint obcaecat cupiditat non proident, sunt in culpa qui officia deserunt nollit anin id est laborun.', true, true, 'REFERRAL_SUBMITTED', '2023-10-05T12:48:20');
