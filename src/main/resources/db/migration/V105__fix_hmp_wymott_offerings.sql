update offering set withdrawn = true
where organisation_id ='WMI'and course_id in (select c.course_id from course c where c.identifier ='NMS-AO');