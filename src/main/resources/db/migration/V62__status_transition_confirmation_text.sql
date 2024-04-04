update referral_status_transitions set primary_heading = 'Not eligible for the programme' where transition_from_status = 'REFERRAL_SUBMITTED' and transition_to_status = 'NOT_ELIGIBLE';
update referral_status_transitions set primary_description = 'If you update the status to not elibiblem the referral will be closed.' where transition_from_status = 'REFERRAL_SUBMITTED' and transition_to_status = 'NOT_ELIGIBLE';
update referral_status_transitions set secondary_heading = 'Not Eligible' where transition_from_status = 'REFERRAL_SUBMITTED' and transition_to_status = 'NOT_ELIGIBLE';
update referral_status_transitions set secondary_description = 'You must give a reason why this person is not eligible for the programme.' where transition_from_status = 'REFERRAL_SUBMITTED' and transition_to_status = 'NOT_ELIGIBLE';

update referral_status_transitions set primary_heading = 'Put referral on hold' where transition_from_status = 'REFERRAL_SUBMITTED' and transition_to_status = 'ON_HOLD_REFERRAL_SUBMITTED';
update referral_status_transitions set primary_description = 'If you put a referral on hold it will not progress until it is removed from hold.' where transition_from_status = 'REFERRAL_SUBMITTED' and transition_to_status = 'ON_HOLD_REFERRAL_SUBMITTED';
update referral_status_transitions set secondary_heading = 'On hold' where transition_from_status = 'REFERRAL_SUBMITTED' and transition_to_status = 'ON_HOLD_REFERRAL_SUBMITTED';
update referral_status_transitions set secondary_description = 'You must give a reason for putting this referral on hold.' where transition_from_status = 'REFERRAL_SUBMITTED' and transition_to_status = 'ON_HOLD_REFERRAL_SUBMITTED';

update referral_status_transitions set primary_heading = 'Remove the referral from hold' where transition_from_status = 'ON_HOLD_REFERRAL_SUBMITTED' and transition_to_status = 'REFERRAL_SUBMITTED';
update referral_status_transitions set primary_description = 'Removing the referral from hold will move it back to in progress.' where transition_from_status = 'ON_HOLD_REFERRAL_SUBMITTED' and transition_to_status = 'REFERRAL_SUBMITTED';
update referral_status_transitions set secondary_heading = 'Remove hold' where transition_from_status = 'ON_HOLD_REFERRAL_SUBMITTED' and transition_to_status = 'REFERRAL_SUBMITTED';
update referral_status_transitions set secondary_description = 'The referral will move back to in progress. You must give a reason for removing the hold' where transition_from_status= 'ON_HOLD_REFERRAL_SUBMITTED' and transition_to_status = 'REFERRAL_SUBMITTED';

update referral_status_transitions set primary_heading = 'Put referral on hold' where transition_from_status = 'AWAITING_ASSESSMENT' and transition_to_status = 'ON_HOLD_AWAITING_ASSESSMENT';
update referral_status_transitions set primary_description = 'If you put a referral on hold it will not progress until it is removed from hold.' where transition_from_status = 'AWAITING_ASSESSMENT' and transition_to_status = 'ON_HOLD_AWAITING_ASSESSMENT';
update referral_status_transitions set secondary_heading = 'On hold' where transition_from_status = 'AWAITING_ASSESSMENT' and transition_to_status = 'ON_HOLD_AWAITING_ASSESSMENT';
update referral_status_transitions set secondary_description = 'You must give a reason for putting this referral on hold.' where transition_from_status = 'AWAITING_ASSESSMENT' and transition_to_status = 'ON_HOLD_AWAITING_ASSESSMENT';

update referral_status_transitions set primary_heading = 'Remove the referral from hold' where transition_from_status = 'ON_HOLD_AWAITING_ASSESSMENT' and transition_to_status = 'AWAITING_ASSESSMENT';
update referral_status_transitions set primary_description = 'Removing the referral from hold will move it back to in progress.' where transition_from_status = 'ON_HOLD_AWAITING_ASSESSMENT' and transition_to_status = 'AWAITING_ASSESSMENT';
update referral_status_transitions set secondary_heading = 'Remove hold' where transition_from_status = 'ON_HOLD_REFERRAL_SUBMITTED' and transition_to_status = 'AWAITING_ASSESSMENT';
update referral_status_transitions set secondary_description = 'The referral will move back to in progress. You must give a reason for removing the hold' where transition_from_status= 'ON_HOLD_AWAITING_ASSESSMENT' and transition_to_status = 'AWAITING_ASSESSMENT';

update referral_status_transitions set primary_heading = 'Pause referral: suitable but not ready' where transition_from_status = 'ASSESSMENT_STARTED' and transition_to_status = 'SUITABLE_NOT_READY';
update referral_status_transitions set primary_description = 'The referral will be paused until the person is ready to continue.' where transition_from_status = 'ASSESSMENT_STARTED' and transition_to_status = 'SUITABLE_NOT_READY';
update referral_status_transitions set secondary_heading = 'Give a reason' where transition_from_status = 'ASSESSMENT_STARTED' and transition_to_status = 'SUITABLE_NOT_READY';
update referral_status_transitions set secondary_description = 'You must give a reason why the person is not ready to continue.' where transition_from_status = 'ASSESSMENT_STARTED' and transition_to_status = 'SUITABLE_NOT_READY';

update referral_status_transitions set primary_heading = 'Continue referral' where transition_from_status = 'SUITABLE_NOT_READY' and transition_to_status = 'ASSESSMENT_STARTED';
update referral_status_transitions set primary_description = 'The referral will resume because the person is suitable and ready to continue.' where transition_from_status = 'SUITABLE_NOT_READY' and transition_to_status = 'ASSESSMENT_STARTED';
update referral_status_transitions set secondary_heading = 'Give a reason' where transition_from_status = 'SUITABLE_NOT_READY' and transition_to_status = 'ASSESSMENT_STARTED';
update referral_status_transitions set secondary_description = 'The referral will resume. You must give a reason why the person is now ready' where transition_from_status = 'SUITABLE_NOT_READY' and transition_to_status = 'ASSESSMENT_STARTED';

update referral_status_transitions set primary_heading = 'Close referral: not suitable' where transition_from_status = 'ASSESSMENT_STARTED' and transition_to_status = 'NOT_SUITABLE';
update referral_status_transitions set primary_description = 'If you decide that the person is not suitable for this programme the referral will be closed.' where transition_from_status = 'ASSESSMENT_STARTED' and transition_to_status = 'NOT_SUITABLE';
update referral_status_transitions set secondary_heading = 'Give a reason' where transition_from_status = 'ASSESSMENT_STARTED' and transition_to_status = 'NOT_SUITABLE';
update referral_status_transitions set secondary_description = 'You must give a reason why the person is not suitable for this programme.' where transition_from_status = 'ASSESSMENT_STARTED' and transition_to_status = 'NOT_SUITABLE';

update referral_status_transitions set primary_heading = 'Put referral on hold: assessment not completed' where transition_from_status = 'ASSESSMENT_STARTED' and transition_to_status = 'ON_HOLD_ASSESSMENT_STARTED';
update referral_status_transitions set primary_description = 'If you put the referral on hold it will not continue until it is removed from hold.' where transition_from_status = 'ASSESSMENT_STARTED' and transition_to_status = 'ON_HOLD_ASSESSMENT_STARTED';
update referral_status_transitions set secondary_heading = 'Give a reason' where transition_from_status = 'ASSESSMENT_STARTED' and transition_to_status = 'ON_HOLD_ASSESSMENT_STARTED';
update referral_status_transitions set secondary_description = 'You must give a reason for putting this referral on hold.' where transition_from_status = 'ASSESSMENT_STARTED' and transition_to_status = 'ON_HOLD_ASSESSMENT_STARTED';

update referral_status_transitions set primary_heading = 'Remove referral from hold' where transition_from_status = 'ON_HOLD_ASSESSMENT_STARTED' and transition_to_status = 'ASSESSMENT_STARTED';
update referral_status_transitions set primary_description = 'Removing the referral from hold will move it back to in progress.' where transition_from_status = 'ON_HOLD_ASSESSMENT_STARTED' and transition_to_status = 'ASSESSMENT_STARTED';
update referral_status_transitions set secondary_heading = 'Remove hold' where transition_from_status = 'ON_HOLD_ASSESSMENT_STARTED' and transition_to_status = 'ASSESSMENT_STARTED';
update referral_status_transitions set secondary_description = 'The referral will move back to in progress. You must give a reason for removing the hold.' where transition_from_status = 'ON_HOLD_ASSESSMENT_STARTED' and transition_to_status = 'ASSESSMENT_STARTED';

update referral_status_transitions set primary_heading = 'Pause referral: suitable but not ready' where transition_from_status = 'ASSESSED_SUITABLE' and transition_to_status = 'SUITABLE_NOT_READY';
update referral_status_transitions set primary_description = 'This referral will be paused until the person is ready to continue.' where transition_from_status = 'ASSESSED_SUITABLE' and transition_to_status = 'SUITABLE_NOT_READY';
update referral_status_transitions set secondary_heading = 'Give a reason' where transition_from_status = 'ASSESSED_SUITABLE' and transition_to_status = 'SUITABLE_NOT_READY';
update referral_status_transitions set secondary_description = 'You must give a reason why the person is not ready to continue.' where transition_from_status = 'ASSESSED_SUITABLE' and transition_to_status = 'SUITABLE_NOT_READY';

update referral_status_transitions set primary_heading = 'Continue referral' where transition_from_status = 'SUITABLE_NOT_READY' and transition_to_status = 'ASSESSED_SUITABLE';
update referral_status_transitions set primary_description = 'The referral will resume because the person is suitable and ready to continue.' where transition_from_status = 'SUITABLE_NOT_READY' and transition_to_status = 'ASSESSED_SUITABLE';
update referral_status_transitions set secondary_heading = 'Give a reason' where transition_from_status = 'SUITABLE_NOT_READY' and transition_to_status = 'ASSESSED_SUITABLE';
update referral_status_transitions set secondary_description = 'The referral will resume. You must give a reason why the person is now.' where transition_from_status = 'SUITABLE_NOT_READY' and transition_to_status = 'ASSESSED_SUITABLE';