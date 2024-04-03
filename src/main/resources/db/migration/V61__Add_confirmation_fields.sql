ALTER TABLE referral_status_transitions ADD COLUMN primary_heading text;
ALTER TABLE referral_status_transitions ADD COLUMN primary_description text;
ALTER TABLE referral_status_transitions ADD COLUMN secondary_heading text;
ALTER TABLE referral_status_transitions ADD COLUMN secondary_description text;
ALTER TABLE referral_status_transitions ADD COLUMN warning_text text;