

-- High Intensity - sexual offence

INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email, referable, withdrawn) VALUES
('3f8c2b1e-4b8d-4a6e-8b1e-1b2c3d4e5f6a', 'f925d6c5-d1be-4246-88ba-91bf77dbd101', 'RHI', 'programmesfunctionalmailbox@uk.g4s.com', NULL, true, false),
('7a8b9c0d-1e2f-3a4b-5c6d-7e8f9a0b1c2d', 'f925d6c5-d1be-4246-88ba-91bf77dbd101', 'FWI', 'stephen.gravett@uk.g4s.com', NULL, true, false);

-- Moderate Intensity - sexual offence
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email, referable, withdrawn) VALUES
('9d0e1f2a-3b4c-5d6e-7f8a-9b0c1d2e3f4a', 'f925d6c5-d1be-4246-88ba-91bf77dbd201', 'RHI', 'whattonprogrammes@justice.gov.uk', NULL, true, false),
('5a6b7c8d-9e0f-1a2b-3c4d-5e6f7a8b9c0d', 'f925d6c5-d1be-4246-88ba-91bf77dbd201', 'FWI', 'whattonprogrammes@justice.gov.uk', NULL, true, false);


-- Moderate Intensity - general offence
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email, referable, withdrawn) VALUES
('1e2f3a4b-5c6d-7e8f-9a0b-1c2d3e4f5a6b', 'f925d6c5-d1be-4246-88ba-91bf77dbd202', 'SKI', 'Programmes.Stocken@justice.gov.uk', NULL, true, false),
('4b5c6d7e-8f9a-0b1c-2d3e-4f5a6b7c8d9e', 'f925d6c5-d1be-4246-88ba-91bf77dbd202', 'HHI', 'programmes.holmehouse@justice.gov.uk', NULL, true, false),
('2a3b4c5d-6e7f-8a9b-0c1d-2e3f4a5b6c7d', 'f925d6c5-d1be-4246-88ba-91bf77dbd202', 'HMI', 'programmeshumber@justice.gov.uk', NULL, true, false);

-- High Intensity - general offence
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email, referable, withdrawn) VALUES
('8f9a0b1c-2d3e-4f5a-6b7c-8d9e0a1b2c3d', 'f925d6c5-d1be-4246-88ba-91bf77dbd102', 'SKI', 'Programmes.Stocken@justice.gov.uk', NULL, true, false),
('6e7f8a9b-0c1d-2e3f-4a5b-6c7d8e9f0a1b', 'f925d6c5-d1be-4246-88ba-91bf77dbd102', 'HHI', 'programmes.holmehouse@justice.gov.uk', NULL, true, false),
('0b1c2d3e-4f5a-6b7c-8d9e-0a1b2c3d4e5f', 'f925d6c5-d1be-4246-88ba-91bf77dbd102', 'HMI', 'programmeshumber@justice.gov.uk', NULL, true, false);


UPDATE offering set contact_email='Programmes.Stocken@justice.gov.uk' where organisation_id='SKI';