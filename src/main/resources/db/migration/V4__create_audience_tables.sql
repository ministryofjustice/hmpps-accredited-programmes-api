CREATE TABLE IF NOT EXISTS audience
(
    audience_id    uuid,
    -- 'value' is a reserved word, hence 'audience_value' with mapping to 'value' in entity
    audience_value text not null unique,
    primary key (audience_id)
);

CREATE TABLE IF NOT EXISTS course_audience
(
    course_id   uuid,
    audience_id uuid,
    primary key (course_id, audience_id),
    foreign key (course_id) references course (course_id),
    foreign key (audience_id) references audience (audience_id)
);