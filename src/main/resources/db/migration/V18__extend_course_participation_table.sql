ALTER TABLE course_participation
    ADD COLUMN year_completed integer;

ALTER TABLE course_participation
    RENAME COLUMN setting TO type;

ALTER TABLE course_participation
    ADD COLUMN location text;

ALTER TABLE course_participation
    ADD COLUMN created_by_username text NOT NULL default current_user;

ALTER TABLE course_participation
    ALTER COLUMN created_by_username SET NOT NULL;

ALTER TABLE course_participation
    ADD COLUMN created_date_time TIMESTAMP NOT NULL default NOW();

ALTER TABLE course_participation
    ALTER COLUMN created_date_time SET NOT NULL;

ALTER TABLE course_participation
    ADD COLUMN last_modified_by_username text;

ALTER TABLE course_participation
    ADD COLUMN last_modified_date_time TIMESTAMP;
