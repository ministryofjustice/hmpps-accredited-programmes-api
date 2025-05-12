CREATE TABLE selected_sexual_offence_details (
     id UUID PRIMARY KEY,
     referral_id UUID NOT NULL,
     sexual_offence_details_id UUID,
     CONSTRAINT fk_referral FOREIGN KEY (referral_id) REFERENCES referral(referral_id),
     CONSTRAINT fk_sexual_offence_details FOREIGN KEY (sexual_offence_details_id) REFERENCES sexual_offence_details(id)
);

-- Add indexes for the foreign keys
CREATE INDEX idx_selected_sexual_offence_details_referral_id ON selected_sexual_offence_details(referral_id);
CREATE INDEX idx_selected_sexual_offence_details_sexual_offence_details_id ON selected_sexual_offence_details(sexual_offence_details_id);
