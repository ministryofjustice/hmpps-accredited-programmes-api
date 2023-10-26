-- Update additional_information column with values from reason
-- only if additional_information is empty or null.
UPDATE referral
SET additional_information = reason
WHERE (additional_information IS NULL OR additional_information = '')
  AND reason IS NOT NULL;

-- Drop the reason column.
ALTER TABLE referral DROP COLUMN reason;
