
update referral set status = 'ON_PROGRAMME'
                where prison_number='A6918AF'AND referral_id='3e417f06-49c8-44fb-a850-29e9bf2e9653';

delete from referral_status_history
       where status_history_id='f109c012-0e8a-4935-b741-fba7f34a2634' AND referral_id='3e417f06-49c8-44fb-a850-29e9bf2e9653'
        AND status='PROGRAMME_COMPLETE' AND previous_status='ON_PROGRAMME';

update referral_status_history set status_end_date = null
        where status_history_id='26f08d12-74f2-47b9-affe-f6fbc39e6779' AND referral_id='3e417f06-49c8-44fb-a850-29e9bf2e9653'
        AND status='ON_PROGRAMME' AND previous_status='ASSESSED_SUITABLE';
