CREATE TABLE staff
(
    id            UUID PRIMARY KEY,
    staff_id      BIGINT NOT NULL,
    first_name    TEXT NOT NULL,
    last_name     TEXT NOT NULL,
    primary_email TEXT NOT NULL,
    username      TEXT NOT NULL,
    type          TEXT NOT NULL,
    account_type  TEXT,
    referral_id   UUID         NOT NULL,
    CONSTRAINT fk_referral
        FOREIGN KEY (referral_id)
            REFERENCES referral (referral_id)
);