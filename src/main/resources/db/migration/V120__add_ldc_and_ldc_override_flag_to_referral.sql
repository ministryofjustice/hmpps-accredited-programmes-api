ALTER TABLE referral
    ADD COLUMN has_ldc BOOLEAN;

ALTER TABLE referral
    ADD COLUMN has_ldc_been_overridden_by_programme_team BOOLEAN NOT NULL DEFAULT false;
