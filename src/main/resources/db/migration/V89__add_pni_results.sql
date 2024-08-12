
CREATE TABLE IF NOT EXISTS pni_result
(
    pni_result_id UUID NOT NULL PRIMARY KEY,
    referral_id UUID,
    prison_number TEXT NOT NULL,
    crn TEXT,
    oasys_assessment_id BIGINT,
    oasys_assessment_completed_date TIMESTAMP,
    programme_pathway TEXT,
    needs_classification TEXT,
    overall_needs_score SMALLINT,
    risk_classification TEXT,
    pni_assessment_date TIMESTAMP,
    pni_valid BOOLEAN,
    pni_result_json TEXT
);

ALTER TABLE pni_result
    ADD CONSTRAINT r_pni_result_referral_fk
        FOREIGN KEY (referral_id) REFERENCES referral(referral_id);