CREATE TABLE IF NOT EXISTS sexual_offence_details
(
    id          UUID NOT NULL PRIMARY KEY,
    category    TEXT NOT NULL,
    description TEXT NOT NULL,
    hint_text   TEXT NULL,
    score       SMALLINT
);

INSERT INTO sexual_offence_details (id, category, description, hint_text, score)
SELECT '6fbbfb78-e5b7-4d9c-9b09-e0fce3ddecd1',
       'AGAINST_MINORS',
       'A victim aged 12 or younger',
       null,
       1
WHERE NOT EXISTS (SELECT 1
                  FROM sexual_offence_details
                  WHERE id = '6fbbfb78-e5b7-4d9c-9b09-e0fce3ddecd1');

INSERT INTO sexual_offence_details (id, category, description, hint_text, score)
SELECT '7e297108-34af-4ceb-8cb6-f1fdbd5c6950',
       'AGAINST_MINORS',
       'A male victim aged 15 or younger',
       null,
       1
WHERE NOT EXISTS (SELECT 1
                  FROM sexual_offence_details
                  WHERE id = '7e297108-34af-4ceb-8cb6-f1fdbd5c6950');

INSERT INTO sexual_offence_details (id, category, description, hint_text, score)
SELECT '9aa62d62-2cbc-4a09-be64-a075bb39a226',
       'AGAINST_MINORS',
       'More than one victim aged 15 or younger',
       null,
       1
WHERE NOT EXISTS (SELECT 1
                  FROM sexual_offence_details
                  WHERE id = '9aa62d62-2cbc-4a09-be64-a075bb39a226');

INSERT INTO sexual_offence_details (id, category, description, hint_text, score)
SELECT '29c24c4c-f0c2-4a13-9eab-aedefa202bd4',
       'AGAINST_MINORS',
       'A victim outside the family aged 15 or younger',
       'Include victims who are not the son, daughter, stepson, stepdaughter, nephew, niece, grandchild or first cousin',
       1
WHERE NOT EXISTS (SELECT 1
                  FROM sexual_offence_details
                  WHERE id = '29c24c4c-f0c2-4a13-9eab-aedefa202bd4');

INSERT INTO sexual_offence_details (id, category, description, hint_text, score)
SELECT 'f5afed62-0747-432e-97b4-19b255e72b52',
       'AGAINST_MINORS',
       'Possessing indecent images of under-18s',
       'Images showing someone who is or appears to be under 18, in a sexually suggestive pose, in sexual conduct with another person, or that are sexually explicit. This includes visual and non-visual depictions of real children and depictions of fictional children.',
       3
WHERE NOT EXISTS (SELECT 1
                  FROM sexual_offence_details
                  WHERE id = 'f5afed62-0747-432e-97b4-19b255e72b52');

INSERT INTO sexual_offence_details (id, category, description, hint_text, score)
SELECT '1b73119d-b14c-4680-9d4f-3dc472df9b86',
       'AGAINST_MINORS',
       'Self-reported sexual attraction to under-18s',
       null,
       3
WHERE NOT EXISTS (SELECT 1
                  FROM sexual_offence_details
                  WHERE id = '1b73119d-b14c-4680-9d4f-3dc472df9b86');

INSERT INTO sexual_offence_details (id, category, description, hint_text, score)
SELECT '9db48010-fa98-4863-ad77-c426b82e61f7',
       'INCLUDES_VIOLENCE_FORCE_HUMILIATION',
       'At least one conviction for a coercive sexual offence',
       'Unwanted sexual activity without consent, or if consent has been forcefully gained, tricked, pressured or manipulated, or if consent cannot be freely and knowingly given, for example because of sleep, drugs or alcoholic incapacitation',
       1
WHERE NOT EXISTS (SELECT 1
                  FROM sexual_offence_details
                  WHERE id = '9db48010-fa98-4863-ad77-c426b82e61f7');

INSERT INTO sexual_offence_details (id, category, description, hint_text, score)
SELECT '462cbe6f-6d10-42ca-9d9d-d3c9a851b442',
       'INCLUDES_VIOLENCE_FORCE_HUMILIATION',
       'More than one conviction for a coercive sexual offence against 3 or more separate victims',
       null,
       3
WHERE NOT EXISTS (SELECT 1
                  FROM sexual_offence_details
                  WHERE id = '462cbe6f-6d10-42ca-9d9d-d3c9a851b442');

INSERT INTO sexual_offence_details (id, category, description, hint_text, score)
SELECT '7234d72b-3d07-4a1f-8cfe-bb2d5e605897',
       'INCLUDES_VIOLENCE_FORCE_HUMILIATION',
       'Mutilated parts of the victim''s body',
       'Actions that injure, disfigure, or make imperfect by removing or irreparably damaging a body part',
       1
WHERE NOT EXISTS (SELECT 1
                  FROM sexual_offence_details
                  WHERE id = '7234d72b-3d07-4a1f-8cfe-bb2d5e605897');

INSERT INTO sexual_offence_details (id, category, description, hint_text, score)
SELECT 'e8b10289-22cb-448b-8f5e-2af442c5b49c',
       'INCLUDES_VIOLENCE_FORCE_HUMILIATION',
       'Tortured or engaged in acts of cruelty to the victim',
       'Deliberately inflicting severe pain or suffering for sexual pleasure. This does not include safe BDSM practice with a consenting partner.',
       1
WHERE NOT EXISTS (SELECT 1
                  FROM sexual_offence_details
                  WHERE id = 'e8b10289-22cb-448b-8f5e-2af442c5b49c');

INSERT INTO sexual_offence_details (id, category, description, hint_text, score)
SELECT '1af7fc66-d5b6-4fad-8362-d78cd8774be4',
       'INCLUDES_VIOLENCE_FORCE_HUMILIATION',
       'Gratuitous violence or wounding toward the victim',
       'Any use of additional violence or force not necessary to gain victim compliance. ''Wounding'' is an intentional injury that breaks the skin, with or without using a weapon.',
       1
WHERE NOT EXISTS (SELECT 1
                  FROM sexual_offence_details
                  WHERE id = '1af7fc66-d5b6-4fad-8362-d78cd8774be4');

INSERT INTO sexual_offence_details (id, category, description, hint_text, score)
SELECT '0273dc4f-ce33-4ffb-92fb-23ef7f2064d8',
       'INCLUDES_VIOLENCE_FORCE_HUMILIATION',
       'Kept records or trophies (for example hair, underwear, ID)',
       null,
       1
WHERE NOT EXISTS (SELECT 1
                  FROM sexual_offence_details
                  WHERE id = '0273dc4f-ce33-4ffb-92fb-23ef7f2064d8');

INSERT INTO sexual_offence_details (id, category, description, hint_text, score)
SELECT 'eca2a59e-9917-4e98-81df-a430649742b9',
       'INCLUDES_VIOLENCE_FORCE_HUMILIATION',
       'Victim was abducted or confined',
       'Forcing or threatening a victim to go with them or taking them away against their will. ''Confined'' means keeping them in an enclosed space with limited or restricted movement or ability to leave.',
       1
WHERE NOT EXISTS (SELECT 1
                  FROM sexual_offence_details
                  WHERE id = 'eca2a59e-9917-4e98-81df-a430649742b9');

INSERT INTO sexual_offence_details (id, category, description, hint_text, score)
SELECT '5833cf58-6acb-4c9b-9717-8800bd0f5441',
       'INCLUDES_VIOLENCE_FORCE_HUMILIATION',
       'Evidence of ritualism in the offence',
       'Fixed actions or words, performed in a specific way',
       1
WHERE NOT EXISTS (SELECT 1
                  FROM sexual_offence_details
                  WHERE id = '5833cf58-6acb-4c9b-9717-8800bd0f5441');

INSERT INTO sexual_offence_details (id, category, description, hint_text, score)
SELECT '1e370cb5-1257-4f5e-883c-2adf34a1dbb6',
       'INCLUDES_VIOLENCE_FORCE_HUMILIATION',
       'Objects inserted into bodily orifices',
       null,
       1
WHERE NOT EXISTS (SELECT 1
                  FROM sexual_offence_details
                  WHERE id = '1e370cb5-1257-4f5e-883c-2adf34a1dbb6');

INSERT INTO sexual_offence_details (id, category, description, hint_text, score)
SELECT 'e642f0df-cd93-45f1-b611-6372c8f6fccb',
       'INCLUDES_VIOLENCE_FORCE_HUMILIATION',
       'Self-reported preference for non-consensual or forced sex',
       null,
       2
WHERE NOT EXISTS (SELECT 1
                  FROM sexual_offence_details
                  WHERE id = 'e642f0df-cd93-45f1-b611-6372c8f6fccb');

INSERT INTO sexual_offence_details (id, category, description, hint_text, score)
SELECT 'eeeab7b1-3c9a-4ca0-813a-f78860844cb3',
       'INCLUDES_VIOLENCE_FORCE_HUMILIATION',
       'Self-reported sexual arousal to the idea of inflicting violence, pain, fear or humiliation on another person',
       null,
       2
WHERE NOT EXISTS (SELECT 1
                  FROM sexual_offence_details
                  WHERE id = 'eeeab7b1-3c9a-4ca0-813a-f78860844cb3');

INSERT INTO sexual_offence_details (id, category, description, hint_text, score)
SELECT '38915389-f446-47e5-b37d-beee7c9eba14',
       'OTHER',
       'Exhibitionism',
       'Exposing genitals to an unsuspecting person for sexual gratification',
       2
WHERE NOT EXISTS (SELECT 1
                  FROM sexual_offence_details
                  WHERE id = '38915389-f446-47e5-b37d-beee7c9eba14');

INSERT INTO sexual_offence_details (id, category, description, hint_text, score)
SELECT '6d3d2191-dedc-4c8c-b5b5-c0fbda1263a0',
       'OTHER',
       'Frotteurism',
       'Touching or rubbing (usually genitals) against someone in a sexual manner without their consent',
       2
WHERE NOT EXISTS (SELECT 1
                  FROM sexual_offence_details
                  WHERE id = '6d3d2191-dedc-4c8c-b5b5-c0fbda1263a0');

INSERT INTO sexual_offence_details (id, category, description, hint_text, score)
SELECT '723ab539-f692-46d6-97f0-99e51fb7f3c1',
       'OTHER',
       'Intercourse with an animal',
       'A sexual act with an animal. Indecent images only count if the person is involved in the image.',
       3
WHERE NOT EXISTS (SELECT 1
                  FROM sexual_offence_details
                  WHERE id = '723ab539-f692-46d6-97f0-99e51fb7f3c1');

INSERT INTO sexual_offence_details (id, category, description, hint_text, score)
SELECT '88031211-850f-4847-87d1-26aad52f765c',
       'OTHER',
       'Sexual murder',
       'Murder that contains a sexual element. For example, sexual acts before, during or after the murder, leaving the body in a state of undress or taking underwear.',
       3
WHERE NOT EXISTS (SELECT 1
                  FROM sexual_offence_details
                  WHERE id = '88031211-850f-4847-87d1-26aad52f765c');

INSERT INTO sexual_offence_details (id, category, description, hint_text, score)
SELECT '7d79342f-ae81-4523-a2f1-785002d95eac',
       'OTHER',
       'Sexual penetration of a corpse',
       'A sexual act with a dead body. Indecent images only count if the person is involved in the image.',
       3
WHERE NOT EXISTS (SELECT 1
                  FROM sexual_offence_details
                  WHERE id = '7d79342f-ae81-4523-a2f1-785002d95eac');

INSERT INTO sexual_offence_details (id, category, description, hint_text, score)
SELECT '70813fb3-33c8-4812-94cd-201eff0cdd6e',
       'OTHER',
       'Voyeurism',
       'Watching an unaware or unconsenting person when they are naked or engaged in sexual activity',
       2
WHERE NOT EXISTS (SELECT 1
                  FROM sexual_offence_details
                  WHERE id = '70813fb3-33c8-4812-94cd-201eff0cdd6e');
