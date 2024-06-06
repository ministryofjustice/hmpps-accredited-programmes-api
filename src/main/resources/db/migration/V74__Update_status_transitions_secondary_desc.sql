
UPDATE referral_status_transitions SET secondary_heading='Remove hold' WHERE transition_from_Status = 'ON_HOLD_AWAITING_ASSESSMENT' AND transition_to_status='AWAITING_ASSESSMENT';
