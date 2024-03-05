CREATE TABLE IF NOT EXISTS enabled_organisation
(
    code text NOT NULL PRIMARY KEY,
    description text NOT NULL
);

INSERT INTO enabled_organisation (code, description) VALUES ('WTI', 'Whatton');
INSERT INTO enabled_organisation (code, description) VALUES ('ONI', 'Onley');
INSERT INTO enabled_organisation (code, description) VALUES ('SKI', 'Stocken');
