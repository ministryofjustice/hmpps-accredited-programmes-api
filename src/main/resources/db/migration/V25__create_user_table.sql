CREATE TABLE IF NOT EXISTS referrer_user
(
    referrer_username TEXT NOT NULL,
    CONSTRAINT referrer_user_username_pk PRIMARY KEY (referrer_username)
);

ALTER TABLE referral
    ADD COLUMN referrer_username TEXT;

ALTER TABLE referral
    ADD CONSTRAINT referrer_user_username_fk
        FOREIGN KEY (referrer_username) REFERENCES referrer_user(referrer_username);