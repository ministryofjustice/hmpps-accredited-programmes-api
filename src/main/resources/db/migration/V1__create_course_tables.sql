CREATE TABLE IF NOT EXISTS course
(
    course_id   uuid,
    name        text not null,
    description text,
    type        text not null,
    primary key (course_id)
);
