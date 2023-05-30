CREATE TABLE IF NOT EXISTS prerequisite
(
    course_id       uuid not null,
    name            text not null,
    description     text not null,
    primary key (course_id, name, description),
    foreign key (course_id) references course(course_id)
);
