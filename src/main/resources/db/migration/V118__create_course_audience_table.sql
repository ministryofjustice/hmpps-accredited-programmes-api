CREATE TABLE course_audience
(
    course_id   UUID NOT NULL REFERENCES course (course_id) ON DELETE CASCADE,
    audience_id UUID NOT NULL REFERENCES audience (audience_id) ON DELETE CASCADE,
    PRIMARY KEY (course_id, audience_id)
);
