INSERT INTO audience(audience_id, audience_value)
VALUES ('7fffcc6a-11f8-4713-be35-cf5ff1aee517', 'Sexual violence');

INSERT INTO course(course_id, name, description)
VALUES ('d3abc217-75ee-46e9-a010-368f30282367', 'Lime Course', 'Explicabo exercitationem non asperiores corrupti accusamus quidem autem amet modi. Mollitia tenetur fugiat quo aperiam quasi error consectetur. Fugit neque rerum velit rem laboriosam. Atque nostrum quam aspernatur excepturi laborum harum officia eveniet porro.'),
       ('28e47d30-30bf-4dab-a8eb-9fda3f6400e8', 'Azure Course', 'Similique laborum incidunt sequi rem quidem incidunt incidunt dignissimos iusto. Explicabo nihil atque quod culpa animi quia aspernatur dolorem consequuntur.'),
       ('1811faa6-d568-4fc4-83ce-41118b90242e', 'Violet Course', 'Tenetur a quisquam facilis amet illum voluptas error. Eaque eum sunt odit dolor voluptatibus eius sint impedit. Illo voluptatem similique quod voluptate laudantium. Ratione suscipit tempore amet autem quam dolorum. Necessitatibus tenetur recusandae aliquam recusandae temporibus voluptate velit similique fuga. Id tempora doloremque.');

INSERT INTO prerequisite(course_id, name, description)
VALUES ('d3abc217-75ee-46e9-a010-368f30282367', 'Setting', 'Custody'),
       ('d3abc217-75ee-46e9-a010-368f30282367', 'Risk criteria', 'High ESARA/SARA/OVP, High OGRS'),
       ('d3abc217-75ee-46e9-a010-368f30282367', 'Criminogenic needs', 'Relationships, Thinking and Behaviour, Attitudes, Lifestyle'),
       ('28e47d30-30bf-4dab-a8eb-9fda3f6400e8', 'Setting', 'Custody'),
       ('28e47d30-30bf-4dab-a8eb-9fda3f6400e8', 'Risk criteria', 'High ESARA/SARA/OVP, High OGRS'),
       ('28e47d30-30bf-4dab-a8eb-9fda3f6400e8', 'Criminogenic needs', 'Relationships, Thinking and Behaviour, Attitudes, Lifestyle'),
       ('1811faa6-d568-4fc4-83ce-41118b90242e', 'Setting', 'Custody'),
       ('1811faa6-d568-4fc4-83ce-41118b90242e', 'Risk criteria', 'High ESARA/SARA/OVP, High OGRS'),
       ('1811faa6-d568-4fc4-83ce-41118b90242e', 'Criminogenic needs', 'Relationships, Thinking and Behaviour, Attitudes, Lifestyle');

INSERT INTO offering(offering_id, course_id, organisation_id, contact_email)
VALUES ('7fffcc6a-11f8-4713-be35-cf5ff1aee517', 'd3abc217-75ee-46e9-a010-368f30282367', 'MDI', 'nobody-mdi@digital.justice.gov.uk'),
       ('790a2dfe-7de5-4504-bb9c-83e6e53a6537', 'd3abc217-75ee-46e9-a010-368f30282367', 'BWN', 'nobody-bwn@digital.justice.gov.uk'),
       ('39b77a2f-7398-4d5f-b744-cdcefca12671', 'd3abc217-75ee-46e9-a010-368f30282367', 'BXI', 'nobody-bxi@digital.justice.gov.uk'),

       ('ee20f564-4853-40b2-bae4-65dd7e0207fa', '28e47d30-30bf-4dab-a8eb-9fda3f6400e8', 'MDI', 'nobody-mdi@digital.justice.gov.uk'),

       ('b328ebc8-1f7b-4236-b4ac-30f50b43a92d', '1811faa6-d568-4fc4-83ce-41118b90242e', 'BWN', 'nobody-bwn@digital.justice.gov.uk');

-- Add link table values

INSERT INTO course_audience (course_id, audience_id)
VALUES ('28e47d30-30bf-4dab-a8eb-9fda3f6400e8', '7fffcc6a-11f8-4713-be35-cf5ff1aee517');
