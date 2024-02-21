ALTER TABLE referral_status ADD COLUMN draft boolean default false;
ALTER TABLE referral_status ADD COLUMN closed boolean default false;

update referral_status set draft = true where code =  'REFERRAL_STARTED';
update referral_status set closed = true where code = 'PROGRAMME_COMPLETE';
update referral_status set closed = true where code = 'WITHDRAWN';
update referral_status set closed = true where code = 'NOT_SUITABLE';
