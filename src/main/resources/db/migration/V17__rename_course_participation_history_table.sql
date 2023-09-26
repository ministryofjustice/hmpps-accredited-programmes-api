ALTER TABLE course_participation_history
    RENAME TO course_participation;

ALTER TABLE course_participation
    RENAME COLUMN course_participation_history_id TO course_participation_id;