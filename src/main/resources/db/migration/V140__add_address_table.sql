CREATE TABLE IF NOT EXISTS address
(
    id   uuid,
    address_line_1 text,
    address_line_2 text,
    country text,
    county text,
    postal_code text,
    town text,
    primary key (id)
    );

ALTER TABLE organisation
    ADD COLUMN address uuid;

ALTER TABLE organisation
    ADD CONSTRAINT address_id_fk
        FOREIGN KEY (address) REFERENCES address(id);
