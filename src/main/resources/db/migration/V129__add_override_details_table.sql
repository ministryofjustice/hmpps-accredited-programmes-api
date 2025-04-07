CREATE TABLE IF NOT EXISTS override_details
(
    id UUID NOT NULL PRIMARY KEY,
    referral_id UUID NOT NULL,
    recommended_pathway TEXT,
    requested_pathway TEXT,
    referrer_override_reason TEXT
);

ALTER TABLE override_details
    ADD CONSTRAINT override_details_referral_fk
        FOREIGN KEY (referral_id) REFERENCES referral(referral_id);

ALTER TABLE referral ADD COLUMN override_details_id UUID NULL;
ALTER TABLE referral ADD CONSTRAINT referral_override_details_fk FOREIGN KEY (override_details_id) REFERENCES override_details(id);