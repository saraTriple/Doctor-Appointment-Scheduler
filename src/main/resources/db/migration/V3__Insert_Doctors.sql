-- Insert doctors with various specializations
INSERT INTO doctor (email, name, specialization, qualification, state, city, password) VALUES
('dr.john@example.com', 'Dr. John Doe', 'Cardiologist', 'MD', 'California', 'Los Angeles', 'pass123'),
('dr.smith@example.com', 'Dr. Alice Smith', 'Neurologist', 'PhD', 'Texas', 'Houston', 'pass123'),
('dr.miller@example.com', 'Dr. Robert Miller', 'Dermatologist', 'MD', 'New York', 'New York City', 'pass123'),
('dr.lee@example.com', 'Dr. Kevin Lee', 'Orthopedic', 'MS', 'Florida', 'Miami', 'pass123'),
('dr.wilson@example.com', 'Dr. Emma Wilson', 'Pediatrician', 'MD', 'Illinois', 'Chicago', 'pass123'),
('dr.anderson@example.com', 'Dr. James Anderson', 'Oncologist', 'MD', 'Arizona', 'Phoenix', 'pass123'),
('dr.thomas@example.com', 'Dr. Olivia Thomas', 'ENT Specialist', 'MD', 'Nevada', 'Las Vegas', 'pass123'),
('dr.taylor@example.com', 'Dr. Sophia Taylor', 'Radiologist', 'PhD', 'Georgia', 'Atlanta', 'pass123'),
('dr.white@example.com', 'Dr. William White', 'Psychiatrist', 'MD', 'Washington', 'Seattle', 'pass123'),
('dr.moore@example.com', 'Dr. Daniel Moore', 'Urologist', 'MD', 'Michigan', 'Detroit', 'pass123'),
('dr.harris@example.com', 'Dr. Emily Harris', 'Gynecologist', 'MD', 'Colorado', 'Denver', 'pass123'),
('dr.clark@example.com', 'Dr. Ethan Clark', 'General Surgeon', 'MS', 'Oregon', 'Portland', 'pass123'),
('dr.hall@example.com', 'Dr. Charlotte Hall', 'Nephrologist', 'MD', 'Ohio', 'Columbus', 'pass123'),
('dr.young@example.com', 'Dr. Benjamin Young', 'Hematologist', 'PhD', 'Minnesota', 'Minneapolis', 'pass123'),
('dr.king@example.com', 'Dr. Isabella King', 'Pulmonologist', 'MD', 'Pennsylvania', 'Philadelphia', 'pass123'),
('dr.scott@example.com', 'Dr. Henry Scott', 'Endocrinologist', 'MD', 'Tennessee', 'Nashville', 'pass123'),
('dr.green@example.com', 'Dr. Victoria Green', 'Rheumatologist', 'MD', 'Missouri', 'St. Louis', 'pass123'),
('dr.adams@example.com', 'Dr. Samuel Adams', 'Gastroenterologist', 'MD', 'Massachusetts', 'Boston', 'pass123'),
('dr.baker@example.com', 'Dr. Ava Baker', 'Ophthalmologist', 'MD', 'Indiana', 'Indianapolis', 'pass123'),
('dr.carter@example.com', 'Dr. Lucas Carter', 'Neurologist', 'PhD', 'North Carolina', 'Charlotte', 'pass123');

-- Mark initializer as completed
INSERT INTO initialization_tracker (initializer_name, initialized_at) VALUES
('DoctorDataInitializer', CURRENT_TIMESTAMP);
