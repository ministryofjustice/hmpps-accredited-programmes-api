
update audit_record set audit_action = 'CREATE_REFERRAL' where audit_action = '0';
update audit_record set audit_action = 'UPDATE_REFERRAL' where audit_action = '1';
update audit_record set audit_action = 'NOMIS_SEARCH_FOR_PERSON' where audit_action = '2';
