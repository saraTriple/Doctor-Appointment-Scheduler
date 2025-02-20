package com.externship.appointment.initializers;

import com.externship.appointment.Doctor_storage.Doctor;
import com.externship.appointment.Doctor_storage.DoctorRepository;
import com.externship.appointment.Patient_storage.Patient;
import com.externship.appointment.Patient_storage.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
@Order(1)
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private static final List<String> PERSIAN_FIRST_NAMES = Arrays.asList(
        "Ali", "Mohammad", "Hossein", "Reza", "Amir", "Mehdi", "Sara", "Fateme", "Zahra", "Maryam",
        "Nima", "Parsa", "Arash", "Pouya", "Sina", "Nazanin", "Narges", "Yasmin", "Aida", "Shima"
    );

    private static final List<String> PERSIAN_LAST_NAMES = Arrays.asList(
        "Hosseini", "Mohammadi", "Rezaei", "Mousavi", "Alavi", "Karimi", "Najafi", "Hashemi", "Sadeghi", "Ahmadi",
        "Akbari", "Azizi", "Farhadi", "Rahmani", "Jafari", "Yousefi", "Moradi", "Ghasemi", "Salehi", "Taheri"
    );

    private static final List<String> SPECIALIZATIONS = Arrays.asList(
        "Cardiologist", "Neurologist", "Internal Medicine", "Pediatrician", "Gynecologist",
        "Ophthalmologist", "ENT Specialist", "Dermatologist", "General Surgeon", "Dentist"
    );

    @Override
    public void run(String... args) {
        if (doctorRepository.count() == 0) {
            createDoctors();
        }
        if (patientRepository.count() == 0) {
            createPatients();
        }
    }

    private void createDoctors() {
        for (int i = 0; i < 10; i++) {
            Doctor doctor = new Doctor();
            String firstName = PERSIAN_FIRST_NAMES.get(i);
            String lastName = PERSIAN_LAST_NAMES.get(i);
            doctor.setName(firstName + " " + lastName);
            doctor.setEmail(firstName.toLowerCase() + "." + lastName.toLowerCase() + "@hospital.com");
            doctor.setPassword(passwordEncoder.encode("password"));
            doctor.setSpecialization(SPECIALIZATIONS.get(i));
//            doctor.setPhoneNumber("09" + String.format("%09d", i + 100000000));
            doctorRepository.save(doctor);
        }
    }

    private void createPatients() {
        for (int i = 10; i < 20; i++) {
            Patient patient = new Patient();
            String firstName = PERSIAN_FIRST_NAMES.get(i);
            String lastName = PERSIAN_LAST_NAMES.get(i);
            patient.setFirstName(firstName);
            patient.setLastName(lastName);
            patient.setEmail(firstName.toLowerCase() + "." + lastName.toLowerCase() + "@email.com");
            patient.setPassword(passwordEncoder.encode("password"));
            patient.setPhoneNumber("09" + String.format("%09d", i + 100000000));
            patient.setAddress("Tehran, " + PERSIAN_LAST_NAMES.get(i) + " Street");
            patient.setGender(i % 2 == 0 ? "Male" : "Female");
            patientRepository.save(patient);
        }
    }
}
