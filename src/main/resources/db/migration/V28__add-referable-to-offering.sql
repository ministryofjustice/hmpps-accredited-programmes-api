ALTER TABLE offering
    ADD COLUMN referable BOOLEAN NOT NULL DEFAULT true;

UPDATE offering
SET referable = (
    SELECT c.referable
    FROM course c
    WHERE c.course_id = offering.course_id
);