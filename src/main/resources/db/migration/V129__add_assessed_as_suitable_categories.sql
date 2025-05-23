insert into referral_status_category values ('AS_RISK', 'ASSESSED_SUITABLE', 'Risk and need', true);
insert into referral_status_category values ('AS_INCOMPLETE', 'ASSESSED_SUITABLE', 'Incomplete assessment', true);
insert into referral_status_category values ('AS_SENTENCE', 'ASSESSED_SUITABLE', 'Sentence type', true);
insert into referral_status_category values ('AS_OPERATIONAL', 'ASSESSED_SUITABLE', 'Operational', true);

insert into referral_status_reason (code, referral_status_category_code, description, active, deselect_open)
values ('AS_SEXUAL_KILLING', 'AS_RISK', 'The person is low risk but has been convicted of a sexual killing', true, true);
insert into referral_status_reason (code, referral_status_category_code, description, active, deselect_open)
values ('AS_EXTREMIST_OFFENDING', 'AS_RISK', 'The person is low risk but has been convicted of extremist offending, for example terrorism', true, true);
insert into referral_status_reason (code, referral_status_category_code, description, active, deselect_open)
values ('AS_REOFFENDING_RISK', 'AS_RISK', 'The person''s psychological risk assessment shows high risk of reoffending', true, true);
insert into referral_status_reason (code, referral_status_category_code, description, active, deselect_open)
values ('AS_NEED_HIGH_INTENSITY', 'AS_RISK', 'The person is substantially different to others on the moderate intensity pathway. They need a high intensity programme', true, true);
insert into referral_status_reason (code, referral_status_category_code, description, active, deselect_open)
values ('AS_NEED_MODERATE_INTENSITY', 'AS_RISK', 'The person is substantially different to others on the high intensity pathway. They need a moderate intensity programme', true, true);

insert into referral_status_reason (code, referral_status_category_code, description, active, deselect_open)
values ('AS_MISSING_INFORMATION', 'AS_INCOMPLETE', 'Information is missing from the risk and need assessment but the person''s risks and needs do match the recommendations for the programme', true, true);
insert into referral_status_reason (code, referral_status_category_code, description, active, deselect_open)
values ('AS_OUTDATED', 'AS_INCOMPLETE', 'The risk and need assessment is outdated', true, true);

insert into referral_status_reason (code, referral_status_category_code, description, active, deselect_open)
values ('AS_HIGH_ROSH', 'AS_SENTENCE', 'The person has an Indefinite Sentence for the Public Protection and high ROSH (Risk of Serious Harm)', true, true);

insert into referral_status_reason (code, referral_status_category_code, description, active, deselect_open)
values ('AS_NOT_ENOUGH_TIME', 'AS_OPERATIONAL', 'There is not enough time to complete a high intensity programme so the person should complete a moderate intensity programme', true, true);
