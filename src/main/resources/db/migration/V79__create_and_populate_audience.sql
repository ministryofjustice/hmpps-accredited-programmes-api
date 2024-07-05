

CREATE TABLE IF NOT EXISTS audience (
    audience_id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    colour VARCHAR(255),
    CONSTRAINT unique_name_colour UNIQUE (name, colour)
    );


INSERT INTO audience (audience_id, name, colour) VALUES
                                                     ('e4d1a44a-9c3b-4a7c-b79c-4d8a76488eb2', 'All offences', 'light-blue'),
                                                     ('e9f9a8d8-2f7d-4a88-8f39-5d3b9071e75b', 'Extremism offence', 'turquoise'),
                                                     ('7e2db7fc-18c5-4bda-bb0d-ec39bfa20414', 'Gang-related offence', 'purple'),
                                                     ('ff3a547a-86c2-40ba-b57f-534d1548fd5b', 'General violence offence', 'yellow'),
                                                     ('2b33b6d4-2f53-4e19-8e92-55bba8a99f1a', 'Intimate partner violence offence', 'green'),
                                                     ('5ef4c4b2-03fa-45d8-a9d4-9e57c52a6f1d', 'Legacy Course', NULL),
                                                     ('a7b3d6f0-7cfa-42d8-9201-2f8bda8b3a74', 'Sexual offence', 'orange');