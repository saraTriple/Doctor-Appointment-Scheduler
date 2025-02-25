-- Insert appointments
DO $$
DECLARE
    doctor_rec RECORD;
    patient_rec RECORD;
    status_rec RECORD;
    appointment_date DATE;
    appointment_time TIME;
    attempt_count INTEGER;
    max_attempts INTEGER := 10;
BEGIN
    -- For each doctor
    FOR doctor_rec IN SELECT email FROM doctor LOOP
        -- Create 10 appointments per doctor
        FOR i IN 1..10 LOOP
            -- Get random patient
            SELECT email INTO patient_rec
            FROM patient
            ORDER BY random()
            LIMIT 1;

            -- Get random status (SCHEDULED or COMPLETED)
            SELECT status INTO status_rec
            FROM appointment_status
            WHERE status IN ('SCHEDULED', 'COMPLETED')
            ORDER BY random()
            LIMIT 1;

            -- Try to find a unique time slot
            attempt_count := 0;
            LOOP
                -- Generate random date and time
                appointment_date := CURRENT_DATE + (floor(random() * 30)::int || ' days')::interval;
                -- Generate time between 9 AM and 4 PM in 30-minute intervals
                appointment_time := make_time(9 + floor(random() * 7)::int, floor(random() * 2)::int * 30, 0);

                -- Check if this slot is available
                IF NOT EXISTS (
                    SELECT 1 FROM appointment 
                    WHERE doctor_email = doctor_rec.email 
                    AND date = appointment_date 
                    AND time = appointment_time
                ) THEN
                    -- Insert appointment
                    INSERT INTO appointment (
                        doctor_email,
                        patient_email,
                        date,
                        time,
                        status,
                        price
                    ) VALUES (
                        doctor_rec.email,
                        patient_rec.email,
                        appointment_date,
                        appointment_time,
                        status_rec.status,
                        80.0 + floor(random() * 421) -- Random price between 80 and 500
                    );
                    EXIT; -- Successfully inserted, exit loop
                END IF;

                attempt_count := attempt_count + 1;
                IF attempt_count >= max_attempts THEN
                    EXIT; -- Give up after max attempts
                END IF;
            END LOOP;
        END LOOP;
    END LOOP;
END $$;
