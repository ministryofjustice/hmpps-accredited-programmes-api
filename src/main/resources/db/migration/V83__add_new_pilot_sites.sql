

UPDATE enabled_organisation SET description='Whatton (HMP)' where code = 'WTI';
UPDATE enabled_organisation SET description='Onley (HMP)' where code = 'ONI';
UPDATE enabled_organisation SET description='Stocken (HMP)' where code = 'SKI';

INSERT INTO enabled_organisation (code, description) VALUES ('FEI', 'Fosse Way (HMP & YOI)');
