-- Insert prescription histories
DO $$
DECLARE
    patient_rec RECORD;
    common_symptoms text[] := ARRAY[
        'Fever and Fatigue', 'Chest Pain', 'Digestive Issues', 'Skin Rash',
        'Dizziness', 'Chronic Pain', 'Respiratory Problems', 'Vision Problems'
    ];
    diagnoses text[] := ARRAY[
        'Common Cold', 'Hypertension', 'Gastritis', 'Dermatitis',
        'Vertigo', 'Arthritis', 'Bronchitis', 'Conjunctivitis'
    ];
    medications text[] := ARRAY[
        'Paracetamol 500mg', 'Lisinopril 10mg', 'Omeprazole 20mg', 'Hydrocortisone Cream',
        'Meclizine 25mg', 'Ibuprofen 400mg', 'Albuterol Inhaler', 'Antibiotic Eye Drops'
    ];
    med_combinations text[] := ARRAY[
        'Paracetamol 500mg, Vitamin C 1000mg',
        'Lisinopril 10mg, Hydrochlorothiazide 12.5mg',
        'Omeprazole 20mg, Domperidone 10mg',
        'Amlodipine 5mg, Losartan 50mg'
    ];
    index INTEGER;
BEGIN
    -- For each patient
    FOR patient_rec IN SELECT email FROM patient LOOP
        -- Create initial prescription (3 months ago)
        index := floor(random() * array_length(common_symptoms, 1) + 1);
        INSERT INTO prescription (
            patient_email,
            prescription_date,
            symptoms,
            diagnosis,
            medications
        ) VALUES (
            patient_rec.email,
            CURRENT_DATE - INTERVAL '3 months',
            common_symptoms[index],
            diagnoses[index],
            medications[index]
        );

        -- 70% chance of follow-up prescription
        IF random() < 0.7 THEN
            index := floor(random() * array_length(med_combinations, 1) + 1);
            INSERT INTO prescription (
                patient_email,
                prescription_date,
                symptoms,
                diagnosis,
                medications
            ) VALUES (
                patient_rec.email,
                CURRENT_DATE - INTERVAL '1 month',
                'Follow-up visit',
                'Continuing treatment',
                med_combinations[index]
            );
        END IF;
    END LOOP;
END $$;

-- Mark initializer as completed
INSERT INTO initialization_tracker (initializer_name, initialized_at) VALUES
('PrescriptionHistoryInitializer', CURRENT_TIMESTAMP);
