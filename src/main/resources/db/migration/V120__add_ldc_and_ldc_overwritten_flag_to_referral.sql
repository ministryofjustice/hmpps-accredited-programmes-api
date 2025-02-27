ALTER TABLE referral
    ADD COLUMN has_ldc BOOLEAN NOT NULL DEFAULT false;

ALTER TABLE referral
    ADD COLUMN has_ldc_been_overwritten_by_programme_team BOOLEAN NOT NULL DEFAULT false;