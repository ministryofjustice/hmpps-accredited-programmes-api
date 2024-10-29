

ALTER TABLE organisation ADD COLUMN gender TEXT;

update organisation SET gender = 'MALE' where code in ('WTI', 'SKI', 'ONI', 'FEI', 'LLI', 'PBI');
update organisation SET gender = 'FEMALE' where code in ('DHI', 'PFI');

DELETE from offering where organisation_id='GLI';
DELETE from organisation where CODE='GLI';

-- Male Prisons
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e001', 'WDI', 'Woodhill (HMP)', 'MALE');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e002', 'BSI', 'Brinsford (HMP & YOI)', 'MALE');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e003', 'LHI', 'Lindholme (HMP)', 'MALE');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e004', 'HII', 'Hindley (HMP & YOI)', 'MALE');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e005', 'RHI', 'Rye Hill (HMP)', 'MALE');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e006', 'SLI', 'Swaleside (HMP)', 'MALE');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e007', 'FMI', 'Feltham B (HMP & YOI)', 'MALE');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e008', 'DGI', 'Dovegate (HMP)', 'MALE');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e009', 'BAI', 'Belmarsh (HMP)', 'MALE');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e010', 'FKI', 'Frankland (HMP)', 'MALE');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e011', 'HOI', 'Hollesley Bay (HMP)', 'MALE');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e013', 'PCI', 'Pucklechurch (hmrc)', 'MALE');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e014', 'EYI', 'Elmley (HMP)', 'MALE');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e015', 'LGI', 'Lowdham Grange (HMP)', 'MALE');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e016', 'FWI', 'Five Wells (HMP)', 'MALE');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e018', 'HLI', 'Hull (HMP)', 'MALE');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e019', 'LFI', 'Lancaster Farms (HMP)', 'MALE');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e021', 'BRI', 'Bure (HMP)', 'MALE');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e023', 'UKI', 'Usk (HMP)', 'MALE');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e024', 'SHI', 'Stoke Heath (HMP & YOI)', 'MALE');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e025', 'ISI', 'Isis (HMP & YOI)', 'MALE');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e026', 'WLI', 'Wayland (HMP)', 'MALE');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e027', 'LWI', 'Lewes (HMP)', 'MALE');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e028', 'WMI', 'Wymott (HMP)', 'MALE');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e029', 'NLI', 'Northumberland (HMP)', 'MALE');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e030', 'DNI', 'Doncaster (HMP)', 'MALE');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e032', 'SNI', 'Swinfen Hall (HMP & YOI)', 'MALE');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e033', 'HPI', 'Highpoint (HMP)', 'MALE');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e034', 'MDI', 'Moorland (HMP & YOI)', 'MALE');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e035', 'FSI', 'Featherstone (HMP)', 'MALE');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e036', 'HHI', 'Holme House (HMP)', 'MALE');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e037', 'WHI', 'Woodhill (HMP & YOI)', 'MALE');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e038', 'HMI', 'Humber (HMP)', 'MALE');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e039', 'GTI', 'Gartree (HMP)', 'MALE');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e041', 'RSI', 'Risley (HMP)', 'MALE');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e042', 'MRI', 'Manchester (HMP)', 'MALE');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e043', 'WEI', 'Wealstun (HMP)', 'MALE');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e044', 'BNI', 'Bullingdon (HMP)', 'MALE');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e046', 'FBI', 'Forest Bank (HMP)', 'MALE');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e047', 'BCI', 'Buckley Hall (HMP)', 'MALE');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e048', 'GHI', 'Garth (HMP)', 'MALE');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e050', 'LPI', 'Liverpool (HMP)', 'MALE');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e051', 'DTI', 'Deerbolt (HMP & YOI)', 'MALE');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e052', 'AYI', 'Aylesbury (HMP)', 'MALE');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e053', 'WRI', 'Whitemoor (HMP)', 'MALE');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e054', 'BMI', 'Birmingham (HMP)', 'MALE');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e055', 'IWI', 'Isle of Wight (HMP)', 'MALE');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e057', 'PRI', 'Parc (HMP)', 'MALE');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e058', 'GMI', 'Guys Marsh (HMP)', 'MALE');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e059', 'PDI', 'Portland (HMP & YOI)', 'MALE');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e060', 'STI', 'Styal (HMP & YOI)', 'MALE');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e061', 'DAI', 'Dartmoor (HMP)', 'MALE');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e062', 'OWI', 'Oakwood (HMP)', 'MALE');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e063', 'ASI', 'Ashfield (HMP)', 'MALE');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e065', 'MTI', 'The Mount (HMP)', 'MALE');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e066', 'SFI', 'Stafford (HMP)', 'MALE');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e067', 'RNI', 'Ranby (HMP)', 'MALE');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e068', 'RCI', 'Rochester (HMP & YOI)', 'MALE');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e069', 'NHI', 'New Hall (HMP & YOI)', 'MALE');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e070', 'CWI', 'Channings Wood (HMP)', 'MALE');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e071', 'BWI', 'Berwyn (HMP)', 'MALE');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e072', 'FNI', 'Full Sutton (HMP)', 'MALE');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e073', 'LTI', 'Littlehey (HMP)', 'MALE');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e074', 'EEI', 'Erlestoke (HMP)', 'MALE');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e075', 'ACI', 'Altcourse (HMP & YOI)', 'MALE');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e076', 'CKI', 'Cookham Wood (HMYOI)', 'MALE');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e077', 'BLI', 'Bristol (HMP & YOI)', 'MALE');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e079', 'PYI', 'Parc (HMYOI)', 'MALE');

-- Female Prisons
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424f001', 'DWI', 'Downview (HMP & YOI)', 'FEMALE');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424f002', 'LNI', 'Low Newton (HMP & YOI)', 'FEMALE');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424f005', 'FHI', 'Foston Hall (HMP & YOI)', 'FEMALE');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424f006', 'DRI', 'Dorchester (HMP)', 'FEMALE');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424f007', 'PFI', 'Peterborough Female (HMP & YOI)', 'FEMALE');
