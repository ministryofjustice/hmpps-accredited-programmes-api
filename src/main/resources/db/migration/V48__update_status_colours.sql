update referral_status set colour = 'light-blue' where code = 'AWAITING_ASSESSMENT';
update referral_status set colour = 'purple' where code = 'ASSESSED_SUITABLE';
update referral_status set colour = 'pink' where code = 'ON_PROGRAMME';
update referral_status set colour = 'yellow' where code = 'SUITABLE_NOT_READY';
update referral_status set colour = 'yellow' where code = 'ON_HOLD_AWAITING_ASSESSMENT';
update referral_status set colour = 'blue' where code = 'ASSESSMENT_STARTED';
update referral_status set colour = 'green' where code = 'REFERRAL_SUBMITTED';
update referral_status set colour = 'light-blue' where code = 'REFERRAL_STARTED';
update referral_status set colour = 'grey' where code = 'PROGRAMME_COMPLETE';
update referral_status set colour = 'red' where code = 'WITHDRAWN';
update referral_status set colour = 'red' where code = 'NOT_SUITABLE';
update referral_status set colour = 'red' where code = 'DESELECTED';
update referral_status set colour = 'red' where code = 'NOT_ELIGIBLE';
update referral_status set colour = 'yellow' where code = 'ON_HOLD_REFERRAL_SUBMITTED';
update referral_status set colour = 'yellow' where code = 'ON_HOLD_ASSESSMENT_STARTED';