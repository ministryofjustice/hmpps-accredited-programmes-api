
DROP TABLE organisation;

CREATE TABLE IF NOT EXISTS organisation
(
    organisation_id UUID primary key,
    code TEXT NOT NULL,
    name TEXT NOT NULL
);

CREATE INDEX idx_organisation_code
    ON organisation(code);