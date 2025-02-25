-- Create prescription_medications table for storing multiple medications per prescription
CREATE TABLE prescription_medications (
    prescription_id BIGINT NOT NULL REFERENCES prescription(id),
    medications VARCHAR(255)
);
