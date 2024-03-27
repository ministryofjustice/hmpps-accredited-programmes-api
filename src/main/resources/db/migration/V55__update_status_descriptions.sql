update referral_status set description = 'Suitable but not ready' where code = 'SUITABLE_NOT_READY';
update referral_status set description = 'Referral submitted' where code = 'REFERRAL_SUBMITTED';
update referral_status set description = 'Assessment started' where code = 'ASSESSMENT_STARTED';
update referral_status set description = 'Awaiting assessment' where code = 'AWAITING_ASSESSMENT';
update referral_status set description = 'Referral started' where code = 'REFERRAL_STARTED';
update referral_status set description = 'Not eligible' where code = 'NOT_ELIGIBLE';
update referral_status set description = 'Withdrawn' where code = 'WITHDRAWN';
update referral_status set description = 'Not suitable' where code = 'NOT_SUITABLE';
update referral_status set description = 'Deselected' where code = 'DESELECTED';
update referral_status set description = 'Assessed as suitable' where code = 'ASSESSED_SUITABLE';
update referral_status set description = 'On programme' where code = 'ON_PROGRAMME';
update referral_status set description = 'Programme complete' where code = 'PROGRAMME_COMPLETE';
update referral_status set description = 'On hold - referral submitted' where code = 'ON_HOLD_REFERRAL_SUBMITTED';
update referral_status set description = 'On hold - assessment started' where code = 'ON_HOLD_ASSESSMENT_STARTED';
update referral_status set description = 'On hold - awaiting assessment' where code = 'ON_HOLD_AWAITING_ASSESSMENT';

