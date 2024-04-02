
update referral_status_transitions set description = 'Assessed as suitable but not ready' where transition_from_status = 'ASSESSED_SUITABLE' and transition_to_status = 'SUITABLE_NOT_READY';
update referral_status_transitions set hint_text = 'This person meet the suitability criteria but is not ready to start the programme. The referral will be paused until they are ready.' where transition_from_status = 'ASSESSED_SUITABLE' and transition_to_status = 'SUITABLE_NOT_READY';

update referral_status_transitions set description = 'Assessed as suitable and ready to continue' where transition_from_status = 'SUITABLE_NOT_READY' and transition_to_status = 'ASSESSED_SUITABLE';
update referral_status_transitions set hint_text = 'This referral will resume because the person is suitable and ready to be considered for a programme.' where transition_from_status = 'SUITABLE_NOT_READY' and transition_to_status = 'ASSESSED_SUITABLE';

update referral_status_transitions set description = 'Withdraw referral' where transition_to_status = 'WITHDRAWN';
