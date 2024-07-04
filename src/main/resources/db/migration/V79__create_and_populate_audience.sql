
CREATE TABLE IF NOT EXISTS audience (
    audience_id UUID DEFAULT RANDOM_UUID() PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    colour VARCHAR(255)
    );

-- Insert unique records into the audience table
INSERT INTO audience (name, colour)
SELECT DISTINCT audience, audience_colour
FROM course;
