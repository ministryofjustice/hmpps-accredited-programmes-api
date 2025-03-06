

CREATE TABLE IF NOT EXISTS OASYS_PNI_RESULT_TEMP
(
    pni_result_id UUID NOT NULL PRIMARY KEY,
    prison_number TEXT NOT NULL,
    oasys_assessment_id BIGINT,
    programme_pathway TEXT
);