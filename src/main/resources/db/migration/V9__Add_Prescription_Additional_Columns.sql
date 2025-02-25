-- Add additional columns to prescription table
ALTER TABLE prescription
    ADD COLUMN doctor_notes TEXT,
    ADD COLUMN dosage_instructions TEXT,
    ADD COLUMN lab_tests TEXT,
    ADD COLUMN follow_up_instructions TEXT,
    ADD COLUMN appointment_id BIGINT REFERENCES appointment(id);
