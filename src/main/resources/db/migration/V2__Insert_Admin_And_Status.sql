-- Insert initial admin users
INSERT INTO admin (email, password) VALUES
('admin1@example.com', 'password1'),
('admin2@example.com', 'password2');

-- Insert appointment statuses
INSERT INTO appointment_status (status, description) VALUES
('AVAILABLE', 'Appointment slot is available for booking'),
('SCHEDULED', 'Appointment has been booked by a patient'),
('COMPLETED', 'Appointment has been completed'),
('CANCELLED', 'Appointment has been cancelled'),
('NO_SHOW', 'Patient did not show up for the appointment');

-- Mark initializers as completed
INSERT INTO initialization_tracker (initializer_name, initialized_at) VALUES
('AdminInitializer', CURRENT_TIMESTAMP),
('AppointmentStatusInitializer', CURRENT_TIMESTAMP);
