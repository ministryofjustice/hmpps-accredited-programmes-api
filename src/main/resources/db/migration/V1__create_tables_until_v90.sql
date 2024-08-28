
create table if not exists flyway_schema_history
(
    installed_rank integer                 not null
    constraint flyway_schema_history_pk
    primary key,
    version        varchar(50),
    description    varchar(200)            not null,
    type           varchar(20)             not null,
    script         varchar(1000)           not null,
    checksum       integer,
    installed_by   varchar(100)            not null,
    installed_on   timestamp default now() not null,
    execution_time integer                 not null,
    success        boolean                 not null
    );


create index if not exists flyway_schema_history_s_idx
    on flyway_schema_history (success);

create table if not exists course
(
    course_id         uuid                     not null
    primary key,
    name              text                     not null,
    description       text,
    alternate_name    text,
    identifier        text    default ''::text not null
    constraint course_identifier_unique
    unique,
    withdrawn         boolean default false    not null,
    audience          text,
    audience_colour   text,
    list_display_name text,
    version           bigint  default 0        not null
);


create table if not exists prerequisite
(
    course_id   uuid not null
    references course,
    name        text not null,
    description text not null,
    primary key (course_id, name, description)
    );


create table if not exists offering_old
(
    offering_id             uuid not null,
    course_id               uuid not null
    constraint offering_course_id_fkey
    references course,
    organisation_id         text not null,
    contact_email           text not null,
    secondary_contact_email text,
    constraint offering_pkey
    primary key (course_id, organisation_id)
    );

create table if not exists offering
(
    offering_id             uuid                  not null
    constraint offering_pk
    primary key,
    course_id               uuid                  not null
    constraint offering_course_fk
    references course,
    organisation_id         text                  not null,
    contact_email           text                  not null,
    secondary_contact_email text,
    withdrawn               boolean default false not null,
    referable               boolean default true  not null,
    version                 bigint  default 0     not null,
    constraint offering_business_key_unique
    unique (course_id, organisation_id)
    );


create table if not exists course_participation
(
    course_participation_id   uuid                           not null
    constraint course_participation_history_pkey
    primary key,
    prison_number             text                           not null,
    course_id                 uuid
    constraint course_participation_history_course_fk
    references course,
    other_course_name         text,
    year_started              integer,
    source                    text,
    type                      text,
    outcome_status            text,
    outcome_detail            text,
    year_completed            integer,
    location                  text,
    created_by_username       text      default CURRENT_USER not null,
    created_date_time         timestamp default now()        not null,
    last_modified_by_username text,
    last_modified_date_time   timestamp,
    detail                    text,
    course_name               text
    );


create table if not exists referrer_user
(
    referrer_username text not null
    constraint referrer_user_username_pk
    primary key
);


create table if not exists person
(
    person_id                  uuid             not null
    primary key,
    prison_number              text             not null
    unique,
    forename                   text             not null,
    surname                    text             not null,
    conditional_release_date   date,
    parole_eligibility_date    date,
    tariff_expiry_date         date,
    earliest_release_date      date,
    earliest_release_date_type text,
    indeterminate_sentence     boolean,
    non_dto_release_date_type  text,
    sentence_type              text,
    location                   text,
    gender                     text,
    version                    bigint default 0 not null
);


create index if not exists idx_person_prison_number
    on person (prison_number);

create index if not exists idx_audit_prison_number
    on person (prison_number);

create table if not exists organisation
(
    organisation_id uuid not null
    primary key,
    code            text not null,
    name            text not null
);


create index if not exists idx_organisation_code
    on organisation (code);

create table if not exists referral_status
(
    code              text                 not null
    primary key,
    description       text                 not null,
    colour            text                 not null,
    active            boolean default true not null,
    draft             boolean default false,
    closed            boolean default false,
    has_notes         boolean default true,
    has_confirmation  boolean default false,
    hint_text         text,
    confirmation_text text,
    hold              boolean default false,
    release           boolean default false,
    default_order     smallint,
    notes_optional    boolean default false
);


create table if not exists referral
(
    referral_id                    uuid                                     not null
    constraint referral_pk
    primary key,
    offering_id                    uuid                                     not null
    constraint referral_offering_fk
    references offering,
    prison_number                  text                                     not null,
    oasys_confirmed                boolean default false                    not null,
    status                         text    default 'REFERRAL_STARTED'::text not null
    constraint r_status_fk
    references referral_status,
    has_reviewed_programme_history boolean default false                    not null,
    additional_information         text,
    submitted_on                   timestamp,
    referrer_username              text
    constraint referrer_user_username_fk
    references referrer_user,
    deleted                        boolean default false                    not null,
    version                        bigint  default 0                        not null
);


create table if not exists referral_status_category
(
    code                 text                 not null
    primary key,
    referral_status_code text                 not null
    constraint referral_status_category_status_code_fk
    references referral_status,
    description          text                 not null,
    active               boolean default true not null
);


create table if not exists referral_status_reason
(
    code                          text                 not null
    primary key,
    referral_status_category_code text                 not null
    constraint referral_status_reason_category_code_fk
    references referral_status_category,
    description                   text                 not null,
    active                        boolean default true not null,
    deselect_open                 boolean default true not null
);

create table if not exists audit_record
(
    audit_record_id      uuid                                not null
    primary key,
    referral_id          uuid,
    prison_number        text                                not null,
    referrer_username    text,
    referral_status_from text,
    referral_status_to   text,
    course_id            uuid,
    course_name          text,
    course_location      text,
    audit_action         text                                not null,
    audit_username       text                                not null,
    audit_date_time      timestamp default CURRENT_TIMESTAMP not null
);

create table if not exists referral_status_history
(
    status_history_id       uuid             not null
    primary key,
    referral_id             uuid             not null
    constraint rs_referral_fk
    references referral,
    status                  text             not null
    constraint rs_status_fk
    references referral_status,
    previous_status         text
    constraint rs_previous_status_fk
    references referral_status,
    category                text
    constraint rs_category_fk
    references referral_status_category,
    reason                  text
    constraint rs_reason_fk
    references referral_status_reason,
    notes                   text,
    status_start_date       timestamp,
    status_end_date         timestamp,
    duration_at_this_status bigint,
    username                text,
    version                 bigint default 0 not null
);


create table if not exists referral_status_transitions
(
    referral_status_transition_id uuid    not null
    primary key,
    pt_user                       boolean not null,
    pom_user                      boolean not null,
    transition_from_status        text    not null
    constraint from_status_fk
    references referral_status,
    transition_to_status          text    not null
    constraint to_status_fk
    references referral_status,
    hint_text                     text,
    description                   text,
    primary_heading               text,
    primary_description           text,
    secondary_heading             text,
    secondary_description         text,
    warning_text                  text
);


create table if not exists enabled_organisation
(
    code        text not null
    primary key,
    description text not null
);

create table if not exists audience
(
    audience_id uuid         not null
    primary key,
    name        varchar(255) not null,
    colour      varchar(255),
    constraint unique_name_colour
    unique (name, colour)
    );

create table if not exists pni_rule
(
    rule_id          uuid not null
    primary key,
    overall_need     text not null,
    overall_risk     text not null,
    combined_pathway text not null,
    constraint unique_need_risk
    unique (overall_need, overall_risk)
    );

create table if not exists pni_result
(
    pni_result_id                   uuid not null
    primary key,
    referral_id                     uuid
    constraint r_pni_result_referral_fk
    references referral,
    prison_number                   text not null,
    crn                             text,
    oasys_assessment_id             bigint,
    oasys_assessment_completed_date timestamp,
    programme_pathway               text,
    needs_classification            text,
    overall_needs_score             smallint,
    risk_classification             text,
    pni_assessment_date             timestamp,
    pni_valid                       boolean,
    pni_result_json                 text
);

create or replace view referral_view
            (referral_id, prison_number, forename, surname, conditional_release_date, parole_eligibility_date,
             tariff_expiry_date, earliest_release_date, earliest_release_date_type, non_dto_release_date_type, location,
             organisation_id, organisation_name, status, status_description, status_colour, referrer_username,
             course_name, audience, submitted_on, sentence_type, list_display_name)
as
SELECT r.referral_id,
       r.prison_number,
       p.forename,
       p.surname,
       p.conditional_release_date,
       p.parole_eligibility_date,
       p.tariff_expiry_date,
       p.earliest_release_date,
       p.earliest_release_date_type,
       p.non_dto_release_date_type,
       p.location,
       o.organisation_id,
       org.name       AS organisation_name,
       r.status,
       rs.description AS status_description,
       rs.colour      AS status_colour,
       r.referrer_username,
       c.name         AS course_name,
       c.audience,
       r.submitted_on,
       p.sentence_type,
       CASE
           WHEN c.list_display_name IS NOT NULL THEN c.list_display_name
           ELSE c.name
           END        AS list_display_name
FROM referral r
         LEFT JOIN person p ON r.prison_number = p.prison_number
         LEFT JOIN offering o ON o.offering_id = r.offering_id
         LEFT JOIN course c ON c.course_id = o.course_id
         LEFT JOIN organisation org ON org.code = o.organisation_id
         LEFT JOIN referral_status rs ON rs.code = r.status
WHERE r.deleted = false;


