-- Insert HSP course if it doesn't exist
INSERT INTO course (course_id, name, description, alternate_name, identifier, withdrawn, audience, audience_colour, version)
SELECT '0198c765-788d-4531-9af3-f09b5501e70d',
       'Healthy Sex Programme',
       'Healthy Sex Programme (HSP) is a follow-up programme for men who have completed Kaizen, Becoming New Me Plus, Horizon, or New Me Strengths. It uses cognitive behavioural therapy and is aimed at men convicted of a sexual offence, or an offence with a sexual element.',
       'HSP',
       'HSP-SO',
       false,
       'Sexual offence',
       'orange',
       0
WHERE NOT EXISTS (SELECT 1
                  FROM course
                  WHERE name = 'Healthy Sex Programme');

-- Insert for ACI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
               withdrawn, referable, version)
SELECT '32557b41-44d6-4394-8d50-880c4c2258fd',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'ACI',
       'AC.AccreditedInterventions@sodexogov.co.uk',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'ACI');

-- Insert for ASI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT 'b9a5ff98-9b58-4fbd-b4af-50690535cee4',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'ASI',
       'Ashfield_programmes@serco.com',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'ASI');

-- Insert for AYI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT '9bd84076-f217-4bcc-a87a-1adddbb63ca6',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'AYI',
       'programmesadmin.aylesbury@justice.gov.uk',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'AYI');

-- Insert for BAI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT '15ce5eb7-6d85-400d-83b1-382088671253',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'BAI',
       'ProgrammesBelmarsh@justice.gov.uk',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'BAI');

-- Insert for BCI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT '6680c467-694f-4709-9215-e6cf2932fa49',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'BCI',
       'programmesbuckleyhall@justice.gov.uk',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'BCI');

-- Insert for BMI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT '43268c82-68fc-47ef-951d-ca353c211df6',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'BMI',
       'Programmes.Birmingham@justice.gov.uk',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'BMI');

-- Insert for BNI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT '29bf55a3-ca77-4bb5-8ce6-82adc52d9212',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'BNI',
       'Bullingdon.programmes@justice.gov.uk',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'BNI');

-- Insert for BRI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT '146ccfd9-d266-486b-9109-4242f115c531',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'BRI',
       'obpbure@justice.gov.uk',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'BRI');

-- Insert for BSI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT '141437b7-92dd-454a-b20b-1102e200dccb',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'BSI',
       'programmes.brinsford@justice.gov.uk',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'BSI');

-- Insert for BWI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT 'f1e795e6-cf38-452b-bef3-8fed93fe751e',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'BWI',
       'interventions.berwyn@justice.gov.uk',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'BWI');

-- Insert for CWI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT '13445540-4b93-454a-bd4a-e7476b8a24e9',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'CWI',
       'Programmes.channingswood@justice.gov.uk',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'CWI');

-- Insert for DAI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT '3cb6576c-a880-4d11-b77f-f8615f3df190',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'DAI',
       'programmes.dartmoor@justice.gov.uk',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'DAI');

-- Insert for DGI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT '655ef6d1-a306-4572-b15e-057b4bce97cb',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'DGI',
       'Dovegate.OBFacilitators@serco.com',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'DGI');

-- Insert for DHI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT '90265f28-da73-4dfd-9dec-e052d41ed9b2',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'DHI',
       'TBC',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'DHI');

-- Insert for DNI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT '6b746bba-eb2a-4dc8-ae96-c9ee460e4880',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'DNI',
       'Programmes.Doncaster@serco.com',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'DNI');

-- Insert for DRI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT 'f6042793-c895-4922-95ac-c55ae0648195',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'DRI',
       'TBC',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'DRI');

-- Insert for DTI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT '0d142fc5-3fdc-4f60-be84-94dc41666022',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'DTI',
       'omu.deerbolt@justice.gov.uk',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'DTI');

-- Insert for DWI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT '550cd4da-2fc7-4835-aed7-78a7d3ef7b3d',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'DWI',
       'TBC',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'DWI');

-- Insert for EEI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT '52eead85-6f04-4b14-843d-38f9574734a9',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'EEI',
       'kaizen.erlestoke@justice.gov.uk',
       'Programmes.erlestoke@justice.gov.uk',
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'EEI');

-- Insert for EYI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT 'ef2624cc-e53e-41c8-b214-598db81a7fa8',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'EYI',
       'Elmley.programmes@justice.gov.uk',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'EYI');

-- Insert for FBI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT '337f6f4c-d821-42a7-9c14-751185d895ae',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'FBI',
       'programmes@sodexogov.co.uk',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'FBI');

-- Insert for FEI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT '79d3abca-084e-4958-89fb-2e5e44b45070',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'FEI',
       'laura.gardner@serco.com',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'FEI');

-- Insert for FHI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT '3d395359-1130-4cf8-a19d-be0c4b4878a0',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'FHI',
       'programmesfostonhall@justice.gov.uk',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'FHI');

-- Insert for FKI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT '5e0f3e5a-9745-4815-85bc-d6a9315d048e',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'FKI',
       'Psychologyandprogrammes.frankland@justice.gov.uk',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'FKI');

-- Insert for FMI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT 'aacdd249-a761-4be3-a438-49ed0d485f73',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'FMI',
       'Programmes.Feltham@justice.gov.uk',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'FMI');

-- Insert for FNI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT '7c8e5302-0c50-4f70-a717-6666098d4733',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'FNI',
       'FullSuttonPsychologyDepartmentalmailbox@justice.gov.uk',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'FNI');

-- Insert for FSI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT '12919612-b505-4e63-b203-8207b4ea74eb',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'FSI',
       'Programmes.featherstone@justice.gov.uk',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'FSI');

-- Insert for FWI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT '7088bd24-00a2-4593-8140-e72bb99142a1',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'FWI',
       'Psychologyandprogrammes.frankland@justice.gov.uk',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'FWI');

-- Insert for GHI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT '35598434-a5ce-4c17-a32a-2f64d3edf49d',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'GHI',
       'ProgrammesGarth@justice.gov.uk',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'GHI');

-- Insert for GMI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT '8bb7afc0-41d9-453b-a8ee-e51fe6883622',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'GMI',
       'Progammes.guysmarsh@justice.gov.uk',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'GMI');

-- Insert for GTI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT '87d39beb-b055-459c-8e44-927dd05ed399',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'GTI',
       'PsychologyandProgrammes.Gartree@justice.gov.uk',
       'GartreeTherapeuticCommunity@justice.gov.uk',
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'GTI');

-- Insert for HHI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT 'e214997a-33f9-466b-a885-3afd25ab3e94',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'HHI',
       'programmes.holmehouse@justice.gov.uk',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'HHI');

-- Insert for HII if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT 'e7ab28fc-3710-4ae3-b614-a63b389a147b',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'HII',
       'programmeshindley@justice.gov.uk',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'HII');

-- Insert for HLI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT '70c837b1-d973-4583-a903-df043fc3247d',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'HLI',
       'programmeshull@justice.gov.uk',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'HLI');

-- Insert for HMI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT 'e9f0a1b2-c3d4-5e6f-7a8b-9c0d1e2f3a4b',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'HMI',
       'programmeshumber@justice.gov.uk',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'HMI');

-- Insert for HOI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT 'f0a1b2c3-d4e5-6f7a-8b9c-0d1e2f3a4b5c',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'HOI',
       'Programmes.highdown@justice.gov.uk',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'HOI');

-- Insert for HPI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT 'a1b2c3d4-e5f6-7a8b-9c0d-1e2f3a4b5c6d',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'HPI',
       'kaizen.highpoint@justice.gov.uk',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'HPI');

-- Insert for ISI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT 'b2c3d4e5-f6a7-8b9c-0d1e-2f3a4b5c6d7e',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'ISI',
       'Interventions.isis@justice.gov.uk',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'ISI');

-- Insert for IWI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT 'c3d4e5f6-a7b8-9c0d-1e2f-3a4b5c6d7e8f',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'IWI',
       'programmesiow@justice.gov.uk',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'IWI');

-- Insert for LFI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT 'd4e5f6a7-b8c9-0d1e-2f3a-4b5c6d7e8f9a',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'LFI',
       'programmes.lancasterfarms@justice.gov.uk',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'LFI');

-- Insert for LGI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT 'e5f6a7b8-c9d0-1e2f-3a4b-5c6d7e8f9a0b',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'LGI',
       'lowdham.programmes@justice.gov.uk',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'LGI');

-- Insert for LHI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT 'f6a7b8c9-d0e1-2f3a-4b5c-6d7e8f9a0b1c',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'LHI',
       'gemma.jarvis@justice.gov.uk',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'LHI');

-- Insert for LLI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT 'a7b8c9d0-e1f2-3a4b-5c6d-7e8f9a0b1c2d',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'LLI',
       'PsychologicalServices.LongLartin@justice.gov.uk',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'LLI');

-- Insert for LNI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT 'b8c9d0e1-f2a3-4b5c-6d7e-8f9a0b1c2d3e',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'LNI',
       'programmes.lownewton@justice.gov.uk',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'LNI');

-- Insert for LPI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT 'c9d0e1f2-a3b4-5c6d-7e8f-9a0b1c2d3e4f',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'LPI',
       'programmes.liverpool@justice.gov.uk',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'LPI');

-- Insert for LTI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT 'fc16269f-68de-4d9f-9f1d-35bc5d90d8f2',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'LTI',
       'programmeslittlehey@justice.gov.uk',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'LTI');

-- Insert for LWI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT '3fdf517c-e9a5-4c80-996f-432533b2da6f',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'LWI',
       'Programmes.lewes@justice.gov.uk',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'LWI');

-- Insert for MDI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT '8a0a19ee-d94d-4275-9de8-9e383c2c22ab',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'MDI',
       'ap-admin@digital.justice.gov.uk',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'MDI');

-- Insert for MRI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT 'a8060280-b10e-4d6a-8199-527e701aba93',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'MRI',
       'Programmes-manchester@justice.gov.uk',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'MRI');

-- Insert for MTI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT '9c6589b7-40a6-43d6-9fbc-bed74598e25e',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'MTI',
       'Programmesthemount@justice.gov.uk',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'MTI');

-- Insert for NHI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT '1bf9383d-aaac-4042-bf3a-279b739696eb',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'NHI',
       'Newhall.programmes@justice.gov.uk',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'NHI');

-- Insert for NLI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT '33248fbc-e88b-4fc4-9bb7-5408d647dbea',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'NLI',
       'obpunorthumberland@justice.gov.uk',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'NLI');

-- Insert for NMI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT 'c955f655-c681-4b58-b826-f552a61ee409',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'NMI',
       'TBC',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'NMI');

-- Insert for ONI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT '3f15eadb-4db7-4bb5-b899-b2a55c183ed8',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'ONI',
       'interventions.onley@justice.gov.uk',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'ONI');

-- Insert for OWI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT 'cb998ef0-0a2a-4b38-819e-4c84bd6212f4',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'OWI',
       'interventions.oakwood@uk.g4s.com',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'OWI');

-- Insert for PBI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT '9cf7fc75-a2c4-4713-b822-f0d046179186',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'PBI',
       'PBProgrammes@sodexogov.co.uk',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'PBI');

-- Insert for PCI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT '6fd17081-7fd9-4a84-849c-c99c370945a4',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'PCI',
       'ProgrammesReferralsRochester@justice.gov.uk',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'PCI');

-- Insert for PDI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT '77d3f735-9442-4bbd-babb-0de78869719a',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'PDI',
       'Programmes.portland@justice.gov.uk',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'PDI');

-- Insert for PFI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT '7950a208-93fc-4980-b0ac-aeabf583220f',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'PFI',
       'PBProgrammes@sodexogov.co.uk',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'PFI');

-- Insert for PRI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT '1d447fd1-be41-4d39-8dfd-942e5cf3d2ad',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'PRI',
       'anna.brunt@uk.g4s.com',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'PRI');

-- Insert for PYI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT 'e22d267d-6005-400a-b7b0-8bf401853d36',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'PYI',
       'chloe.rice@uk.g4s.com',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'PYI');

-- Insert for RCI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT 'ca2ee4c1-5e3a-4df1-a8e1-5d935018b32c',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'RCI',
       'ProgrammesReferralsRochester@justice.gov.uk',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'RCI');

-- Insert for RHI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT '1b00705b-463f-4818-938e-6b39a0291153',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'RHI',
       'programmesfunctionalmailbox@uk.g4s.com',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'RHI');

-- Insert for RNI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT 'a3438192-ef2f-499c-9a59-bec5cdf797af',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'RNI',
       'Programmes.Ranby@justice.gov.uk',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'RNI');

-- Insert for RSI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT 'af3b2fa1-5599-4872-8fe3-5c624f9a5e15',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'RSI',
       'programmesrisley@justice.gov.uk',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'RSI');

-- Insert for SFI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT '2e7ae748-7eae-4f63-8164-782b372ad145',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'SFI',
       'Programmesadmin.stafford@justice.gov.uk',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'SFI');

-- Insert for SHI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT 'b5476bb7-55c0-49aa-b5a1-08302032b86e',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'SHI',
       'Programmes.StokeHeath2@justice.gov.uk',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'SHI');

-- Insert for SKI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT '48e47751-00e9-4736-bc6e-9f0902ebbe46',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'SKI',
       'Programmes.Stocken@justice.gov.uk',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'SKI');

-- Insert for SLI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT '9936e848-3e3d-4113-bb54-b42c9744c78a',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'SLI',
       'ATBSheppeyCluster@justice.gov.uk',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'SLI');

-- Insert for SNI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT 'd70a8501-deef-49e1-a0de-7b54398ec726',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'SNI',
       'Programmes.Swinfen@justice.gov.uk',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'SNI');

-- Insert for STI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT 'ef0c565a-1516-415d-bb0c-33093f0e5a01',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'STI',
       'Programmes.Styal@justice.gov.uk',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'STI');

-- Insert for UKI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT '3081d9cc-be9b-4b8b-8ba4-91e450b8a144',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'UKI',
       'ProgrammesUSK@justice.gov.uk',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'UKI');

-- Insert for WDI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT '70b779e9-e140-4a60-a3c1-dd556844d695',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'WDI',
       'AssessmentInterventionsCentre.Wakefield@justice.gov.uk',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'WDI');

-- Insert for WEI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT '545d0af3-4af3-41a0-9e53-6a36b86600f3',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'WEI',
       'tspwealstun@justice.gov.uk',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'WEI');

-- Insert for WHI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT 'f6087d05-76a4-45c1-aa91-a1a796208dba',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'WHI',
       'programmes.woodhill@justice.gov.uk',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'WHI');

-- Insert for WLI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT '31134736-917e-42ff-845b-13f561d17ca7',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'WLI',
       'Programmes.Wayland@justice.gov.uk',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'WLI');

-- Insert for WMI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT 'c1f7ca8a-e477-4cff-9c8b-57d072987574',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'WMI',
       'programmeswymott@justice.gov.uk',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'WMI');

-- Insert for WRI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT 'df2d7ad8-6d57-4c97-9cb4-f01c7f5366d6',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'WRI',
       'PsychologyAdmin.Whitemoor@justice.gov.uk',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'WRI');

-- Insert for WSI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT '6afe1830-563b-45a2-93f1-ef6478fe0f5a',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'WSI',
       'TBC',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'WSI');

-- Insert for WTI if it doesn't exist
INSERT INTO offering (offering_id, course_id, organisation_id, contact_email, secondary_contact_email,
                      withdrawn, referable, version)
SELECT '8ae3e88b-5edc-40d4-af23-a5a889a5c3c2',
       (SELECT course_id FROM course where identifier = 'HSP-SO'),
       'WTI',
       'whattonprogrammes@justice.gov.uk',
       null,
       false,
       false,
       0
WHERE NOT EXISTS (SELECT 1
                  FROM offering
                  WHERE course_id = (SELECT course_id FROM course where identifier = 'HSP-SO')
                    AND organisation_id = 'WTI');
