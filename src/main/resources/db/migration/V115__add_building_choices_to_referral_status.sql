INSERT INTO referral_status (code, description,colour,active, draft, closed, has_notes, has_confirmation, hint_text, confirmation_text, hold, release, default_order, notes_optional, case_notes_subtype, case_notes_message)
VALUES ('MOVE_TO_BUILDING_CHOICES','Moved to building choices','light-grey', true, false, false, true, false, null, null, false, false, 160, true,  'MVD_BLDG', 'The referral for PRISONER_NAME has been moved from PGM_NAME_STRAND to BC_STRAND.');

insert into referral_status_transitions values ('3a57c79a-192c-4654-9a22-04a3d2317db4', true, false, 'REFERRAL_SUBMITTED','MOVE_TO_BUILDING_CHOICES', null, null, null, null, null, null, null);
insert into referral_status_transitions values ('b26ed41e-b505-4e8f-9273-32cf12f70198', true, false, 'ON_HOLD_REFERRAL_SUBMITTED','MOVE_TO_BUILDING_CHOICES', null, null, null, null, null, null, null);
insert into referral_status_transitions values ('0e8ff064-9e2c-487a-a621-cce273dc229f', true, false, 'AWAITING_ASSESSMENT','MOVE_TO_BUILDING_CHOICES', null, null, null, null, null, null, null);
insert into referral_status_transitions values ('79aa0127-8e6e-4ab5-bff9-9fe6b9db4ab0', true, false, 'ON_HOLD_AWAITING_ASSESSMENT','MOVE_TO_BUILDING_CHOICES', null, null, null, null, null, null, null);
insert into referral_status_transitions values ('f0bb9e08-2879-4a91-8271-b210e8ead61b', true, false, 'ASSESSMENT_STARTED','MOVE_TO_BUILDING_CHOICES', null, null, null, null, null, null, null);
insert into referral_status_transitions values ('ecbecf35-e047-416e-8de6-8110156fe2a6', true, false, 'ON_HOLD_ASSESSMENT_STARTED','MOVE_TO_BUILDING_CHOICES', null, null, null, null, null, null, null);
insert into referral_status_transitions values ('e5a18c7b-7524-4780-ba27-ee37f739cccb', true, false, 'ASSESSED_SUITABLE','MOVE_TO_BUILDING_CHOICES', null, null, null, null, null, null, null);
insert into referral_status_transitions values ('e19efb89-65a1-4e29-8e5f-370d20cf8501', true, false, 'SUITABLE_NOT_READY','MOVE_TO_BUILDING_CHOICES', null, null, null, null, null, null, null);


