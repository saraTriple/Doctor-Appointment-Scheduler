-- Create function to generate random text from array
CREATE OR REPLACE FUNCTION random_array_element(arr text[])
RETURNS text AS $$
BEGIN
    RETURN arr[floor(random() * array_length(arr, 1) + 1)];
END;
$$ LANGUAGE plpgsql;

-- Insert patient histories
DO $$
DECLARE
    patient_rec RECORD;
    doctor_rec RECORD;
    symptoms text[] := ARRAY['Headache', 'Fever', 'Cough', 'Fatigue', 'Back pain', 'Joint pain',
                           'Chest pain', 'Shortness of breath', 'Dizziness', 'Nausea'];
    diagnoses text[] := ARRAY['Migraine', 'Common Cold', 'Hypertension', 'Diabetes', 'Arthritis',
                             'Asthma', 'Anxiety', 'Depression', 'Allergies', 'Bronchitis'];
    medications text[] := ARRAY['Aspirin', 'Ibuprofen', 'Amoxicillin', 'Lisinopril', 'Metformin',
                               'Omeprazole', 'Sertraline', 'Albuterol', 'Methotrexate', 'Prednisone'];
    allergies text[] := ARRAY['Penicillin', 'Peanuts', 'Latex', 'Dairy', 'Shellfish', 'None',
                             'Dust', 'Pollen', 'Eggs', 'Soy'];
    blood_types text[] := ARRAY['A+', 'A-', 'B+', 'B-', 'O+', 'O-', 'AB+', 'AB-'];
    chronic_conditions text[] := ARRAY['None', 'Hypertension', 'Diabetes', 'Asthma', 'Arthritis',
                                     'Heart Disease', 'COPD', 'Depression', 'Anxiety', 'Hypothyroidism'];
    random_symptoms text;
    random_medications text;
    i INTEGER;
BEGIN
    -- For each patient
    FOR patient_rec IN SELECT email FROM patient LOOP
        -- Create 1-3 history records
        FOR i IN 1..floor(random() * 3 + 1)::int LOOP
            -- Generate random symptoms (1-2)
            random_symptoms := random_array_element(symptoms);
            IF random() > 0.5 THEN
                random_symptoms := random_symptoms || ', ' || random_array_element(symptoms);
            END IF;

            -- Generate random medications (1-2)
            random_medications := random_array_element(medications);
            IF random() > 0.5 THEN
                random_medications := random_medications || ', ' || random_array_element(medications);
            END IF;

            -- Insert patient history
            INSERT INTO patient_history (
                patient_email,
                record_date,
                symptoms,
                diagnosis,
                medications,
                allergies,
                blood_type,
                weight,
                height,
                chronic_conditions,
                family_history
            ) VALUES (
                patient_rec.email,
                CURRENT_DATE - (floor(random() * 365)::int || ' days')::interval,
                random_symptoms,
                random_array_element(diagnoses),
                random_medications,
                random_array_element(allergies),
                random_array_element(blood_types),
                50 + random() * 50, -- Weight between 50-100 kg
                150 + random() * 50, -- Height between 150-200 cm
                random_array_element(chronic_conditions),
                'Family history of ' || random_array_element(diagnoses)
            );
        END LOOP;
    END LOOP;
END $$;

-- Mark initializer as completed
INSERT INTO initialization_tracker (initializer_name, initialized_at) VALUES
('PatientHistoryDataInitializer', CURRENT_TIMESTAMP);
