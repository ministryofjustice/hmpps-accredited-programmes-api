update referral_status_transitions set primary_description = 'If you update the status to not eligible, the referral will be closed.' where transition_from_status = 'REFERRAL_SUBMITTED' and transition_to_status = 'NOT_ELIGIBLE';

