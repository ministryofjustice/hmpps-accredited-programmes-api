insert into referral_status_transitions values ('7cca5375-df34-443d-baa8-f517b2ba0d5f', true, true, 'REFERRAL_STARTED','REFERRAL_SUBMITTED');

--PT transitions
insert into referral_status_transitions values ('29a74834-8a81-4f99-bb0b-7d2f66723b56', true, false, 'REFERRAL_SUBMITTED','AWAITING_ASSESSMENT');
insert into referral_status_transitions values ('8a6cc070-f076-49d8-b5d9-9fba748f6bae', true, false, 'REFERRAL_SUBMITTED','NOT_ELIGIBLE');
insert into referral_status_transitions values ('5ab83de8-0102-4279-93c2-f926aeb716fa', true, false, 'REFERRAL_SUBMITTED','ON_HOLD_REFERRAL_SUBMITTED');
insert into referral_status_transitions values ('1352df62-d7bb-40ef-92d7-654cdaa12081', true, false, 'REFERRAL_SUBMITTED','WITHDRAWN');

insert into referral_status_transitions values ('e2b303eb-9ac8-4e10-9cea-0ba90450e3e0', true, false, 'AWAITING_ASSESSMENT','ASSESSMENT_STARTED');
insert into referral_status_transitions values ('b1c6291e-17ad-469a-ab77-12f361aef0b6', true, false, 'AWAITING_ASSESSMENT','NOT_ELIGIBLE');
insert into referral_status_transitions values ('e6dc8e4b-2644-4504-a072-acc9f3a58c85', true, false, 'AWAITING_ASSESSMENT','ON_HOLD_AWAITING_ASSESSMENT');
insert into referral_status_transitions values ('b510c888-c906-4ff7-82d4-a43c990a8a55', true, false, 'AWAITING_ASSESSMENT','WITHDRAWN');

insert into referral_status_transitions values ('aec54f43-2d8c-4a04-9eaa-5410da266f56', true, false, 'ASSESSMENT_STARTED','ASSESSED_SUITABLE');
insert into referral_status_transitions values ('038e0761-347c-4936-b0c6-95fc672645c2', true, false, 'ASSESSMENT_STARTED','SUITABLE_NOT_READY');
insert into referral_status_transitions values ('7d04fcc8-946d-4945-9a43-43f590f5f5b4', true, false, 'ASSESSMENT_STARTED','NOT_SUITABLE');
insert into referral_status_transitions values ('2414ad6e-47b2-4579-a144-d39d856a9d81', true, false, 'ASSESSMENT_STARTED','ON_HOLD_ASSESSMENT_STARTED');
insert into referral_status_transitions values ('eafe7046-3921-461b-86fb-04bcc937fd45', true, false, 'ASSESSMENT_STARTED','WITHDRAWN');

insert into referral_status_transitions values ('4a730684-2262-4f60-bb61-8e3251be828e', true, false, 'ASSESSED_SUITABLE','ON_PROGRAMME');
insert into referral_status_transitions values ('6ff18bb3-0a2a-44e1-afcc-8c62ced3fc36', true, false, 'ASSESSED_SUITABLE','SUITABLE_NOT_READY');
insert into referral_status_transitions values ('fd475c09-ab1f-4cfa-97d0-1bb2fe6e5318', true, false, 'ASSESSED_SUITABLE','WITHDRAWN');

insert into referral_status_transitions values ('92aa0c54-4450-41fc-886a-9da6fbfaed90', true, false, 'SUITABLE_NOT_READY','ASSESSED_SUITABLE');
insert into referral_status_transitions values ('c1afa295-0a69-494f-925e-b1971de660dd', true, false, 'SUITABLE_NOT_READY','WITHDRAWN');

insert into referral_status_transitions values ('e8263d79-2c84-426e-a374-2395997e884d', true, false, 'ON_PROGRAMME','PROGRAMME_COMPLETE');
insert into referral_status_transitions values ('7fae085c-73d3-480e-a418-7bc91f6a881e', true, false, 'ON_PROGRAMME','DESELECTED');
insert into referral_status_transitions values ('ab9406a3-41f9-4386-8074-544bbdda7bdf', true, false, 'ON_PROGRAMME','ASSESSED_SUITABLE');
insert into referral_status_transitions values ('e7a3aa5c-7e4d-4053-8fe9-c30515717af7', true, false, 'ON_PROGRAMME','SUITABLE_NOT_READY');

insert into referral_status_transitions values ('770b978f-b1e6-4885-8b0f-8612fb22ae62', true, false, 'ON_HOLD_REFERRAL_SUBMITTED','REFERRAL_SUBMITTED');
insert into referral_status_transitions values ('2485ad6d-b2b6-4055-9e7b-6beb1879e80a', true, false, 'ON_HOLD_REFERRAL_SUBMITTED','WITHDRAWN');

insert into referral_status_transitions values ('ac4d01a8-6c37-4a2f-95e1-213d895c3146', true, false, 'ON_HOLD_AWAITING_ASSESSMENT','AWAITING_ASSESSMENT');
insert into referral_status_transitions values ('c4e20256-9508-41ff-899c-5aad527860c0', true, false, 'ON_HOLD_AWAITING_ASSESSMENT','WITHDRAWN');

insert into referral_status_transitions values ('a7ccf929-d91a-4dfc-b536-f42b0815aa49', true, false, 'ON_HOLD_ASSESSMENT_STARTED','ASSESSMENT_STARTED');
insert into referral_status_transitions values ('2b98eefb-7950-43ea-9800-bd5622bbb4fa', true, false, 'ON_HOLD_ASSESSMENT_STARTED','WITHDRAWN');

--POM transitions

insert into referral_status_transitions values ('5026ce4c-acf6-41c3-80b7-9eeaaa1c966d', false, true, 'REFERRAL_SUBMITTED','ON_HOLD_REFERRAL_SUBMITTED');
insert into referral_status_transitions values ('18f4de44-3f8f-47ea-80d0-a3af02c97f86', false, true, 'REFERRAL_SUBMITTED','WITHDRAWN');

insert into referral_status_transitions values ('0774e02f-ba90-4819-a854-847319b6e1be', false, true, 'AWAITING_ASSESSMENT','ON_HOLD_AWAITING_ASSESSMENT');
insert into referral_status_transitions values ('bd0d6d96-bc82-4e15-bbd6-07fc5a0c28bd', false, true, 'AWAITING_ASSESSMENT','WITHDRAWN');

insert into referral_status_transitions values ('cd320dfb-1860-4ca7-b82c-9fba2a682a2d', false, true, 'ASSESSMENT_STARTED','ON_HOLD_ASSESSMENT_STARTED');
insert into referral_status_transitions values ('bc896255-655d-4720-bfac-5bd24c9c3f8e', false, true, 'ASSESSMENT_STARTED','WITHDRAWN');

insert into referral_status_transitions values ('54911a5b-6de5-4d3a-8168-11d4c5a6e585', false, true, 'ASSESSED_SUITABLE','WITHDRAWN');

insert into referral_status_transitions values ('5865dd8a-6c04-40a9-94dc-55930156bad2', false, true, 'SUITABLE_NOT_READY','WITHDRAWN');

insert into referral_status_transitions values ('6b86eb83-a100-431a-a24c-f9c6c63d633f', false, true, 'ON_HOLD_REFERRAL_SUBMITTED','REFERRAL_SUBMITTED');
insert into referral_status_transitions values ('1313fc0c-1351-4caf-a4c1-ff05588ea642', false, true, 'ON_HOLD_REFERRAL_SUBMITTED','WITHDRAWN');

insert into referral_status_transitions values ('e030adcb-03a1-446b-a8cf-c57c70574e0c', false, true, 'ON_HOLD_AWAITING_ASSESSMENT','AWAITING_ASSESSMENT');
insert into referral_status_transitions values ('aa2ae2c2-c499-4107-b6b5-d9379e5427a4', false, true, 'ON_HOLD_AWAITING_ASSESSMENT','WITHDRAWN');

insert into referral_status_transitions values ('7e5a4eff-472d-4b86-9484-ba78f7f23761', false, true, 'ON_HOLD_ASSESSMENT_STARTED','ASSESSMENT_STARTED');
insert into referral_status_transitions values ('6a066fb7-dd3a-4e6a-a6d0-3f4a82b5a62f', false, true, 'ON_HOLD_ASSESSMENT_STARTED','WITHDRAWN');