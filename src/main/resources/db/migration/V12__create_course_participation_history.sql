CREATE TABLE course_participation_history
(
    course_participation_history_id UUID NOT NULL PRIMARY KEY,
    prison_number                   TEXT NOT NULL,
    course_id                       UUID,
    other_course_name               TEXT,
    year_started                    integer,
    setting                         text not null,
    outcome_status                  text,
    outcome_detail                  text,
    constraint course_participation_history_course_fk foreign key (course_id) references course (course_id)
);