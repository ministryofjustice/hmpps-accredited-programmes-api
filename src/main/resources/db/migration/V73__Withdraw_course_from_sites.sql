
-- Oakwood - OWI
UPDATE offering set withdrawn=true where organisation_id='OWI' AND course_id IN (select course_id from course where name='Kaizen');

-- Dovegate : DGI
UPDATE offering set withdrawn=true where organisation_id='DGI' AND course_id IN (select course_id from course where name='Kaizen' and list_display_name='Kaizen: intimate partner violence');

-- Lancaster : LAI
UPDATE offering set withdrawn=true where organisation_id='LAI' AND course_id IN (select course_id from course where name='Building Better Relationships');

-- Northumberland : NLI
UPDATE offering set withdrawn=true where organisation_id='NLI' AND course_id IN (select course_id from course where name='Kaizen' and list_display_name='Kaizen: intimate partner violence');

UPDATE offering set withdrawn=true where organisation_id='NLI' AND course_id IN (select course_id from course where name='Becoming New Me Plus');
