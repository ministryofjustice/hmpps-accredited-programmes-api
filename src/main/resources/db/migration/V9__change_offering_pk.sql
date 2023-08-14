ALTER TABLE offering
    RENAME TO offering_old;

CREATE TABLE offering
(
    offering_id             uuid not null,
    course_id               uuid not null,
    organisation_id         text not null,
    contact_email           text not null,
    secondary_contact_email text,
    constraint offering_pk primary key (offering_id),
    constraint offering_course_fk foreign key (course_id) references course (course_id),
    constraint offering_business_key_unique unique (course_id, organisation_id)
);

INSERT INTO offering(offering_id, course_id, organisation_id, contact_email, secondary_contact_email)
SELECT offering_id, course_id, organisation_id, contact_email, secondary_contact_email
FROM offering_old;

