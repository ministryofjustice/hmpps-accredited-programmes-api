DELETE FROM referral_status_transitions where transition_to_status = 'MOVE_TO_BUILDING_CHOICES';

UPDATE referral_status SET code = 'MOVED_TO_BUILDING_CHOICES' where code = 'MOVE_TO_BUILDING_CHOICES';

insert into referral_status_transitions values ('3a57c79a-192c-4654-9a22-04a3d2317db4', true, false, 'REFERRAL_SUBMITTED','MOVED_TO_BUILDING_CHOICES', null, null, null, null, null, null, null);
insert into referral_status_transitions values ('b26ed41e-b505-4e8f-9273-32cf12f70198', true, false, 'ON_HOLD_REFERRAL_SUBMITTED','MOVED_TO_BUILDING_CHOICES', null, null, null, null, null, null, null);
insert into referral_status_transitions values ('0e8ff064-9e2c-487a-a621-cce273dc229f', true, false, 'AWAITING_ASSESSMENT','MOVED_TO_BUILDING_CHOICES', null, null, null, null, null, null, null);
insert into referral_status_transitions values ('79aa0127-8e6e-4ab5-bff9-9fe6b9db4ab0', true, false, 'ON_HOLD_AWAITING_ASSESSMENT','MOVED_TO_BUILDING_CHOICES', null, null, null, null, null, null, null);
insert into referral_status_transitions values ('f0bb9e08-2879-4a91-8271-b210e8ead61b', true, false, 'ASSESSMENT_STARTED','MOVED_TO_BUILDING_CHOICES', null, null, null, null, null, null, null);
insert into referral_status_transitions values ('ecbecf35-e047-416e-8de6-8110156fe2a6', true, false, 'ON_HOLD_ASSESSMENT_STARTED','MOVED_TO_BUILDING_CHOICES', null, null, null, null, null, null, null);
insert into referral_status_transitions values ('e5a18c7b-7524-4780-ba27-ee37f739cccb', true, false, 'ASSESSED_SUITABLE','MOVED_TO_BUILDING_CHOICES', null, null, null, null, null, null, null);
insert into referral_status_transitions values ('e19efb89-65a1-4e29-8e5f-370d20cf8501', true, false, 'SUITABLE_NOT_READY','MOVED_TO_BUILDING_CHOICES', null, null, null, null, null, null, null);
