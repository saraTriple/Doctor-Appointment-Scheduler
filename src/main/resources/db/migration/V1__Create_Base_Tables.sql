-- Create Admin table
CREATE TABLE admin (
    email VARCHAR(255) PRIMARY KEY,
    password VARCHAR(255) NOT NULL
);

-- Create Doctor table
CREATE TABLE doctor (
    email VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    specialization VARCHAR(255) NOT NULL,
    qualification VARCHAR(50),
    state VARCHAR(100),
    city VARCHAR(100),
    password VARCHAR(255) NOT NULL
);

-- Create Patient table
CREATE TABLE patient (
    email VARCHAR(255) PRIMARY KEY,
    password VARCHAR(255) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    gender VARCHAR(10),
    date_of_birth DATE,
    phone_number VARCHAR(20),
    address TEXT,
    insurance_provider VARCHAR(255),
    insurance_policy_number VARCHAR(255)
);

-- Create AppointmentStatus table
CREATE TABLE appointment_status (
    status VARCHAR(50) PRIMARY KEY,
    description VARCHAR(255)
);

-- Create Appointment table
CREATE TABLE appointment (
    id BIGSERIAL PRIMARY KEY,
    doctor_email VARCHAR(255) REFERENCES doctor(email),
    patient_email VARCHAR(255) REFERENCES patient(email),
    date DATE NOT NULL,
    time TIME NOT NULL,
    status VARCHAR(50) REFERENCES appointment_status(status),
    price DECIMAL(10,2),
    CONSTRAINT unique_doctor_datetime UNIQUE (doctor_email, date, time)
);

-- Create PatientHistory table
CREATE TABLE patient_history (
    id BIGSERIAL PRIMARY KEY,
    patient_email VARCHAR(255) REFERENCES patient(email),
    record_date DATE NOT NULL,
    symptoms TEXT,
    diagnosis TEXT,
    medications TEXT,
    allergies TEXT,
    blood_type VARCHAR(5),
    weight DECIMAL(5,2),
    height DECIMAL(5,2),
    chronic_conditions TEXT,
    family_history TEXT
);

-- Create Prescription table
CREATE TABLE prescription (
    id BIGSERIAL PRIMARY KEY,
    patient_email VARCHAR(255) REFERENCES patient(email),
    prescription_date DATE NOT NULL,
    symptoms TEXT,
    diagnosis TEXT,
    medications TEXT
);

-- Create InitializationTracker table
CREATE TABLE initialization_tracker (
    initializer_name VARCHAR(255) PRIMARY KEY,
    initialized_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
