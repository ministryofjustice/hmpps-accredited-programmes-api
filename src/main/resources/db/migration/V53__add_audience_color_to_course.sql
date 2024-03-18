ALTER TABLE COURSE
    ADD COLUMN audience_colour text;

update COURSE set audience_colour = 'turquoise' where audience = 'Extremism offence';
update COURSE set audience_colour = 'purple' where audience = 'Gang offence';
update COURSE set audience_colour = 'pink' where audience = 'General offence';
update COURSE set audience_colour = 'yellow' where audience = 'General violence offence';
update COURSE set audience_colour = 'green' where audience = 'Intimate partner violence offence';
update COURSE set audience_colour = 'orange' where audience = 'Sexual offence';