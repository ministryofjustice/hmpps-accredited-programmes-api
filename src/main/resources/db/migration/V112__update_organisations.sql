

INSERT INTO enabled_organisation (code, description) VALUES ('HOI', 'High Down (HMP & YOI)');

DELETE FROM ORGANISATION WHERE CODE = 'DRI' and name = 'Dorchester (HMP)';
DELETE FROM enabled_organisation WHERE CODE = 'DRI' and description = 'Dorchester (HMP)';

DELETE FROM ORGANISATION WHERE CODE = 'PCI' and name = 'Pucklechurch (hmrc)';
DELETE FROM enabled_organisation WHERE CODE = 'PCI' and description = 'Pucklechurch (hmrc)';

INSERT INTO enabled_organisation (code, description) VALUES ('GNI', 'Grendon (HMP)');
INSERT INTO enabled_organisation (code, description) VALUES ('SDI', 'Send (HMP)');
INSERT INTO enabled_organisation (code, description) VALUES ('WII', 'Warren Hill (HMP)');






DRI,Dorchester (HMP)
