
ALTER TABLE staff DROP COLUMN type;

ALTER TABLE referral ADD COLUMN primaryPomStaffId BIGINT NULL;
ALTER TABLE referral ADD COLUMN secondaryPomStaffId BIGINT NULL;
