ALTER TABLE referral
    ADD CONSTRAINT r_status_fk
        FOREIGN KEY (status) REFERENCES referral_status(code);

ALTER TABLE referral_status_history
    ADD CONSTRAINT rs_referral_fk
        FOREIGN KEY (referral_id) REFERENCES referral(referral_id);

ALTER TABLE referral_status_history
    ADD CONSTRAINT rs_status_fk
        FOREIGN KEY (status) REFERENCES referral_status(code);

ALTER TABLE referral_status_history
    ADD CONSTRAINT rs_previous_status_fk
        FOREIGN KEY (previous_status) REFERENCES referral_status(code);

ALTER TABLE referral_status_history
    ADD CONSTRAINT rs_category_fk
        FOREIGN KEY (category) REFERENCES referral_status_category(code);

ALTER TABLE referral_status_history
    ADD CONSTRAINT rs_reason_fk
        FOREIGN KEY (reason) REFERENCES referral_status_reason(code);