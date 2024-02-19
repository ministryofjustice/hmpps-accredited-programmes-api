CREATE TABLE IF NOT EXISTS referral_status_history
(
    status_history_id UUID primary key,
    referral_id UUID NOT NULL,
    status TEXT NOT NULL,
    previous_status TEXT,
    category TEXT,
    reason TEXT,
    notes TEXT,
    status_start_date TIMESTAMP,
    status_end_date TIMESTAMP,
    duration_at_this_status BIGINT,
    username TEXT
);
