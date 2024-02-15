CREATE TABLE IF NOT EXISTS audit_record
(
    audit_record_id UUID primary key,
    referral_id UUID,
    prison_number TEXT NOT NULL,
    prisoner_location TEXT,
    referrer_username TEXT,
    referral_status_from TEXT,
    referral_status_to TEXT,
    course_name TEXT,
    course_location TEXT,
    audit_action TEXT NOT NULL,
    audit_username TEXT NOT NULL,
    audit_date_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_audit_prison_number
    ON person(prison_number);
