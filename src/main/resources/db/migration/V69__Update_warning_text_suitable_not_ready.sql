

update referral_status_transitions set warning_text = 'Submitting this will change the referral status to suitable but not ready.'
WHERE transition_from_status='ON_PROGRAMME' AND transition_to_status='SUITABLE_NOT_READY'
  AND warning_text='Submitting this will change the status to suitable not ready.';
