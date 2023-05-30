CREATE TABLE IF NOT EXISTS prerequisite
(
    prerequisite_id uuid,
    name            text not null,
    description     text not null,
    primary key (prerequisite_id)
);

create TABLE IF NOT EXISTS course_prerequisite
(
    course_id       uuid,
    prerequisite_id uuid,
    primary key (course_id, prerequisite_id),
    foreign key (course_id) references course (course_id),
    foreign key (prerequisite_id) references prerequisite (prerequisite_id)
);