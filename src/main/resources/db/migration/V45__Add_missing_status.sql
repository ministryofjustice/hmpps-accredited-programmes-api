update referral_status set closed = true where code = 'DESELECTED';

INSERT INTO referral_status VALUES ('NOT_ELIGIBLE','Not Eligible','light-grey', true, false, true );
INSERT INTO referral_status VALUES ('ON_HOLD_REFERRAL_SUBMITTED','On hold - referral submitted','pink', true, false, false );
INSERT INTO referral_status VALUES ('ON_HOLD_ASSESSMENT_STARTED','On hold - assessment started','pink', true, false, false );