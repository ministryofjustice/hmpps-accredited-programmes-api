
CREATE TABLE IF NOT EXISTS referral_status
(
    code TEXT primary key,
    description TEXT NOT NULL,
    colour TEXT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS referral_status_category
(
    code TEXT primary key,
    referral_status_code TEXT NOT NULL,
    description TEXT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    constraint referral_status_category_status_code_fk foreign key (referral_status_code) references referral_status (code)
);

CREATE TABLE IF NOT EXISTS referral_status_reason
(
    code TEXT primary key,
    referral_status_category_code TEXT NOT NULL,
    description TEXT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    constraint referral_status_reason_category_code_fk foreign key (referral_status_category_code) references referral_status_category (code)
);
