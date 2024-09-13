
ALTER TABLE referral_status ADD COLUMN case_notes_subtype text;
ALTER TABLE referral_status ADD COLUMN case_notes_message text;

update referral_status set case_notes_subtype = 'PGM_STD', case_notes_message='PRISONER_NAME has started the programme.' where code = 'ON_PROGRAMME';
update referral_status set case_notes_subtype = 'NTELG_FR_PGM', case_notes_message='PRISONER_NAME does not meet the eligibility criteria for this programme. The referral will be closed.' where code = 'NOT_ELIGIBLE';
update referral_status set case_notes_subtype = 'REF_WTHDWN', case_notes_message='The referral will be closed.' where code = 'WITHDRAWN';
update referral_status set case_notes_subtype = 'PGM_COMP', case_notes_message='PRISONER_NAME has completed the programme. The referral will be closed.' where code = 'PROGRAMME_COMPLETE';
update referral_status set case_notes_subtype = 'AW_ASSMT', case_notes_message='PRISONER_NAME meets the eligibility criteria for this programme and will be assessed for suitability.' where code = 'AWAITING_ASSESSMENT';
update referral_status set case_notes_subtype = 'ASSMT_STD', case_notes_message='PRISONER_NAME is being assessed by the programme team for suitability.' where code = 'ASSESSMENT_STARTED';
update referral_status set case_notes_subtype = 'SUT_FR_PGM', case_notes_message='PRISONER_NAME meets the suitability criteria. They can now be considered to join the programme.' where code = 'ASSESSED_SUITABLE';
update referral_status set case_notes_subtype = 'NTSUT_FR_PGM', case_notes_message='PRISONER_NAME does not meet the suitability criteria for this programme. The referral will be closed.' where code = 'NOT_SUITABLE';
update referral_status set case_notes_subtype = 'REFD', case_notes_message='PRISONER_NAME has been referred to PGM_NAME_STRAND.' where code = 'REFERRAL_SUBMITTED';
update referral_status set case_notes_subtype = 'PGM_NT_COMP', case_notes_message='PRISONER_NAME cannot continue the programme. The referral will be closed.' where code = 'DESELECTED';
update referral_status set case_notes_subtype = 'REF_ON_HLD', case_notes_message='The referral will be paused until PRISONER_NAME is ready to continue.' where code = 'ON_HOLD_REFERRAL_SUBMITTED';
update referral_status set case_notes_subtype = 'REF_ON_HLD', case_notes_message='The referral will be paused until PRISONER_NAME is ready to continue.' where code = 'ON_HOLD_AWAITING_ASSESSMENT';
update referral_status set case_notes_subtype = 'REF_ON_HLD', case_notes_message='The assessment has not been completed, but the referral will be paused until PRISONER_NAME is ready to continue.' where code = 'ON_HOLD_ASSESSMENT_STARTED';
update referral_status set case_notes_subtype = 'SUTPGM_NTRDY', case_notes_message='Suitable but not ready (including via deselected open route)' where code = 'SUITABLE_NOT_READY';
-- No case notes to be added for REFERRAL_STARTED
update referral_status set case_notes_subtype = '', case_notes_message='' where code = 'REFERRAL_STARTED';
