

-- course_id='f3111abe-c016-46e3-b01c-dcbf7d6ed360'
update COURSE set name='Becoming New Me Plus: general violence offence', description = 'Becoming New Me Plus (BNM+) uses cognitive behavioural therapy. It is for men who are at high or very high risk of reoffending. This strand is for men convicted of a general violence offence.' where course_id='f3111abe-c016-46e3-b01c-dcbf7d6ed360';

DELETE FROM prerequisite where course_id='f3111abe-c016-46e3-b01c-dcbf7d6ed360' and name = 'Risk criteria';

INSERT INTO prerequisite(course_id, name, description)
VALUES ('f3111abe-c016-46e3-b01c-dcbf7d6ed360', 'Suitable for people with learning disabilities or challenges (LDC)?', 'Yes'),
       ('f3111abe-c016-46e3-b01c-dcbf7d6ed360', 'Equivalent non-LDC programme', 'Kaizen'),
       ('f3111abe-c016-46e3-b01c-dcbf7d6ed360', 'Risk criteria', 'High OVP (OASys Violence Predictor)'),
       ('f3111abe-c016-46e3-b01c-dcbf7d6ed360', 'Risk criteria', 'High OGRS (Offender Group Reconviction Scale)'),
       ('f3111abe-c016-46e3-b01c-dcbf7d6ed360', 'Time to complete', 'At least 12 months');

------------------------------------------------------------------------------------------------------------------------
-- course_id='bf5bc47b-a0c6-4256-8d4b-133e488ca70a'
update COURSE set name='Becoming New Me Plus: intimate partner violence offence', description = 'Becoming New Me Plus (BNM+) uses cognitive behavioural therapy. It is for men who are at high or very high risk of reoffending. This strand is for men convicted of an intimate partner violence offence.' where course_id='bf5bc47b-a0c6-4256-8d4b-133e488ca70a';

DELETE FROM prerequisite where course_id='bf5bc47b-a0c6-4256-8d4b-133e488ca70a' and name = 'Risk criteria';

    INSERT INTO prerequisite(course_id, name, description)
VALUES ('bf5bc47b-a0c6-4256-8d4b-133e488ca70a', 'Suitable for people with learning disabilities or challenges (LDC)?', 'Yes'),
    ('bf5bc47b-a0c6-4256-8d4b-133e488ca70a', 'Equivalent non-LDC programme', 'Kaizen'),
    ('bf5bc47b-a0c6-4256-8d4b-133e488ca70a', 'Risk criteria', 'High SARA (Spousal Assault Risk Assessment)'),
    ('bf5bc47b-a0c6-4256-8d4b-133e488ca70a', 'Time to complete', 'At least 12 months');

------------------------------------------------------------------------------------------------------------------------
-- course_id='fc51527c-8cf4-4c41-ae37-24db86b46040'
update COURSE set name='Becoming New Me Plus: sexual offence', description = 'Becoming New Me Plus (BNM+) uses cognitive behavioural therapy. It is for men who are at high or very high risk of reoffending. This strand is for men convicted of a sexual offence.' where course_id='fc51527c-8cf4-4c41-ae37-24db86b46040';

DELETE FROM prerequisite where course_id='fc51527c-8cf4-4c41-ae37-24db86b46040' and name = 'Risk criteria';

INSERT INTO prerequisite(course_id, name, description)
VALUES ('fc51527c-8cf4-4c41-ae37-24db86b46040', 'Suitable for people with learning disabilities or challenges (LDC)?', 'Yes'),
       ('fc51527c-8cf4-4c41-ae37-24db86b46040', 'Equivalent non-LDC programme', 'Kaizen'),
       ('fc51527c-8cf4-4c41-ae37-24db86b46040', 'Risk criteria', 'High or very high OSP (OASys Sexual Reconviction Predictor)'),
       ('fc51527c-8cf4-4c41-ae37-24db86b46040', 'Time to complete', 'At least 12 months');

------------------------------------------------------------------------------------------------------------------------
-- course_id='86d29120-d249-4b0b-a9cb-4e8f723eae77'
update COURSE set name='Building Better Relationships', description = 'Building Better Relationships (BBR) uses cognitive behavioural therapy. It is for men convicted of an intimate partner violence-related (IPV) offence against a female partner.' where course_id='86d29120-d249-4b0b-a9cb-4e8f723eae77';

update prerequisite set description='Medium SARA (Spousal Assault Risk Assessment) in custody' where course_id='86d29120-d249-4b0b-a9cb-4e8f723eae77' and name = 'Risk criteria'  and description='Medium SARA';
update prerequisite set description='Medium or high SARA (Spousal Assault Risk Assessment) in the community' where course_id='86d29120-d249-4b0b-a9cb-4e8f723eae77' and name = 'Risk criteria'  and description='Medium/High SARA';

INSERT INTO prerequisite(course_id, name, description)
VALUES ('86d29120-d249-4b0b-a9cb-4e8f723eae77', 'Suitable for people with learning disabilities or challenges (LDC)?', 'Yes'),
       ('86d29120-d249-4b0b-a9cb-4e8f723eae77', 'Time to complete', 'At least 6 months'),
       ('86d29120-d249-4b0b-a9cb-4e8f723eae77', 'Setting', 'Community');

------------------------------------------------------------------------------------------------------------------------
-- course_id='108b3e16-04d0-48a0-beb9-886f2e6d47d9'

update COURSE set name='Healthy Identity Intervention', description = 'Healthy Identity Intervention (HII) targets the social and psychological drivers of extremist offending. It helps participants disengage from extremist groups, causes, or ideologies and to reconnect with their own personal values and beliefs.' where course_id='108b3e16-04d0-48a0-beb9-886f2e6d47d9';

update prerequisite set description='22+ ERG (Extremism Risk Guidance) and an assessment' where course_id='108b3e16-04d0-48a0-beb9-886f2e6d47d9' and name='Risk criteria' and description='ERG22+';

update prerequisite set description='Custody or community' where course_id='108b3e16-04d0-48a0-beb9-886f2e6d47d9' and name='Setting' and description='Community';
DELETE FROM prerequisite where course_id='108b3e16-04d0-48a0-beb9-886f2e6d47d9' and name='Setting' and description='Custody';

update prerequisite set description='Male or female' where course_id='108b3e16-04d0-48a0-beb9-886f2e6d47d9' and name='Gender' and description='Male';
DELETE FROM prerequisite where course_id='108b3e16-04d0-48a0-beb9-886f2e6d47d9' and name='Gender' and description='Female';

INSERT INTO prerequisite(course_id, name, description)
VALUES ('108b3e16-04d0-48a0-beb9-886f2e6d47d9', 'Suitable for people with learning disabilities or challenges (LDC)?', 'Yes, but reviewed case by case'),
       ('108b3e16-04d0-48a0-beb9-886f2e6d47d9', 'Time to complete', '10 to 32 sessions (at least 3 months)');

------------------------------------------------------------------------------------------------------------------------
-- course_id='8bf2fabf-8667-4858-bc91-a2bd93f41f48'

update COURSE set description = 'Healthy Sex Programme (HSP) is a follow-up programme for men who have completed Kaizen, Becoming New Me Plus, Horizon, or New Me Strengths. It uses cognitive behavioural therapy and is aimed at men convicted of a sexual offence, or an offence with a sexual element.' where course_id='8bf2fabf-8667-4858-bc91-a2bd93f41f48';

update prerequisite set description='Medium, high or very high OSP (OASys Sexual Reconviction Predictor)' where course_id='8bf2fabf-8667-4858-bc91-a2bd93f41f48' and name = 'Risk criteria'  and description='Medium or above OSP';

INSERT INTO prerequisite(course_id, name, description)
VALUES ('8bf2fabf-8667-4858-bc91-a2bd93f41f48', 'Suitable for people with learning disabilities or challenges (LDC)?', 'Yes');

------------------------------------------------------------------------------------------------------------------------
-- course_id='34eafef2-9ba5-4428-8d4e-085d95298144'

update COURSE set description = 'Horizon is for men convicted of a sexual or sexually-motivated offence who are medium risk or above. It helps address problematic factors and how they contribute to behaviour.' where course_id='34eafef2-9ba5-4428-8d4e-085d95298144';

update prerequisite set description='Medium, high or very high OSP (OASys Sexual Reconviction Predictor)' where course_id='34eafef2-9ba5-4428-8d4e-085d95298144' and name = 'Risk criteria'  and description='Medium OSP';

update prerequisite set description='Custody or community' where course_id='34eafef2-9ba5-4428-8d4e-085d95298144' and name='Setting' and description='Custody';
INSERT INTO prerequisite(course_id, name, description)
VALUES ('34eafef2-9ba5-4428-8d4e-085d95298144', 'Suitable for people with learning disabilities or challenges (LDC)?', 'No'),
       ('34eafef2-9ba5-4428-8d4e-085d952981440', 'Equivalent LDC programme', 'Becoming New Me Plus'),
       ('34eafef2-9ba5-4428-8d4e-085d95298144', 'Time to complete', 'At least 6 months (custody); at least 12 months (community)');

------------------------------------------------------------------------------------------------------------------------
-- course_id='6e700027-72b3-44f3-8230-31209f84cd91' TO BE DELETED

DELETE FROM referral where offering_id IN (select offering_id from offering where course_id='6e700027-72b3-44f3-8230-31209f84cd91');
DELETE FROM prerequisite WHERE course_id = '6e700027-72b3-44f3-8230-31209f84cd91';
DELETE FROM offering where course_id = '6e700027-72b3-44f3-8230-31209f84cd91';
DELETE FROM offering_old where course_id = '6e700027-72b3-44f3-8230-31209f84cd91';
DELETE FROM course_participation where course_id = '6e700027-72b3-44f3-8230-31209f84cd91';

------------------------------------------------------------------------------------------------------------------------
-- course_id='c6a0e8a0-4a52-4aa1-ac39-3f430cfa065b'

update COURSE set audience = 'Gang-related offence' where course_id='c6a0e8a0-4a52-4aa1-ac39-3f430cfa065b';
update COURSE set description = 'Identity Matters (IM) is for men whose offending was motivated or enabled by their affiliation to a particular group. It aims to change the nature of their relationship to the group and helps them develop skills to resist peer influence.' where course_id='c6a0e8a0-4a52-4aa1-ac39-3f430cfa065b';

update prerequisite set description='50+ OGRS (Offender Group Reconviction Scale)' where course_id='c6a0e8a0-4a52-4aa1-ac39-3f430cfa065b' and name = 'Risk criteria'  and description='50+ OGRS';
update prerequisite set description='Medium or high ROSH (Risk of Serious Harm)' where course_id='c6a0e8a0-4a52-4aa1-ac39-3f430cfa065b' and name = 'Risk criteria'  and description='Medium/High ROSH';

INSERT INTO prerequisite(course_id, name, description)
VALUES ('c6a0e8a0-4a52-4aa1-ac39-3f430cfa065b', 'Suitable for people with learning disabilities or challenges (LDC)?', 'Assessed case by case'),
       ('c6a0e8a0-4a52-4aa1-ac39-3f430cfa065b', 'Time to complete', 'Flexible delivery with up to 23 sessions plus pre-programme session and programme review. Around 25 hours (at least 3 months).');

------------------------------------------------------------------------------------------------------------------------
-- course_id='9cc3e831-f399-4980-826d-12f01c6cab0f'

update COURSE set name='Kaizen: general violence offence', description = 'Kaizen uses a unified approach, focusing on the needs of participants. It is inclusive and effective for a range of responsivity needs, including difficulties with engagement, therapeutic relationships and denial. This strand is for men convicted of a general violence offence.' where course_id='9cc3e831-f399-4980-826d-12f01c6cab0f';

update prerequisite set description='High OVP (OASys Violence Predictor)' where course_id='9cc3e831-f399-4980-826d-12f01c6cab0f' and name = 'Risk criteria'  and description='High OVP';

INSERT INTO prerequisite(course_id, name, description)
VALUES ('9cc3e831-f399-4980-826d-12f01c6cab0f', 'Suitable for people with learning disabilities or challenges (LDC)?', 'No'),
       ('9cc3e831-f399-4980-826d-12f01c6cab0f', 'Equivalent LDC programme', 'Becoming New Me Plus'),
       ('9cc3e831-f399-4980-826d-12f01c6cab0f', 'Time to complete', 'At least 6 months');

------------------------------------------------------------------------------------------------------------------------
-- course_id='5441bcbe-8fcb-4f5b-90cb-74be0e7f32ff'

update COURSE set name='Kaizen: intimate partner violence offence', description = 'Kaizen uses a unified approach, focusing on the needs of participants. It is inclusive and effective for a range of responsivity needs, including difficulties with engagement, therapeutic relationships and denial. This strand is for men convicted of an intimate partner violence offence.' where course_id='5441bcbe-8fcb-4f5b-90cb-74be0e7f32ff';

update prerequisite set description='High SARA (Spousal Assault Risk Assessment)' where course_id='5441bcbe-8fcb-4f5b-90cb-74be0e7f32ff' and name = 'Risk criteria'  and description='High ESARA / SARH';

INSERT INTO prerequisite(course_id, name, description)
VALUES ('5441bcbe-8fcb-4f5b-90cb-74be0e7f32ff', 'Suitable for people with learning disabilities or challenges (LDC)?', 'No'),
       ('5441bcbe-8fcb-4f5b-90cb-74be0e7f32ff', 'Equivalent LDC programme', 'Becoming New Me Plus'),
       ('5441bcbe-8fcb-4f5b-90cb-74be0e7f32ff', 'Time to complete', 'At least 6 months');

------------------------------------------------------------------------------------------------------------------------
-- course_id='3ff72871-5708-4af0-a6f8-53d2e40e2b6a'

update COURSE set name='Kaizen: sexual offence', description = 'Kaizen uses a unified approach, focusing on the needs of participants rather than offence types. It is inclusive and effective for a range of responsivity needs, including difficulties with engagement, therapeutic relationships and denial. This strand is for men convicted of a sexual offence.' where course_id='3ff72871-5708-4af0-a6f8-53d2e40e2b6a';

update prerequisite set description='High or very high OSP (OASys Sexual Reconviction Predictor)' where course_id='3ff72871-5708-4af0-a6f8-53d2e40e2b6a' and name = 'Risk criteria'  and description='High or Very High OSP';

INSERT INTO prerequisite(course_id, name, description)
VALUES ('3ff72871-5708-4af0-a6f8-53d2e40e2b6a', 'Suitable for people with learning disabilities or challenges (LDC)?', 'No'),
       ('3ff72871-5708-4af0-a6f8-53d2e40e2b6a', 'Equivalent LDC programme', 'Becoming New Me Plus'),
       ('3ff72871-5708-4af0-a6f8-53d2e40e2b6a', 'Risk criteria', 'High SARA (Spousal Assault Risk Assessment)'),
       ('3ff72871-5708-4af0-a6f8-53d2e40e2b6a', 'Time to complete', 'At least 6 months');

------------------------------------------------------------------------------------------------------------------------
-- course_id='12569f25-23b7-43f2-899b-561b98007d31'

update COURSE set name='Living as New Me', audience='All offences', description = 'Living as New Me (LNM) is a booster or maintenance programme for men who have completed New Me Strengths or Becoming New Me Plus and may need extra support. In a custody setting, it is for men convicted of any offence. In the community, it is for sexual offences only.' where course_id='12569f25-23b7-43f2-899b-561b98007d31';

update prerequisite set description='Medium or above risk' where course_id='12569f25-23b7-43f2-899b-561b98007d31' and name = 'Risk criteria'  and description='Medium';
update prerequisite set description='Custody or community' where course_id='12569f25-23b7-43f2-899b-561b98007d31' and name = 'Setting'  and description='Custody';

INSERT INTO prerequisite(course_id, name, description)
VALUES ('12569f25-23b7-43f2-899b-561b98007d31', 'Suitable for people with learning disabilities or challenges (LDC)?', 'Yes'),
       ('12569f25-23b7-43f2-899b-561b98007d31', 'Time to complete', 'Rolling programme with at least 5 sessions, often more');


------------------------------------------------------------------------------------------------------------------------
-- course_id='53454a69-769a-4864-bf25-16a268816f98'

update COURSE set name='New Me Strengths: general violence offence', description = 'New Me Strengths (NMS) is a cognitive-behavioural programme for men at medium risk of reconviction. In a custody setting, it is for men convicted of any offence. In the community, it is for sexual offences only. It helps people recognise the impact and consequences of offending and avoid activities that encourage offending behaviours.' where course_id='53454a69-769a-4864-bf25-16a268816f98';

INSERT INTO prerequisite(course_id, name, description)
VALUES  ('53454a69-769a-4864-bf25-16a268816f98', 'Setting', 'Custody or community'),
        ('53454a69-769a-4864-bf25-16a268816f98', 'Gender', 'Male'),
        ('53454a69-769a-4864-bf25-16a268816f98', 'Risk criteria', 'Medium OVP (OASys Violence Predictor)'),
        ('53454a69-769a-4864-bf25-16a268816f98', 'Risk criteria', '50+ OGRS (Offender Group Reconviction Scale)'),
        ('53454a69-769a-4864-bf25-16a268816f98', 'Suitable for people with learning disabilities or challenges (LDC)?', 'Yes'),
        ('53454a69-769a-4864-bf25-16a268816f98', 'Equivalent non-LDC programmes', 'Building Better Relationships, Horizon, Thinking Skills Programme'),
        ('53454a69-769a-4864-bf25-16a268816f98', 'Time to complete', 'At least 6 months');


------------------------------------------------------------------------------------------------------------------------
-- course_id='de444c21-4106-4af5-af68-6a45d74ed605'
update COURSE set name='New Me Strengths: intimate partner violence offence', description = 'New Me Strengths (NMS) is a cognitive-behavioural programme for men at medium risk of reconviction. In a custody setting, it is for men convicted of any offence. In the community, it is for sexual offences only. It helps people recognise the impact and consequences of offending and avoid activities that encourage offending behaviours.' where course_id='de444c21-4106-4af5-af68-6a45d74ed605';

INSERT INTO prerequisite(course_id, name, description)
VALUES  ('de444c21-4106-4af5-af68-6a45d74ed605', 'Setting', 'Custody or community'),
        ('de444c21-4106-4af5-af68-6a45d74ed605', 'Gender', 'Male'),
        ('de444c21-4106-4af5-af68-6a45d74ed605', 'Risk criteria', 'Medium SARA (Spousal Assault Risk Assessment)'),
        ('de444c21-4106-4af5-af68-6a45d74ed605', 'Suitable for people with learning disabilities or challenges (LDC)?', 'Yes'),
        ('de444c21-4106-4af5-af68-6a45d74ed605', 'Equivalent non-LDC programmes', 'Building Better Relationships, Horizon, Thinking Skills Programme'),
        ('de444c21-4106-4af5-af68-6a45d74ed605', 'Time to complete', 'At least 6 months');

------------------------------------------------------------------------------------------------------------------------
-- course_id='30a8c3ca-f44d-4b92-b4f2-1dddd0c41434'

update COURSE set name='New Me Strengths: sexual offence', description = 'New Me Strengths (NMS) is a cognitive-behavioural programme for men at medium risk of reconviction. In a custody setting, it is for men convicted of any offence. In the community, it is for sexual offences only. It helps people recognise the impact and consequences of offending and avoid activities that encourage offending behaviours.' where course_id='30a8c3ca-f44d-4b92-b4f2-1dddd0c41434';

INSERT INTO prerequisite(course_id, name, description)
VALUES  ('30a8c3ca-f44d-4b92-b4f2-1dddd0c41434', 'Setting', 'Custody or community'),
        ('30a8c3ca-f44d-4b92-b4f2-1dddd0c41434', 'Gender', 'Male'),
        ('30a8c3ca-f44d-4b92-b4f2-1dddd0c41434', 'Risk criteria', 'Medium OSP (OASys Sexual Reconviction Predictor)'),
        ('30a8c3ca-f44d-4b92-b4f2-1dddd0c41434', 'Suitable for people with learning disabilities or challenges (LDC)?', 'Yes'),
        ('30a8c3ca-f44d-4b92-b4f2-1dddd0c41434', 'Equivalent non-LDC programmes', 'Building Better Relationships, Horizon, Thinking Skills Programme'),
        ('30a8c3ca-f44d-4b92-b4f2-1dddd0c41434', 'Time to complete', 'At least 6 months');

------------------------------------------------------------------------------------------------------------------------
-- course_id='e6bd7fed-829e-469d-8d96-1d7af6e55225'

update COURSE set name='Thinking Skills Programme', description = 'Thinking Skills Programme (TSP helps men and women to develop skills in pro-social problem solving, perspective taking, developing and managing relationships, and self-management.', audience='All offences' where course_id='e6bd7fed-829e-469d-8d96-1d7af6e55225';

update prerequisite set description='Custody or community' where course_id='e6bd7fed-829e-469d-8d96-1d7af6e55225' and name='Setting' and description='Community';
DELETE FROM prerequisite where course_id='e6bd7fed-829e-469d-8d96-1d7af6e55225' and name='Setting' and description='Custody';

update prerequisite set description='Male or female' where course_id='e6bd7fed-829e-469d-8d96-1d7af6e55225' and name='Gender' and description='Male';
DELETE FROM prerequisite where course_id='e6bd7fed-829e-469d-8d96-1d7af6e55225' and name='Gender' and description='Female';

update prerequisite set description='50+ OGRS3 (Offender Group Reconviction Scale 3) over 2 years' where course_id='e6bd7fed-829e-469d-8d96-1d7af6e55225' and name='Risk criteria' and description='50+ on OGRS3 (over 2 years)';

INSERT INTO prerequisite(course_id, name, description)
VALUES  ('e6bd7fed-829e-469d-8d96-1d7af6e55225', 'Suitable for all offences', 'for sexual offences, consider Horizon or Kaizen first.'),
        ('e6bd7fed-829e-469d-8d96-1d7af6e55225', 'Suitable for people with learning disabilities or challenges (LDC)?', 'No'),
        ('e6bd7fed-829e-469d-8d96-1d7af6e55225', 'Equivalent LDC programme', 'New Me Strengths'),
        ('e6bd7fed-829e-469d-8d96-1d7af6e55225', 'Time to complete', 'At least 6 months');


------------------------------------------------------------------------------------------------------------------------
-- course_id='bee96044-45fb-4ba4-93a4-19e5f71c8f15'

update COURSE set name='New Me MOT', withdrawn=false, description = 'New Me MOT (NMMOT) is not an Accredited Programme, but is for men who have already completed Becoming New Me Plus, Kaizen, Healthy Sex Programme, Horizon, iHorizon, New Me Strengths, the Cognitive Self-Change Programme or the Self-Change Programme. It offers support throughout a person’s sentence, set out as a series of short exercises. These are completed with support from the person’s Prison Offender Manager in custody or their Probation Practitioner in the community.This gives them  the chance to practise skills in different contexts, generalising and maintaining their learning from the Accredited Programme.', audience='All offences' where course_id='bee96044-45fb-4ba4-93a4-19e5f71c8f15';

INSERT INTO prerequisite(course_id, name, description)
VALUES ('bee96044-45fb-4ba4-93a4-19e5f71c8f15', 'Setting', 'Custody or community'),
       ('bee96044-45fb-4ba4-93a4-19e5f71c8f15', 'Gender', 'Male'),
       ('bee96044-45fb-4ba4-93a4-19e5f71c8f15', 'Risk criteria', 'All risk scores'),
       ('bee96044-45fb-4ba4-93a4-19e5f71c8f15', 'Suitable for people with learning disabilities or challenges (LDC)?', 'Yes'),
       ('bee96044-45fb-4ba4-93a4-19e5f71c8f15', 'Time to complete', 'Number of sessions is at the discretion of the practitioner and depends on the risk, need and responsivity profile of the individual. Has a 30-day Rehabilitation Activity Requirement (RAR) in the community.');



