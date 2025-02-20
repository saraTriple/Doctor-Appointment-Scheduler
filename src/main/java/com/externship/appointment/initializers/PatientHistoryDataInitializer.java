package com.externship.appointment.initializers;

import com.externship.appointment.Appointment_storage.Appointment;
import com.externship.appointment.Appointment_storage.AppointmentRepository;
import com.externship.appointment.Appointment_storage.AppointmentStatus;
import com.externship.appointment.Appointment_storage.AppointmentStatusRepository;
import com.externship.appointment.Doctor_storage.Doctor;
import com.externship.appointment.Doctor_storage.DoctorRepository;
import com.externship.appointment.Patient_history.PatientHistory;
import com.externship.appointment.Patient_history.PatientHistoryRepository;
import com.externship.appointment.Patient_storage.Patient;
import com.externship.appointment.Patient_storage.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Component
@Order(5)
public class PatientHistoryDataInitializer implements CommandLineRunner {

    @Autowired
    private DoctorRepository doctorRepository;
    @Autowired
    private PatientRepository patientRepository;
    @Autowired
    private AppointmentRepository appointmentRepository;
    @Autowired
    private AppointmentStatusRepository statusRepository;
    @Autowired
    private PatientHistoryRepository historyRepository;

    private final String[] firstNames = {"John", "Jane", "Michael", "Emily", "David", "Sarah", "James", "Emma", "William", "Olivia",
            "Daniel", "Sophia", "Matthew", "Isabella", "Joseph", "Mia", "Christopher", "Charlotte", "Andrew", "Amelia"};

    private final String[] lastNames = {"Smith", "Johnson", "Williams", "Brown", "Jones", "Garcia", "Miller", "Davis",
            "Rodriguez", "Martinez", "Hernandez", "Lopez", "Gonzalez", "Wilson", "Anderson", "Thomas", "Taylor", "Moore",
            "Jackson", "Martin"};

    private final String[] specializations = {"Cardiologist", "Neurologist", "Pediatrician", "Dermatologist",
            "Orthopedist", "Psychiatrist", "Oncologist", "Gynecologist", "ENT Specialist", "Ophthalmologist"};

    private final String[] symptoms = {"Headache", "Fever", "Cough", "Fatigue", "Back pain", "Joint pain",
            "Chest pain", "Shortness of breath", "Dizziness", "Nausea"};

    private final String[] diagnoses = {"Migraine", "Common Cold", "Hypertension", "Diabetes", "Arthritis",
            "Asthma", "Anxiety", "Depression", "Allergies", "Bronchitis"};

    private final String[] medications = {"Aspirin", "Ibuprofen", "Amoxicillin", "Lisinopril", "Metformin",
            "Omeprazole", "Sertraline", "Albuterol", "Methotrexate", "Prednisone"};

    private final String[] allergies = {"Penicillin", "Peanuts", "Latex", "Dairy", "Shellfish", "None",
            "Dust", "Pollen", "Eggs", "Soy"};

    private final String[] bloodTypes = {"A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-"};

    private final String[] chronicConditions = {"None", "Hypertension", "Diabetes", "Asthma", "Arthritis",
            "Heart Disease", "COPD", "Depression", "Anxiety", "Hypothyroidism"};

    @Override
    public void run(String... args) {
        createAppointmentStatuses();
        List<Doctor> doctors = doctorRepository.findAll();
        List<Patient> patients = createPatients();
        createAppointmentsAndHistories(doctors, patients);
    }

    private void createAppointmentStatuses() {
        Arrays.asList("AVAILABLE", "SCHEDULED", "COMPLETED", "CANCELLED")
                .forEach(status -> {
                    if (!statusRepository.existsById(status)) {
                        AppointmentStatus appointmentStatus = new AppointmentStatus();
                        appointmentStatus.setStatus(status);
                        statusRepository.save(appointmentStatus);
                    }
                });
    }

    private List<Doctor> createDoctors() {
        List<Doctor> doctors = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            String email = "dr." + lastNames[i].toLowerCase() + "@hospital.com";
            String name = "Dr. " + firstNames[i] + " " + lastNames[i];
            Doctor doctor = createDoctor(email, name, specializations[i], "password");
            doctors.add(doctor);
        }
        return doctors;
    }

    private Doctor createDoctor(String email, String name, String specialization, String password) {
        Optional<Doctor> existingDoctor = doctorRepository.findById(email);
        if (existingDoctor.isEmpty()) {
            Doctor doctor = new Doctor();
            doctor.setEmail(email);
            doctor.setName(name);
            doctor.setSpecialization(specialization);
            doctor.setPassword(password);
            return doctorRepository.save(doctor);
        }
        return existingDoctor.get();
    }

    private List<Patient> createPatients() {
        List<Patient> patients = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            String email = firstNames[i].toLowerCase() + "." + lastNames[i].toLowerCase() + "@email.com";
            Patient patient = createPatient(email, firstNames[i], lastNames[i], "password",
                    i % 2 == 0 ? "Male" : "Female",
                    getRandomBirthDate(),
                    "123-456-" + String.format("%04d", i),
                    i + " Main St, City",
                    "Insurance Provider " + (i % 5 + 1),
                    "INS" + String.format("%06d", i));
            patients.add(patient);
        }
        return patients;
    }

    private LocalDate getRandomBirthDate() {
        Random random = new Random();
        int yearsAgo = 20 + random.nextInt(60); // Ages between 20 and 80
        return LocalDate.now().minusYears(yearsAgo);
    }

    private Patient createPatient(String email, String firstName, String lastName, String password,
                                  String gender, LocalDate dateOfBirth, String phoneNumber, String address,
                                  String insuranceProvider, String insurancePolicyNumber) {

        Patient existingPatient = patientRepository.findByEmail(email);
        if (existingPatient == null) {
            Patient patient = new Patient();
            patient.setEmail(email);
            patient.setFirstName(firstName);
            patient.setLastName(lastName);
            patient.setPassword(password);
            patient.setGender(gender);
            patient.setDateOfBirth(dateOfBirth);
            patient.setPhoneNumber(phoneNumber);
            patient.setAddress(address);
            patient.setInsuranceProvider(insuranceProvider);
            patient.setInsurancePolicyNumber(insurancePolicyNumber);
            return patientRepository.save(patient);
        }
        return existingPatient;
    }

    private void createAppointmentsAndHistories(List<Doctor> doctors, List<Patient> patients) {
        Random random = new Random();
        AppointmentStatus scheduled = statusRepository.findById("SCHEDULED").orElse(null);
        AppointmentStatus completed = statusRepository.findById("COMPLETED").orElse(null);

        for (Doctor doctor : doctors) {
            // Create 50+ appointments
            for (int i = 0; i < 10; i++) {
                Patient patient = patients.get(random.nextInt(patients.size()));
                LocalDate date = LocalDate.now().plusDays(random.nextInt(30)); // Appointments within next 30 days
                LocalTime time = LocalTime.of(9 + random.nextInt(8), 0); // Between 9 AM and 4 PM
                AppointmentStatus status = random.nextBoolean() ? scheduled : completed;
                createAppointment(doctor, patient, date, time, status);
                // Create patient history for completed appointments
                if (status == completed) {
                    createRandomPatientHistory(patient);
                }
            }
        }
    }

    private void createAppointment(Doctor doctor, Patient patient, LocalDate date, LocalTime time, AppointmentStatus status) {
        Appointment appointment = new Appointment();
        appointment.setDoctor(doctor);
        appointment.setPerson(patient);
        appointment.setDate(date);
        appointment.setTime(time);
        appointment.setAppointmentStatus(status);
        appointment.setPrice(80.0 + new Random().nextInt(421)); // Random price between 80 and 500
        appointmentRepository.save(appointment);
    }

    private void createRandomPatientHistory(Patient patient) {
        Random random = new Random();

        String randomSymptoms = symptoms[random.nextInt(symptoms.length)];
        if (random.nextBoolean()) {
            randomSymptoms += ", " + symptoms[random.nextInt(symptoms.length)];
        }

        String randomDiagnosis = diagnoses[random.nextInt(diagnoses.length)];
        String randomMedications = medications[random.nextInt(medications.length)];
        if (random.nextBoolean()) {
            randomMedications += ", " + medications[random.nextInt(medications.length)];
        }

        createPatientHistory(patient,
                randomSymptoms,
                randomDiagnosis,
                randomMedications,
                allergies[random.nextInt(allergies.length)],
                bloodTypes[random.nextInt(bloodTypes.length)],
                50.0 + random.nextDouble() * 50.0, // Weight between 50-100 kg
                150.0 + random.nextDouble() * 50.0, // Height between 150-200 cm
                chronicConditions[random.nextInt(chronicConditions.length)],
                "Family history of " + diagnoses[random.nextInt(diagnoses.length)]);
    }

    private void createPatientHistory(Patient patient, String symptoms, String diagnosis,
                                      String medications, String allergies, String bloodType, Double weight,
                                      Double height, String chronicConditions, String familyHistory) {
        PatientHistory history = new PatientHistory();
        history.setPatient(patient);
        history.setRecordDate(LocalDate.now().minusDays(new Random().nextInt(365))); // Within last year
        history.setSymptoms(symptoms);
        history.setDiagnosis(diagnosis);
        history.setMedications(medications);
        history.setAllergies(allergies);
        history.setBloodType(bloodType);
        history.setWeight(weight);
        history.setHeight(height);
        history.setChronicConditions(chronicConditions);
        history.setFamilyHistory(familyHistory);
        historyRepository.save(history);
    }
}
