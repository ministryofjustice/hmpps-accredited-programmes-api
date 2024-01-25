ALTER TABLE course
    ADD COLUMN audience TEXT;


UPDATE course c
SET audience = (
    SELECT a.audience_value
    FROM audience a
             JOIN course_audience ca ON a.audience_id = ca.audience_id
    WHERE ca.course_id = c.course_id
    LIMIT 1
);