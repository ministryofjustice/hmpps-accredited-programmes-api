CREATE TABLE IF NOT EXISTS person
(
    person_id UUID primary key,
    prison_number TEXT NOT NULL UNIQUE,
    forename TEXT NOT NULL,
    surname TEXT NOT NULL,
    conditional_release_date DATE,
    parole_eligibility_date DATE,
    tariff_expiry_date DATE,
    earliest_release_date DATE,
    earliest_release_date_type TEXT,
    indeterminate_sentence Boolean,
    non_dto_release_date_type TEXT
);

CREATE INDEX IF NOT EXISTS idx_person_prison_number
    ON person(prison_number);