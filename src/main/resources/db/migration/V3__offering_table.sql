CREATE TABLE IF NOT EXISTS offering
(
    offering_id     uuid not null,
    course_id       uuid not null,
    organisation_id text not null,
    contact_email   text not null,
    primary key (course_id, organisation_id),
    foreign key (course_id) references course (course_id)
)