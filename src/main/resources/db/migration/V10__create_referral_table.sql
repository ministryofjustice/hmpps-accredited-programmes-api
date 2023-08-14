CREATE TABLE IF NOT EXISTS referral
(
    referral_id TEXT NOT NULL,
    offering_id TEXT NOT NULL,
    prison_number TEXT NOT NULL,
    referrer_id TEXT NOT NULL,
    CONSTRAINT referral_pk PRIMARY KEY (referral_id),
    CONSTRAINT referral_offering_fk FOREIGN KEY (offering_id) REFERENCES offering(offering_id)
);