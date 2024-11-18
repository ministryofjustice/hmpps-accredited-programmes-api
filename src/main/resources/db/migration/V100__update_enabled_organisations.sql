INSERT INTO enabled_organisation (code, description)
SELECT 'ACI', 'Altcourse (HMP & YOI)'
WHERE NOT EXISTS (
    SELECT 1 FROM enabled_organisation WHERE code = 'ACI'
);


INSERT INTO enabled_organisation (code, description)
SELECT 'ASI', 'Ashfield (HMP)'
WHERE NOT EXISTS (SELECT 1
                  FROM enabled_organisation
                  WHERE code = 'ASI');

INSERT INTO enabled_organisation (code, description)
SELECT 'AYI', 'Aylesbury (HMP)'
WHERE NOT EXISTS (SELECT 1
                  FROM enabled_organisation
                  WHERE code = 'AYI');

INSERT INTO enabled_organisation (code, description)
SELECT 'BAI', 'Belmarsh (HMP)'
WHERE NOT EXISTS (SELECT 1
                  FROM enabled_organisation
                  WHERE code = 'BAI');

INSERT INTO enabled_organisation (code, description)
SELECT 'BCI', 'Buckley Hall (HMP)'
WHERE NOT EXISTS (SELECT 1
                  FROM enabled_organisation
                  WHERE code = 'BCI');

INSERT INTO enabled_organisation (code, description)
SELECT 'BMI', 'Birmingham (HMP)'
WHERE NOT EXISTS (SELECT 1
                  FROM enabled_organisation
                  WHERE code = 'BMI');

INSERT INTO enabled_organisation (code, description)
SELECT 'BNI', 'Bullingdon (HMP)'
WHERE NOT EXISTS (SELECT 1
                  FROM enabled_organisation
                  WHERE code = 'BNI');

INSERT INTO enabled_organisation (code, description)
SELECT 'BRI', 'Bure (HMP)'
WHERE NOT EXISTS (SELECT 1
                  FROM enabled_organisation
                  WHERE code = 'BRI');

INSERT INTO enabled_organisation (code, description)
SELECT 'BSI', 'Brinsford (HMP & YOI)'
WHERE NOT EXISTS (SELECT 1
                  FROM enabled_organisation
                  WHERE code = 'BSI');

INSERT INTO enabled_organisation (code, description)
SELECT 'BWI', 'Berwyn (HMP)'
WHERE NOT EXISTS (SELECT 1
                  FROM enabled_organisation
                  WHERE code = 'BWI');

INSERT INTO enabled_organisation (code, description)
SELECT 'CWI', 'Channings Wood (HMP)'
WHERE NOT EXISTS (SELECT 1
                  FROM enabled_organisation
                  WHERE code = 'CWI');

INSERT INTO enabled_organisation (code, description)
SELECT 'DAI', 'Dartmoor (HMP)'
WHERE NOT EXISTS (SELECT 1
                  FROM enabled_organisation
                  WHERE code = 'DAI');

INSERT INTO enabled_organisation (code, description)
SELECT 'DGI', 'Dovegate (HMP)'
WHERE NOT EXISTS (SELECT 1
                  FROM enabled_organisation
                  WHERE code = 'DGI');

INSERT INTO enabled_organisation (code, description)
SELECT 'DHI', 'Drake Hall (HMP & YOI)'
WHERE NOT EXISTS (SELECT 1
                  FROM enabled_organisation
                  WHERE code = 'DHI');

INSERT INTO enabled_organisation (code, description)
SELECT 'DNI', 'Doncaster (HMP)'
WHERE NOT EXISTS (SELECT 1
                  FROM enabled_organisation
                  WHERE code = 'DNI');

INSERT INTO enabled_organisation (code, description)
SELECT 'DRI', 'Dorchester (HMP)'
WHERE NOT EXISTS (SELECT 1
                  FROM enabled_organisation
                  WHERE code = 'DRI');

INSERT INTO enabled_organisation (code, description)
SELECT 'DTI', 'Deerbolt (HMP & YOI)'
WHERE NOT EXISTS (SELECT 1
                  FROM enabled_organisation
                  WHERE code = 'DTI');

INSERT INTO enabled_organisation (code, description)
SELECT 'DWI', 'Downview (HMP & YOI)'
WHERE NOT EXISTS (SELECT 1
                  FROM enabled_organisation
                  WHERE code = 'DWI');

INSERT INTO enabled_organisation (code, description)
SELECT 'EEI', 'Erlestoke (HMP)'
WHERE NOT EXISTS (SELECT 1
                  FROM enabled_organisation
                  WHERE code = 'EEI');

INSERT INTO enabled_organisation (code, description)
SELECT 'EYI', 'Elmley (HMP)'
WHERE NOT EXISTS (SELECT 1
                  FROM enabled_organisation
                  WHERE code = 'EYI');

INSERT INTO enabled_organisation (code, description)
SELECT 'FBI', 'Forest Bank (HMP)'
WHERE NOT EXISTS (SELECT 1
                  FROM enabled_organisation
                  WHERE code = 'FBI');

INSERT INTO enabled_organisation (code, description)
SELECT 'FEI', 'Fosse Way (HMP & YOI)'
WHERE NOT EXISTS (SELECT 1
                  FROM enabled_organisation
                  WHERE code = 'FEI');

INSERT INTO enabled_organisation (code, description)
SELECT 'FHI', 'Foston Hall (HMP & YOI)'
WHERE NOT EXISTS (SELECT 1
                  FROM enabled_organisation
                  WHERE code = 'FHI');

INSERT INTO enabled_organisation (code, description)
SELECT 'FKI', 'Frankland (HMP)'
WHERE NOT EXISTS (SELECT 1
                  FROM enabled_organisation
                  WHERE code = 'FKI');

INSERT INTO enabled_organisation (code, description)
SELECT 'FMI', 'Feltham B (HMP & YOI)'
WHERE NOT EXISTS (SELECT 1
                  FROM enabled_organisation
                  WHERE code = 'FMI');

INSERT INTO enabled_organisation (code, description)
SELECT 'FNI', 'Full Sutton (HMP)'
WHERE NOT EXISTS (SELECT 1
                  FROM enabled_organisation
                  WHERE code = 'FNI');

INSERT INTO enabled_organisation (code, description)
SELECT 'FSI', 'Featherstone (HMP)'
WHERE NOT EXISTS (SELECT 1
                  FROM enabled_organisation
                  WHERE code = 'FSI');

INSERT INTO enabled_organisation (code, description)
SELECT 'FWI', 'Five Wells (HMP)'
WHERE NOT EXISTS (SELECT 1
                  FROM enabled_organisation
                  WHERE code = 'FWI');

INSERT INTO enabled_organisation (code, description)
SELECT 'GHI', 'Garth (HMP)'
WHERE NOT EXISTS (SELECT 1
                  FROM enabled_organisation
                  WHERE code = 'GHI');

INSERT INTO enabled_organisation (code, description)
SELECT 'GMI', 'Guys Marsh (HMP)'
WHERE NOT EXISTS (SELECT 1
                  FROM enabled_organisation
                  WHERE code = 'GMI');

INSERT INTO enabled_organisation (code, description)
SELECT 'GTI', 'Gartree (HMP)'
WHERE NOT EXISTS (SELECT 1
                  FROM enabled_organisation
                  WHERE code = 'GTI');

INSERT INTO enabled_organisation (code, description)
SELECT 'HHI', 'Holme House (HMP)'
WHERE NOT EXISTS (SELECT 1
                  FROM enabled_organisation
                  WHERE code = 'HHI');

INSERT INTO enabled_organisation (code, description)
SELECT 'HII', 'Hindley (HMP & YOI)'
WHERE NOT EXISTS (SELECT 1
                  FROM enabled_organisation
                  WHERE code = 'HII');

INSERT INTO enabled_organisation (code, description)
SELECT 'HLI', 'Hull (HMP)'
WHERE NOT EXISTS (SELECT 1
                  FROM enabled_organisation
                  WHERE code = 'HLI');

INSERT INTO enabled_organisation (code, description)
SELECT 'HMI', 'Humber (HMP)'
WHERE NOT EXISTS (SELECT 1
                  FROM enabled_organisation
                  WHERE code = 'HMI');

INSERT INTO enabled_organisation (code, description)
SELECT 'HOI', 'Hollesley Bay (HMP)'
WHERE NOT EXISTS (SELECT 1
                  FROM enabled_organisation
                  WHERE code = 'HOI');

INSERT INTO enabled_organisation (code, description)
SELECT 'HPI', 'Highpoint (HMP)'
WHERE NOT EXISTS (SELECT 1
                  FROM enabled_organisation
                  WHERE code = 'HPI');

INSERT INTO enabled_organisation (code, description)
SELECT 'ISI', 'Isis (HMP & YOI)'
WHERE NOT EXISTS (SELECT 1
                  FROM enabled_organisation
                  WHERE code = 'ISI');

INSERT INTO enabled_organisation (code, description)
SELECT 'IWI', 'Isle of Wight (HMP)'
WHERE NOT EXISTS (SELECT 1
                  FROM enabled_organisation
                  WHERE code = 'IWI');

INSERT INTO enabled_organisation (code, description)
SELECT 'LFI', 'Lancaster Farms (HMP)'
WHERE NOT EXISTS (SELECT 1
                  FROM enabled_organisation
                  WHERE code = 'LFI');

INSERT INTO enabled_organisation (code, description)
SELECT 'LGI', 'Lowdham Grange (HMP)'
WHERE NOT EXISTS (SELECT 1
                  FROM enabled_organisation
                  WHERE code = 'LGI');

INSERT INTO enabled_organisation (code, description)
SELECT 'LHI', 'Lindholme (HMP)'
WHERE NOT EXISTS (SELECT 1
                  FROM enabled_organisation
                  WHERE code = 'LHI');

INSERT INTO enabled_organisation (code, description)
SELECT 'LLI', 'Long Lartin (HMP)'
WHERE NOT EXISTS (SELECT 1
                  FROM enabled_organisation
                  WHERE code = 'LLI');

INSERT INTO enabled_organisation (code, description)
SELECT 'LNI', 'Low Newton (HMP & YOI)'
WHERE NOT EXISTS (SELECT 1
                  FROM enabled_organisation
                  WHERE code = 'LNI');

INSERT INTO enabled_organisation (code, description)
SELECT 'LPI', 'Liverpool (HMP)'
WHERE NOT EXISTS (SELECT 1
                  FROM enabled_organisation
                  WHERE code = 'LPI');

INSERT INTO enabled_organisation (code, description)
SELECT 'LTI', 'Littlehey (HMP)'
WHERE NOT EXISTS (SELECT 1
                  FROM enabled_organisation
                  WHERE code = 'LTI');

INSERT INTO enabled_organisation (code, description)
SELECT 'LWI', 'Lewes (HMP)'
WHERE NOT EXISTS (SELECT 1
                  FROM enabled_organisation
                  WHERE code = 'LWI');

INSERT INTO enabled_organisation (code, description)
SELECT 'MDI', 'Moorland (HMP & YOI)'
WHERE NOT EXISTS (SELECT 1
                  FROM enabled_organisation
                  WHERE code = 'MDI');

INSERT INTO enabled_organisation (code, description)
SELECT 'MRI', 'Manchester (HMP)'
WHERE NOT EXISTS (SELECT 1
                  FROM enabled_organisation
                  WHERE code = 'MRI');

INSERT INTO enabled_organisation (code, description)
SELECT 'MTI', 'The Mount (HMP)'
WHERE NOT EXISTS (SELECT 1
                  FROM enabled_organisation
                  WHERE code = 'MTI');

INSERT INTO enabled_organisation (code, description)
SELECT 'NHI', 'New Hall (HMP & YOI)'
WHERE NOT EXISTS (SELECT 1
                  FROM enabled_organisation
                  WHERE code = 'NHI');

INSERT INTO enabled_organisation (code, description)
SELECT 'NLI', 'Northumberland (HMP)'
WHERE NOT EXISTS (SELECT 1
                  FROM enabled_organisation
                  WHERE code = 'NLI');

INSERT INTO enabled_organisation (code, description)
SELECT 'ONI', 'Onley (HMP)'
WHERE NOT EXISTS (SELECT 1
                  FROM enabled_organisation
                  WHERE code = 'ONI');

INSERT INTO enabled_organisation (code, description)
SELECT 'OWI', 'Oakwood (HMP)'
WHERE NOT EXISTS (SELECT 1
                  FROM enabled_organisation
                  WHERE code = 'OWI');

INSERT INTO enabled_organisation (code, description)
SELECT 'PBI', 'Peterborough (HMP & YOI)'
WHERE NOT EXISTS (SELECT 1
                  FROM enabled_organisation
                  WHERE code = 'PBI');

INSERT INTO enabled_organisation (code, description)
SELECT 'PCI', 'Pucklechurch (hmrc)'
WHERE NOT EXISTS (SELECT 1
                  FROM enabled_organisation
                  WHERE code = 'PCI');

INSERT INTO enabled_organisation (code, description)
SELECT 'PDI', 'Portland (HMP & YOI)'
WHERE NOT EXISTS (SELECT 1
                  FROM enabled_organisation
                  WHERE code = 'PDI');

INSERT INTO enabled_organisation (code, description)
SELECT 'PFI', 'Peterborough Female (HMP & YOI)'
WHERE NOT EXISTS (SELECT 1
                  FROM enabled_organisation
                  WHERE code = 'PFI');

INSERT INTO enabled_organisation (code, description)
SELECT 'PRI', 'Parc (HMP)'
WHERE NOT EXISTS (SELECT 1
                  FROM enabled_organisation
                  WHERE code = 'PRI');

INSERT INTO enabled_organisation (code, description)
SELECT 'RCI', 'Rochester (HMP & YOI)'
WHERE NOT EXISTS (SELECT 1
                  FROM enabled_organisation
                  WHERE code = 'RCI');

INSERT INTO enabled_organisation (code, description)
SELECT 'RHI', 'Rye Hill (HMP)'
WHERE NOT EXISTS (SELECT 1
                  FROM enabled_organisation
                  WHERE code = 'RHI');

INSERT INTO enabled_organisation (code, description)
SELECT 'RNI', 'Ranby (HMP)'
WHERE NOT EXISTS (SELECT 1
                  FROM enabled_organisation
                  WHERE code = 'RNI');

INSERT INTO enabled_organisation (code, description)
SELECT 'RSI', 'Risley (HMP)'
WHERE NOT EXISTS (SELECT 1
                  FROM enabled_organisation
                  WHERE code = 'RSI');

INSERT INTO enabled_organisation (code, description)
SELECT 'SFI', 'Stafford (HMP)'
WHERE NOT EXISTS (SELECT 1
                  FROM enabled_organisation
                  WHERE code = 'SFI');

INSERT INTO enabled_organisation (code, description)
SELECT 'SHI', 'Stoke Heath (HMP & YOI)'
WHERE NOT EXISTS (SELECT 1
                  FROM enabled_organisation
                  WHERE code = 'SHI');

INSERT INTO enabled_organisation (code, description)
SELECT 'SKI', 'Stocken (HMP)'
WHERE NOT EXISTS (SELECT 1
                  FROM enabled_organisation
                  WHERE code = 'SKI');

INSERT INTO enabled_organisation (code, description)
SELECT 'SLI', 'Swaleside (HMP)'
WHERE NOT EXISTS (SELECT 1
                  FROM enabled_organisation
                  WHERE code = 'SLI');

INSERT INTO enabled_organisation (code, description)
SELECT 'SNI', 'Swinfen Hall (HMP & YOI)'
WHERE NOT EXISTS (SELECT 1
                  FROM enabled_organisation
                  WHERE code = 'SNI');

INSERT INTO enabled_organisation (code, description)
SELECT 'STI', 'Styal (HMP & YOI)'
WHERE NOT EXISTS (SELECT 1
                  FROM enabled_organisation
                  WHERE code = 'STI');

INSERT INTO enabled_organisation (code, description)
SELECT 'UKI', 'Usk (HMP)'
WHERE NOT EXISTS (SELECT 1
                  FROM enabled_organisation
                  WHERE code = 'UKI');

INSERT INTO enabled_organisation (code, description)
SELECT 'WDI', 'Woodhill (HMP)'
WHERE NOT EXISTS (SELECT 1
                  FROM enabled_organisation
                  WHERE code = 'WDI');

INSERT INTO enabled_organisation (code, description)
SELECT 'WEI', 'Wealstun (HMP)'
WHERE NOT EXISTS (SELECT 1
                  FROM enabled_organisation
                  WHERE code = 'WEI');

INSERT INTO enabled_organisation (code, description)
SELECT 'WHI', 'Woodhill (HMP & YOI)'
WHERE NOT EXISTS (SELECT 1
                  FROM enabled_organisation
                  WHERE code = 'WHI');

INSERT INTO enabled_organisation (code, description)
SELECT 'WLI', 'Wayland (HMP)'
WHERE NOT EXISTS (SELECT 1
                  FROM enabled_organisation
                  WHERE code = 'WLI');

INSERT INTO enabled_organisation (code, description)
SELECT 'WMI', 'Wymott (HMP)'
WHERE NOT EXISTS (SELECT 1
                  FROM enabled_organisation
                  WHERE code = 'WMI');

INSERT INTO enabled_organisation (code, description)
SELECT 'WRI', 'Whitemoor (HMP)'
WHERE NOT EXISTS (SELECT 1
                  FROM enabled_organisation
                  WHERE code = 'WRI');

INSERT INTO enabled_organisation (code, description)
SELECT 'WTI', 'Whatton (HMP)'
WHERE NOT EXISTS (SELECT 1
                  FROM enabled_organisation
                  WHERE code = 'WTI');