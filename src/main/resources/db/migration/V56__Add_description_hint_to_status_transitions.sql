ALTER TABLE referral_status_transitions ADD COLUMN hint_text text;
ALTER TABLE referral_status_transitions ADD COLUMN description text;

delete from referral_status_transitions where transition_from_status = 'AWAITING_ASSESSMENT' and transition_to_status = 'NOT_ELIGIBLE';

update referral_status_transitions set description = 'On hold' where transition_from_status = 'REFERRAL_SUBMITTED' and transition_to_status = 'ON_HOLD_REFERRAL_SUBMITTED';

update referral_status_transitions set description = 'On hold' where transition_from_status = 'AWAITING_ASSESSMENT' and transition_to_status = 'ON_HOLD_AWAITING_ASSESSMENT';

update referral_status_transitions set description = 'Assessed as suitable and ready to continue' where transition_from_status = 'ASSESSMENT_STARTED' and transition_to_status = 'ASSESSED_SUITABLE';
update referral_status_transitions set hint_text = 'This person meets the suitability criteria. They can now be considered to join the programme.' where transition_from_status = 'ASSESSMENT_STARTED' and transition_to_status = 'ASSESSED_SUITABLE';

update referral_status_transitions set description = 'Assessed as suitable but not ready' where transition_from_status = 'ASSESSMENT_STARTED' and transition_to_status = 'SUITABLE_NOT_READY';
update referral_status_transitions set hint_text = 'This person meets the suitability criteria but is not ready to start the programme. The referral will be paused until they are ready.' where transition_from_status = 'ASSESSMENT_STARTED' and transition_to_status = 'SUITABLE_NOT_READY';

update referral_status_transitions set description = 'On hold - assessment not completed' where transition_from_status = 'ASSESSMENT_STARTED' and transition_to_status = 'ON_HOLD_ASSESSMENT_STARTED';
update referral_status_transitions set hint_text = 'The assessment has not been completed, but the referral will be paused until the person is ready to continue.' where transition_from_status = 'ASSESSMENT_STARTED' and transition_to_status = 'ON_HOLD_ASSESSMENT_STARTED';

update referral_status_transitions set description = 'Assessed as suitable but not ready' where transition_from_status = 'ASSESSED_SUITABLE' and transition_to_status = 'SUITABLE_NOT_READY';
update referral_status_transitions set hint_text = 'This person meets the suitability criteria but is not ready to start the programme. The referral wil be paused until they are ready.' where transition_from_status = 'ASSESSED_SUITABLE' and transition_to_status = 'SUITABLE_NOT_READY';

update referral_status_transitions set description = 'Assessed as suitable and ready' where transition_from_status = 'ON_PROGRAMME' and transition_to_status = 'ASSESSED_SUITABLE';
update referral_status_transitions set hint_text = 'This person has been deselected. However, they still meet the suitability criteria and can be considered to join a programme when it runs again.' where transition_from_status = 'ON_PROGRAMME' and transition_to_status = 'ASSESSED_SUITABLE';

update referral_status_transitions set description = 'Assessed as suitable but not ready' where transition_from_status = 'ON_PROGRAMME' and transition_to_status = 'SUITABLE_NOT_READY';
update referral_status_transitions set hint_text = 'This person meet the suitability criteria but is not ready to start the programme. The referral will be paused until they are ready.' where transition_from_status = 'ON_PROGRAMME' and transition_to_status = 'SUITABLE_NOT_READY';

update referral_status_transitions set description = 'Assessed as suitable and ready and ready to continue' where transition_from_status = 'ASSESSED_SUITABLE' and transition_to_status = 'SUITABLE_NOT_READY';
update referral_status_transitions set hint_text = 'The referral will resume because the person is suitable and ready to be considered for a programme.' where transition_from_status = 'ASSESSED_SUITABLE' and transition_to_status = 'SUITABLE_NOT_READY';

update referral_status_transitions set description = 'Assessed as suitable and ready and ready to continue' where transition_from_status = 'ASSESSED_STARTED' and transition_to_status = 'ASSESSED_SUITABLE';
update referral_status_transitions set hint_text = 'This person meets the suitability criteria. they can now be considered to join this programme.' where transition_from_status = 'ASSESSED_STARTED' and transition_to_status = 'ASSESSED_SUITABLE';

update referral_status_transitions set description = 'Assessed as suitable but not ready' where transition_from_status = 'ASSESSED_STARTED' and transition_to_status = 'SUITABLE_NOT_READY';
update referral_status_transitions set hint_text = 'This person meet the suitability criteria but is not ready to start the programme. The referral will be paused until they are ready.' where transition_from_status = 'SUITABLE_NOT_READY' and transition_to_status = 'ASSESSED_SUITABLE';
