update referral_status_transitions set primary_heading = 'Withdraw referral' where  transition_to_status = 'WITHDRAWN';
update referral_status_transitions set primary_description = 'If you withdraw the referral the referral will be closed.' where transition_to_status = 'WITHDRAWN';
update referral_status_transitions set secondary_heading = 'Give a reason' where  transition_to_status = 'WITHDRAWN';
update referral_status_transitions set secondary_description = 'Provide more information about the reason for withdrawing this referral.' where transition_to_status = 'WITHDRAWN';

update referral_status_transitions set primary_heading = 'Deselect person: close referral' where  transition_to_status = 'DESELECTED';
update referral_status_transitions set primary_description = 'This person cannot continue this programme. The referral will be closed.' where transition_to_status = 'DESELECTED';
update referral_status_transitions set secondary_heading = 'Give a reason' where  transition_to_status = 'DESELECTED';
update referral_status_transitions set secondary_description = 'Give more information about the reason for deselecting this person.' where transition_to_status = 'DESELECTED';

update referral_status_transitions set primary_heading = 'Deselect person: assessed as suitable' where transition_from_status = 'PROGRAMME_COMPLETE' and transition_to_status = 'ASSESSED_SUITABLE';
update referral_status_transitions set primary_description = 'This person cannot complete the programme now. They may be able to join or restart when the programme runs again.' where transition_from_status = 'PROGRAMME_COMPLETE' and transition_to_status = 'ASSESSED_SUITABLE';
update referral_status_transitions set secondary_heading = 'Give a reason' where transition_from_status = 'PROGRAMME_COMPLETE' and transition_to_status = 'ASSESSED_SUITABLE';
update referral_status_transitions set secondary_description = 'Give more information about the reason for deselecting this person.' where transition_from_status = 'PROGRAMME_COMPLETE' and transition_to_status = 'ASSESSED_SUITABLE';
update referral_status_transitions set warning_text = 'Submitting this will change the status to assessed as suitable.' where transition_from_status = 'PROGRAMME_COMPLETE' and transition_to_status = 'ASSESSED_SUITABLE';

update referral_status_transitions set primary_heading = 'Deselect person: suitable but not ready' where transition_from_status = 'PROGRAMME_COMPLETE' and transition_to_status = 'SUITABLE_NOT_READY';
update referral_status_transitions set primary_description = 'This person cannot complete the programme now. The referral will be paused until the person is ready to continue.' where transition_from_status = 'PROGRAMME_COMPLETE' and transition_to_status = 'SUITABLE_NOT_READY';
update referral_status_transitions set secondary_heading = 'Give a reason' where transition_from_status = 'PROGRAMME_COMPLETE' and transition_to_status = 'SUITABLE_NOT_READY';
update referral_status_transitions set secondary_description = 'Give more information about the reason for deselecting this person.' where transition_from_status = 'PROGRAMME_COMPLETE' and transition_to_status = 'SUITABLE_NOT_READY';
update referral_status_transitions set warning_text = 'Submitting this will change the status to suitable not ready.' where transition_from_status = 'PROGRAMME_COMPLETE' and transition_to_status = 'SUITABLE_NOT_READY';