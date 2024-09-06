
update referral_status_transitions set hint_text = 'This person meets the suitability criteria but is not ready to start the programme. The referral will be paused until they are ready.' where transition_from_status = 'ON_PROGRAMME' and transition_to_status = 'SUITABLE_NOT_READY';
update referral_status_transitions set hint_text = 'This person meets the suitability criteria but is not ready to start the programme. The referral will be paused until they are ready.' where transition_from_status = 'ASSESSED_SUITABLE' and transition_to_status = 'SUITABLE_NOT_READY';

update referral_status set hint_text = 'This person does not meet the suitability criteria for this programme. The referral will be closed.' where code =  'NOT_SUITABLE';
