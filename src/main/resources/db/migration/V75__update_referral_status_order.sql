update referral_status set default_order = 10 where code = 'REFERRAL_STARTED';
update referral_status set default_order = 20 where code = 'REFERRAL_SUBMITTED';
update referral_status set default_order = 30 where code = 'ON_HOLD_REFERRAL_SUBMITTED';
update referral_status set default_order = 40 where code = 'AWAITING_ASSESSMENT';
update referral_status set default_order = 50 where code = 'ON_HOLD_AWAITING_ASSESSMENT';
update referral_status set default_order = 60 where code = 'ASSESSMENT_STARTED';
update referral_status set default_order = 70 where code = 'ON_HOLD_ASSESSMENT_STARTED';
update referral_status set default_order = 80 where code = 'ASSESSED_SUITABLE';
update referral_status set default_order = 90 where code = 'SUITABLE_NOT_READY';
update referral_status set default_order = 100 where code = 'ON_PROGRAMME';
update referral_status set default_order = 110 where code = 'NOT_ELIGIBLE';
update referral_status set default_order = 120 where code = 'NOT_SUITABLE';
update referral_status set default_order = 130 where code = 'WITHDRAWN';
update referral_status set default_order = 140 where code = 'DESELECTED';
update referral_status set default_order = 150 where code = 'PROGRAMME_COMPLETE';
