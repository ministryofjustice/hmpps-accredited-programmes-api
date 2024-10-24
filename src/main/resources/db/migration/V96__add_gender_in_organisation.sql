

ALTER TABLE organisation ADD COLUMN gender CHAR (1);

update organisation SET gender = 'M' where code in ('WTI', 'SKI', 'ONI', 'FEI', 'LLI', 'PBI');
update organisation SET gender = 'F' where code in ('DHI', 'PFI');

ALTER TABLE offering
    ADD CONSTRAINT offering_organisation_fk
        FOREIGN KEY (organisation_id) REFERENCES organisation(code);
