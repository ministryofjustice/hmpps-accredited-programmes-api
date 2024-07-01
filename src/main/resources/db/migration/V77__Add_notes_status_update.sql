ALTER TABLE referral_status ADD COLUMN notes_optional boolean default false;

update referral_status
set has_notes = true
where code in (
               'ON_PROGRAMME',
               'PROGRAMME_COMPLETE',
               'AWAITING_ASSESSMENT',
               'ASSESSMENT_STARTED',
               'ASSESSED_SUITABLE'
    );

update referral_status
set notes_optional = true
where code in (
               'ON_PROGRAMME',
               'PROGRAMME_COMPLETE',
               'AWAITING_ASSESSMENT',
               'ASSESSMENT_STARTED',
               'ASSESSED_SUITABLE'
    );


