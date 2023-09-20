ALTER TABLE course
ALTER COLUMN identifier SET NOT NULL;

ALTER TABLE course
ADD CONSTRAINT course_identifier_unique UNIQUE (identifier);