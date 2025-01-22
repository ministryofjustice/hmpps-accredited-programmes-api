
ALTER TABLE COURSE
    ADD COLUMN intensity text;


UPDATE COURSE SET intensity = 'HIGH' WHERE name IN ( 'Kaizen', 'Building Choices: high intensity', 'Becoming New Me Plus',
                                                   'Healthy Sex Programme', 'Healthy Identity Intervention');


UPDATE COURSE SET intensity = 'MODERATE' WHERE name IN ('Thinking Skills Programme', 'Building Choices: moderate intensity', 'New Me Strengths',
                                                        'Building Better Relationships', 'Horizon', 'Control of Violence for Angry Impulsive Drinkers',
                                                        'Identity Matters');

UPDATE COURSE SET intensity = 'HIGH MODERATE' WHERE name IN ('New Me MOT', 'Living as New Me');