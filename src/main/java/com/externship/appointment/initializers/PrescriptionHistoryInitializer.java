package com.externship.appointment.initializers;

import com.externship.appointment.Patient_storage.Patient;
import com.externship.appointment.Patient_storage.PatientRepository;
import com.externship.appointment.Prescription_storage.Prescription;
import com.externship.appointment.Prescription_storage.PrescriptionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Component
@Order(5)
public class PrescriptionHistoryInitializer implements CommandLineRunner {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    private static final List<String> COMMON_SYMPTOMS = Arrays.asList(
        "Fever and Fatigue", "Chest Pain", "Digestive Issues", "Skin Rash",
        "Dizziness", "Chronic Pain", "Respiratory Problems", "Vision Problems"
    );

    private static final List<String> COMMON_DIAGNOSES = Arrays.asList(
        "Influenza", "Angina", "IBS", "Eczema",
        "Vertigo", "Fibromyalgia", "Asthma", "Myopia"
    );

    private static final List<List<String>> COMMON_MEDICATIONS = Arrays.asList(
        Arrays.asList("Oseltamivir 75mg", "Acetaminophen 500mg"),
        Arrays.asList("Nitroglycerin 0.4mg", "Aspirin 81mg"),
        Arrays.asList("Dicyclomine 10mg", "Probiotics"),
        Arrays.asList("Hydrocortisone 1%", "Antihistamine"),
        Arrays.asList("Meclizine 25mg", "Vitamin B12"),
        Arrays.asList("Pregabalin 75mg", "Duloxetine 30mg"),
        Arrays.asList("Albuterol Inhaler", "Montelukast 10mg"),
        Arrays.asList("Eye Drops", "Vitamin A")
    );

    private static final List<String> DOCTOR_NOTES = Arrays.asList(
        "Patient responding well to treatment",
        "Symptoms improving with current medication",
        "Regular monitoring required",
        "Consider lifestyle modifications",
        "Follow-up needed in 2 weeks",
        "Condition stable with current regimen",
        "Gradual improvement observed",
        "Continue current treatment plan"
    );

    @Override
    public void run(String... args) {
        List<Patient> patients = patientRepository.findAll();
        for (Patient patient : patients) {
            createPrescriptionHistory(patient);
        }
    }

    private void createPrescriptionHistory(Patient patient) {
        // Create a prescription from 3 months ago
        int index = (int) (Math.random() * COMMON_SYMPTOMS.size());
        
        Prescription prescription = new Prescription();
        prescription.setPatient(patient);
        prescription.setPrescriptionDate(LocalDate.now().minusMonths(3));
        prescription.setSymptoms(COMMON_SYMPTOMS.get(index));
        prescription.setDiagnosis(COMMON_DIAGNOSES.get(index));
        prescription.setMedications(COMMON_MEDICATIONS.get(index));
        prescription.setDoctorNotes(DOCTOR_NOTES.get(index));
        prescription.setFollowUpInstructions("Schedule follow-up in 1 month");

        prescriptionRepository.save(prescription);
        System.out.println("Created prescription history for patient: " + patient.getEmail());
    }
}
