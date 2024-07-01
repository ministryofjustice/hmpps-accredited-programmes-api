update referral_status
set has_notes = true
where code in (
               'ON_PROGRAMME',
               'PROGRAMME_COMPLETE',
               'AWAITING_ASSESSMENT',
               'ASSESSMENT_STARTED',
               'ASSESSED_SUITABLE'
    );


