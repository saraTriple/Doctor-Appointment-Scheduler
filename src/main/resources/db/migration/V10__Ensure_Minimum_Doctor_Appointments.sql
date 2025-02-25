DO $$
DECLARE
    doctor_record RECORD;
    appointment_count INTEGER;
    next_available_date DATE;
    next_available_time TIME;
    status_value VARCHAR;
BEGIN
    -- Get the status ID for available appointments
    SELECT status INTO status_value FROM appointment_status WHERE status = 'AVAILABLE';
    
    -- Set the starting point for new appointments to tomorrow
    next_available_date := CURRENT_DATE + 1;
    
    -- For each doctor
    FOR doctor_record IN SELECT * FROM doctor
    LOOP
        -- Count current available appointments
        SELECT COUNT(*) INTO appointment_count
        FROM appointment
        WHERE doctor_email = doctor_record.email
        AND status = status_value
        AND date >= CURRENT_DATE;
        
        -- If less than 2 available appointments
        WHILE appointment_count < 2 LOOP
            -- Find the next available time slot
            -- Start with 9 AM
            next_available_time := '09:00:00'::TIME;
            
            -- Find a time slot that doesn't conflict
            WHILE EXISTS (
                SELECT 1 FROM appointment 
                WHERE doctor_email = doctor_record.email 
                AND date = next_available_date 
                AND time = next_available_time
            ) LOOP
                -- Move to next hour
                next_available_time := next_available_time + INTERVAL '1 hour';
                
                -- If we've gone past 5 PM, move to next day
                IF next_available_time > '17:00:00'::TIME THEN
                    next_available_date := next_available_date + 1;
                    next_available_time := '09:00:00'::TIME;
                END IF;
            END LOOP;
            
            -- Insert new available appointment
            INSERT INTO appointment (date, time, doctor_email, status)
            VALUES (next_available_date, next_available_time, doctor_record.email, status_value);
            
            appointment_count := appointment_count + 1;
        END LOOP;
    END LOOP;
END $$;
