ALTER TABLE referral_status ADD COLUMN default_order smallint;

update referral_status set default_order = 1 where code = 'REFERRAL_SUBMITTED';
update referral_status set default_order = 1 where code = 'ASSESSMENT_STARTED';
update referral_status set default_order = 1 where code = 'AWAITING_ASSESSMENT';
update referral_status set default_order = 1 where code = 'REFERRAL_STARTED';
update referral_status set default_order = 1 where code = 'DESELECTED';
update referral_status set default_order = 1 where code = 'ASSESSED_SUITABLE';
update referral_status set default_order = 1 where code = 'ON_PROGRAMME';
update referral_status set default_order = 1 where code = 'PROGRAMME_COMPLETE';
update referral_status set default_order = 20 where code = 'NOT_ELIGIBLE';
update referral_status set default_order = 40 where code = 'SUITABLE_NOT_READY';
update referral_status set default_order = 40 where code = 'ON_HOLD_REFERRAL_SUBMITTED';
update referral_status set default_order = 40 where code = 'ON_HOLD_ASSESSMENT_STARTED';
update referral_status set default_order = 40 where code = 'ON_HOLD_AWAITING_ASSESSMENT';
update referral_status set default_order = 50 where code = 'NOT_SUITABLE';
update referral_status set default_order = 100 where code = 'WITHDRAWN';


update referral_status_transitions set description = 'Remove hold' where transition_from_status = 'ON_HOLD_AWAITING_ASSESSMENT' and transition_to_status = 'AWAITING_ASSESSMENT';
update referral_status_transitions set hint_text = 'The referral will resume because the person is ready to continue.' where transition_from_status = 'AWAITING_ASSESSMENT' and transition_to_status = 'ASSESSED_SUITABLE';
