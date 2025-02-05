
ALTER TABLE referral
    ADD COLUMN transfer_reason text;

ALTER TABLE referral
    ADD COLUMN original_referral_id uuid;