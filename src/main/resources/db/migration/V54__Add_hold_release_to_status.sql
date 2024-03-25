ALTER TABLE referral_status ADD COLUMN hold boolean default false;
ALTER TABLE referral_status ADD COLUMN release boolean default false;

update referral_status set hold = true where code =  'ON_HOLD_REFERRAL_SUBMITTED';
update referral_status set hold = true where code =  'ON_HOLD_ASSESSMENT_STARTED';
update referral_status set hold = true where code =  'ON_HOLD_AWAITING_ASSESSMENT';
update referral_status set hold = true where code =  'SUITABLE_NOT_READY';

update referral_status set release = true where code = 'REFERRAL_SUBMITTED';
update referral_status set release = true where code = 'ASSESSMENT_STARTED';
update referral_status set release = true where code = 'AWAITING_ASSESSMENT';
update referral_status set release = true where code = 'ASSESSED_AS_SUITABLE';

