CREATE TABLE IF NOT EXISTS eligibility_override_reason (
     id UUID PRIMARY KEY,
     referral_id UUID NOT NULL,
     reason text not null,
     override_type text not null,
     CONSTRAINT fk_eligibility_override_reason_referral FOREIGN KEY (referral_id) REFERENCES referral(referral_id)
);

-- Add indexes for the foreign keys
CREATE INDEX IF NOT EXISTS idx_eligibility_override_reason_referral_id ON eligibility_override_reason(referral_id);

