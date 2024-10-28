

ALTER TABLE organisation ADD COLUMN gender CHAR (1);

update organisation SET gender = 'M' where code in ('WTI', 'SKI', 'ONI', 'FEI', 'LLI', 'PBI');
update organisation SET gender = 'F' where code in ('DHI', 'PFI');

-- Male Prisons
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e001', 'WDI', 'Woodhill (HMP)', 'M');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e002', 'BSI', 'Brinsford (HMP & YOI)', 'M');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e003', 'LHI', 'Lindholme (HMP)', 'M');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e004', 'HII', 'Hindley (HMP & YOI)', 'M');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e005', 'RHI', 'Rye Hill (HMP)', 'M');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e006', 'SLI', 'Swaleside (HMP)', 'M');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e007', 'FMI', 'Feltham B (HMP & YOI)', 'M');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e008', 'DGI', 'Dovegate (HMP)', 'M');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e009', 'BAI', 'Belmarsh (HMP)', 'M');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e010', 'FKI', 'Frankland (HMP)', 'M');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e011', 'HOI', 'Hollesley Bay (HMP)', 'M');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e013', 'PCI', 'Pucklechurch (hmrc)', 'M');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e014', 'EYI', 'Elmley (HMP)', 'M');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e015', 'LGI', 'Lowdham Grange (HMP)', 'M');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e016', 'FWI', 'Five Wells (HMP)', 'M');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e018', 'HLI', 'Hull (HMP)', 'M');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e019', 'LFI', 'Lancaster Farms (HMP)', 'M');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e021', 'BRI', 'Bure (HMP)', 'M');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e023', 'UKI', 'Usk (HMP)', 'M');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e024', 'SHI', 'Stoke Heath (HMP & YOI)', 'M');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e025', 'ISI', 'Isis (HMP & YOI)', 'M');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e026', 'WLI', 'Wayland (HMP)', 'M');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e027', 'LWI', 'Lewes (HMP)', 'M');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e028', 'WMI', 'Wymott (HMP)', 'M');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e029', 'NLI', 'Northumberland (HMP)', 'M');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e030', 'DNI', 'Doncaster (HMP)', 'M');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e032', 'SNI', 'Swinfen Hall (HMP & YOI)', 'M');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e033', 'HPI', 'Highpoint (HMP)', 'M');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e034', 'MDI', 'Moorland (HMP & YOI)', 'M');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e035', 'FSI', 'Featherstone (HMP)', 'M');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e036', 'HHI', 'Holme House (HMP)', 'M');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e037', 'WHI', 'Woodhill (HMP & YOI)', 'M');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e038', 'HMI', 'Humber (HMP)', 'M');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e039', 'GTI', 'Gartree (HMP)', 'M');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e041', 'RSI', 'Risley (HMP)', 'M');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e042', 'MRI', 'Manchester (HMP)', 'M');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e043', 'WEI', 'Wealstun (HMP)', 'M');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e044', 'BNI', 'Bullingdon (HMP)', 'M');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e046', 'FBI', 'Forest Bank (HMP)', 'M');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e047', 'BCI', 'Buckley Hall (HMP)', 'M');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e048', 'GHI', 'Garth (HMP)', 'M');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e050', 'LPI', 'Liverpool (HMP)', 'M');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e051', 'DTI', 'Deerbolt (HMP & YOI)', 'M');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e052', 'AYI', 'Aylesbury (HMP)', 'M');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e053', 'WRI', 'Whitemoor (HMP)', 'M');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e054', 'BMI', 'Birmingham (HMP)', 'M');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e055', 'IWI', 'Isle of Wight (HMP)', 'M');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e057', 'PRI', 'Parc (HMP)', 'M');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e058', 'GMI', 'Guys Marsh (HMP)', 'M');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e059', 'PDI', 'Portland (HMP & YOI)', 'M');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e060', 'STI', 'Styal (HMP & YOI)', 'M');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e061', 'DAI', 'Dartmoor (HMP)', 'M');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e062', 'OWI', 'Oakwood (HMP)', 'M');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e063', 'ASI', 'Ashfield (HMP)', 'M');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e065', 'MTI', 'The Mount (HMP)', 'M');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e066', 'SFI', 'Stafford (HMP)', 'M');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e067', 'RNI', 'Ranby (HMP)', 'M');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e068', 'RCI', 'Rochester (HMP & YOI)', 'M');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e069', 'NHI', 'New Hall (HMP & YOI)', 'M');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e070', 'CWI', 'Channings Wood (HMP)', 'M');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e071', 'BWI', 'Berwyn (HMP)', 'M');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e072', 'FNI', 'Full Sutton (HMP)', 'M');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e073', 'LTI', 'Littlehey (HMP)', 'M');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e074', 'EEI', 'Erlestoke (HMP)', 'M');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e075', 'ACI', 'Altcourse (HMP & YOI)', 'M');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424e076', 'CKI', 'Cookham Wood (HMYOI)', 'M');
-- Female Prisons
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424f001', 'DWI', 'Downview (HMP & YOI)', 'F');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424f002', 'LNI', 'Low Newton (HMP & YOI)', 'F');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424f005', 'FHI', 'Foston Hall (HMP & YOI)', 'F');
INSERT INTO ORGANISATION (organisation_id, code, name, gender) VALUES ('c177c813-8178-4822-b915-25d9e424f006', 'DRI', 'Dorchester (HMP)', 'F');

ALTER TABLE organisation
    ADD CONSTRAINT organisation_code_unique UNIQUE (code);

ALTER TABLE offering
    ADD CONSTRAINT offering_organisation_fk
        FOREIGN KEY (organisation_id) REFERENCES organisation(code);


