
ALTER TABLE staff DROP COLUMN type;
ALTER TABLE staff DROP COLUMN referral_id;

ALTER TABLE referral ADD COLUMN primary_pom_staff_id BIGINT NULL;
ALTER TABLE referral ADD COLUMN secondary_pom_staff_id BIGINT NULL;