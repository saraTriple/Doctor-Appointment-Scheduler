package com.externship.appointment.initializers;

import com.externship.appointment.Appointment_storage.AppointmentStatusRepository;
import com.externship.appointment.Patient_storage.Patient;
import com.externship.appointment.Patient_storage.PatientRepository;
import com.externship.appointment.Patient_history.PatientHistory;
import com.externship.appointment.Patient_history.PatientHistoryRepository;
import com.externship.appointment.Prescription_storage.Prescription;
import com.externship.appointment.Prescription_storage.PrescriptionRepository;
import com.externship.appointment.Appointment_storage.Appointment;
import com.externship.appointment.Appointment_storage.AppointmentRepository;
import com.externship.appointment.Doctor_storage.Doctor;
import com.externship.appointment.Doctor_storage.DoctorRepository;
import com.externship.appointment.Appointment_storage.AppointmentStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

@Component
@Order(6)
public class PatientRecordsInitializer implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(PatientRecordsInitializer.class);

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private PatientHistoryRepository patientHistoryRepository;

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private AppointmentStatusRepository appointmentStatusRepository;

    private static final List<String> COMMON_SYMPTOMS = Arrays.asList(
        "Headache and Fever", "Sore Throat", "Back Pain", "Stomach Pain", 
        "Allergic Reaction", "Common Cold", "Joint Pain", "High Blood Pressure"
    );

    private static final List<String> COMMON_DIAGNOSES = Arrays.asList(
        "Viral Infection", "Strep Throat", "Muscle Strain", "Gastritis",
        "Seasonal Allergies", "Upper Respiratory Infection", "Arthritis", "Hypertension"
    );

    private static final List<List<String>> COMMON_MEDICATIONS = Arrays.asList(
        Arrays.asList("Acetaminophen 500mg", "Ibuprofen 400mg"),
        Arrays.asList("Amoxicillin 500mg", "Throat Lozenges"),
        Arrays.asList("Diclofenac 50mg", "Muscle Relaxant"),
        Arrays.asList("Omeprazole 20mg", "Antacid"),
        Arrays.asList("Cetirizine 10mg", "Nasal Spray"),
        Arrays.asList("Diphenhydramine 25mg", "Cough Syrup"),
        Arrays.asList("Naproxen 500mg", "Glucosamine"),
        Arrays.asList("Amlodipine 5mg", "Losartan 50mg")
    );

    @Override
    public void run(String... args) {
        System.out.println("Initializing patient records...");
        
        List<Patient> patients = patientRepository.findAll();
        for (Patient patient : patients) {
            // Check if patient already has medical history
            if (patientHistoryRepository.findByPatientOrderByRecordDateDesc(patient).isEmpty()) {
                createInitialMedicalHistory(patient);
            }

            // Check if patient already has prescriptions
            if (prescriptionRepository.findByPatientOrderByDateDesc(patient.getId()).isEmpty()) {
                createInitialPrescription(patient);
            }

            // Check if patient already has an appointment
            if (appointmentRepository.findByPerson_Email(patient.getEmail()).isEmpty()) {
                createInitialAppointment(patient);
            }
        }
        
        System.out.println("Patient records initialization completed.");
    }

    private void createInitialMedicalHistory(Patient patient) {
        PatientHistory history = new PatientHistory();
        history.setPatient(patient);
        history.setBloodType(getRandomBloodType());
        history.setWeight(70.0);
        history.setHeight(170.0);
        history.setAllergies("No known allergies");
        history.setRecordDate(LocalDate.now());
        history.setFamilyHistory("No significant family history");

        patientHistoryRepository.save(history);
        System.out.println("Created initial medical history for patient: " + patient.getEmail());
    }

    private void createInitialPrescription(Patient patient) {
        int index = (int) (Math.random() * COMMON_SYMPTOMS.size());
        
        Prescription prescription = new Prescription();
        prescription.setPatient(patient);
        prescription.setPrescriptionDate(LocalDate.now());
        prescription.setSymptoms(COMMON_SYMPTOMS.get(index));
        prescription.setDiagnosis(COMMON_DIAGNOSES.get(index));
        prescription.setMedications(COMMON_MEDICATIONS.get(index));
        prescription.setDoctorNotes("Regular follow-up recommended");
        prescription.setFollowUpInstructions("Return in 2 weeks if symptoms persist");

        if (prescription.getPatient() == null) {
            logger.warn("Prescription is being saved with a null patient. Prescription ID: " + prescription.getId());
        } else {
            logger.info("Saving Prescription for Patient: " + prescription.getPatient().getId());
        }

        prescriptionRepository.save(prescription);
        System.out.println("Created initial prescription for patient: " + patient.getEmail());
    }

    private void createInitialAppointment(Patient patient) {
        List<Doctor> doctors = doctorRepository.findAll();
        if (!doctors.isEmpty()) {
            Doctor doctor = doctors.get(0);
            Appointment appointment = new Appointment();
            appointment.setPerson(patient);
            appointment.setDoctor(doctor);
            appointment.setDate(LocalDate.now().plusDays(7));
            appointment.setTime(LocalTime.of(10, 0));
            AppointmentStatus scheduledStatus = appointmentStatusRepository.findById("SCHEDULED").get();
            appointment.setAppointmentStatus(scheduledStatus);
            appointment.setPrice(10.0);

            appointmentRepository.save(appointment);
            System.out.println("Created initial appointment for patient: " + patient.getEmail());
        } else {
            logger.warn("No doctors found in the system. Skipping appointment creation for patient: " + patient.getEmail());
        }
    }

    private String getRandomBloodType() {
        String[] bloodTypes = {"A+", "A-", "B+", "B-", "O+", "O-", "AB+", "AB-"};
        return bloodTypes[(int) (Math.random() * bloodTypes.length)];
    }
}
