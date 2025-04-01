ALTER table referral add column has_reviewed_additional_information boolean null;

UPDATE referral set has_reviewed_additional_information = true
WHERE additional_information is not null and status = 'REFERRAL_STARTED';
