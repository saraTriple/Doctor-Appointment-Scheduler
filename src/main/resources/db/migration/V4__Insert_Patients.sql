-- Insert initial patients
DO $$
DECLARE
    i INTEGER;
BEGIN
    FOR i IN 1..50 LOOP
        INSERT INTO patient (
            email,
            password,
            first_name,
            last_name,
            gender,
            date_of_birth,
            phone_number,
            address,
            insurance_provider,
            insurance_policy_number
        ) VALUES (
            'patient' || i || '@example.com',
            'password',
            'FirstName' || i,
            'LastName' || i,
            CASE WHEN i % 2 = 0 THEN 'Male' ELSE 'Female' END,
            CURRENT_DATE - ((20 + (i % 30)) * INTERVAL '1 year'),
            '+123456789' || i,
            'Address ' || i,
            CASE WHEN i % 3 = 0 THEN 'Insurance Co ' || (i % 5) ELSE NULL END,
            CASE WHEN i % 3 = 0 THEN 'POLICY' || i ELSE NULL END
        );
    END LOOP;
END $$;

-- Mark initializer as completed
INSERT INTO initialization_tracker (initializer_name, initialized_at) VALUES
('PatientInitializer', CURRENT_TIMESTAMP);
