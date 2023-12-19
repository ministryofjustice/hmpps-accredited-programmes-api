
-- Ensure data integrity in the referrer_user table by adding new unique users which don't already exist to it.
INSERT INTO referrer_user (referrer_username)
SELECT DISTINCT r.referrer_username
FROM referral r
WHERE r.referrer_username IS NOT NULL
  AND NOT EXISTS (
    SELECT 1
    FROM referrer_user ru
    WHERE ru.referrer_username = r.referrer_username
);

INSERT INTO referrer_user(referrer_username) VALUES ('UNKNOWN_USER');

-- Always ensure that referrals have an associated user, even if that user is a placeholder for now.
UPDATE referral
SET referrer_username = 'UNKNOWN_USER'
WHERE referrer_username IS NULL OR referrer_username = '';


-- Delete the old field.
ALTER TABLE referral DROP COLUMN referrer_id;
