ALTER TABLE referral_status ADD COLUMN has_notes boolean default true;
ALTER TABLE referral_status ADD COLUMN has_confirmation boolean default false;
ALTER TABLE referral_status ADD COLUMN hint_text text;
ALTER TABLE referral_status ADD COLUMN confirmation_text text;

update referral_status set has_notes = false where code =  'ASSESSED_SUITABLE';
update referral_status set has_notes = false where code =  'AWAITING_ASSESSMENT';
update referral_status set has_notes = false where code =  'ASSESSMENT_STARTED';
update referral_status set has_notes = false where code =  'ON_PROGRAMME';
update referral_status set has_notes = false where code =  'PROGRAMME_COMPLETE';

update referral_status set has_confirmation = true where code =  'ASSESSED_SUITABLE';
update referral_status set has_confirmation = true where code =  'AWAITING_ASSESSMENT';
update referral_status set has_confirmation = true where code =  'ASSESSMENT_STARTED';
update referral_status set has_confirmation = true where code =  'ON_PROGRAMME';
update referral_status set has_confirmation = true where code =  'PROGRAMME_COMPLETE';

update referral_status set hint_text = 'This person meets the eligibility criteria for this programme and will be assessed for suitability.' where code =  'AWAITING_ASSESSMENT';
update referral_status set hint_text = 'This person does not meet the eligibility criteria for this programme. The referral will be closed.' where code =  'NOT_ELIGIBLE';
update referral_status set hint_text = 'The referral will be paused until the person is ready to continue.' where code =  'ON_HOLD_REFERRAL_SUBMITTED';
update referral_status set hint_text = 'The assessment has not been completed. The referral will be paused until the person is ready to continue.' where code =  'ON_HOLD_ASSESSMENT_STARTED';
update referral_status set hint_text = 'The referral will be paused until the person is ready to continue.' where code =  'ON_HOLD_AWAITING_ASSESSMENT';
update referral_status set hint_text = 'The referral will be closed.' where code =  'WITHDRAWN';
update referral_status set hint_text = 'This person is being assessed by the programme team for suitability.' where code =  'ASSESSMENT_STARTED';
update referral_status set hint_text = 'This person meets the suitability criteria. They can now be considered to join this programme.' where code =  'ASSESSED_SUITABLE';
update referral_status set hint_text = 'This person meets the suitability criteria but is not ready to start the programme. The referral will be paused until they are ready.' where code =  'SUITABLE_NOT_READY';
update referral_status set hint_text = 'This person does not meet the suitablity criteria for this programme. The referral will be closed.' where code =  'NOT_SUITABLE';
update referral_status set hint_text = 'This person has started the programme.' where code =  'ON_PROGRAMME';
update referral_status set hint_text = 'This person has completed the programme. The referral will now be closed.' where code =  'PROGRAMME_COMPLETE';
update referral_status set hint_text = 'This person cannot continue the programme. The referral will be closed.' where code =  'DESELECTED';

update referral_status set confirmation_text = 'I confirm the person has been assessed as suitable.' where code =  'ASSESSED_SUITABLE';
update referral_status set confirmation_text = 'I confirm the person meets the programme eligibility criteria and will be assessed for suitability.' where code =  'AWAITING_ASSESSMENT';
update referral_status set confirmation_text = 'I confirm the person is being assessed for suitability by the programme team.' where code =  'ASSESSMENT_STARTED';
update referral_status set confirmation_text = 'I confirm the person has started the programme.' where code =  'ON_PROGRAMME';
update referral_status set confirmation_text = 'I confirm the person has completed the programme.' where code =  'PROGRAMME_COMPLETE';