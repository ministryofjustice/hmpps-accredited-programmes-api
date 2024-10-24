

ALTER TABLE organisation ADD COLUMN gender CHAR (1);

update organisation SET gender = 'M' where code in ('WTI', 'SKI', 'ONI', 'FEI', 'LLI', 'PBI');
update organisation SET gender = 'F' where code in ('DHI', 'PFI');
