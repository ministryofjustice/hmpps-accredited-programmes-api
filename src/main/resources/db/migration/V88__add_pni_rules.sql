
CREATE TABLE IF NOT EXISTS pni_rule
(
    rule_id UUID NOT NULL PRIMARY KEY
    overall_need TEXT NOT NULL,
    overall_risk TEXT NOT NULL,
    combined_pathway TEXT NOT NULL,
    CONSTRAINT unique_need_risk UNIQUE (overall_need, overall_risk)
);

-- High Intensity BC
INSERT INTO pni_rule (rule_id, overall_need, overall_risk, combined_pathway) VALUES ('f69c4ad1-bf54-4ac2-b20e-d9c8e48a9a8a', 'HIGH_NEED', 'HIGH_RISK', 'HIGH_INTENSITY_BC');

-- Moderate Intensity BC
INSERT INTO pni_rule (rule_id, overall_need, overall_risk, combined_pathway) VALUES ('c5d163f8-957e-4da2-94f4-4c5b0737b3ac', 'MEDIUM_NEED', 'HIGH_RISK', 'MODERATE_INTENSITY_BC');
INSERT INTO pni_rule (rule_id, overall_need, overall_risk, combined_pathway) VALUES ('a1d5b3e7-8cb2-4f88-9fae-276fbb5a70cb', 'HIGH_NEED', 'MEDIUM_RISK', 'MODERATE_INTENSITY_BC');
INSERT INTO pni_rule (rule_id, overall_need, overall_risk, combined_pathway) VALUES ('e64e8b14-29e4-41e8-90d5-1d3a8a3c9b1e', 'MEDIUM_NEED', 'MEDIUM_RISK', 'MODERATE_INTENSITY_BC');

-- Alternative Pathway
INSERT INTO pni_rule (rule_id, overall_need, overall_risk, combined_pathway) VALUES ('8eac8b5d-8f21-41d4-8eb6-dba5f0d67b71', 'LOW_NEED', 'HIGH_RISK', 'ALTERNATIVE_PATHWAY');
INSERT INTO pni_rule (rule_id, overall_need, overall_risk, combined_pathway) VALUES ('f7b8dcb4-3b88-434d-90e0-9b4d7cfb454d', 'LOW_NEED', 'MEDIUM_RISK', 'ALTERNATIVE_PATHWAY');
INSERT INTO pni_rule (rule_id, overall_need, overall_risk, combined_pathway) VALUES ('e10a1c09-86d6-4299-9c5e-5d44b4dfb0d6', 'HIGH_NEED', 'LOW_RISK', 'ALTERNATIVE_PATHWAY');
INSERT INTO pni_rule (rule_id, overall_need, overall_risk, combined_pathway) VALUES ('2f4e5145-6f14-4dfb-95ad-f3fdf4e08092', 'MEDIUM_NEED', 'LOW_RISK', 'ALTERNATIVE_PATHWAY');
INSERT INTO pni_rule (rule_id, overall_need, overall_risk, combined_pathway) VALUES ('c1043a29-463d-450c-8473-2b4221bc6a4c', 'LOW_NEED', 'LOW_RISK', 'ALTERNATIVE_PATHWAY');
