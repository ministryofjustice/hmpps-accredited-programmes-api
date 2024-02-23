CREATE TABLE IF NOT EXISTS referral_status_transitions
(
    referral_status_transition_id UUID PRIMARY KEY,
    pt_user BOOLEAN NOT NULL,
    pom_user BOOLEAN NOT NULL,
    transition_from_status TEXT NOT NULL,
    transition_to_status TEXT NOT NULL
);

ALTER TABLE referral_status_transitions
    ADD CONSTRAINT from_status_fk
        FOREIGN KEY (transition_from_status) REFERENCES referral_status(code);

ALTER TABLE referral_status_transitions
    ADD CONSTRAINT to_status_fk
        FOREIGN KEY (transition_to_status) REFERENCES referral_status(code);