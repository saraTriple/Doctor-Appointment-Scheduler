package com.externship.appointment.initializers;

import com.externship.appointment.Patient_storage.Patient;
import com.externship.appointment.Patient_storage.PatientRepository;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Component
@Order(4)
public class PatientInitializer {

    private final PatientRepository patientRepository;
    private final PasswordEncoder passwordEncoder;

    public PatientInitializer(PatientRepository patientRepository, PasswordEncoder passwordEncoder) {
        this.patientRepository = patientRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostConstruct
    public void init() {
        if (patientRepository.count() == 0) {
            List<Patient> patients = new ArrayList<>();
            for (int i = 1; i <= 50; i++) {
                Patient patient = new Patient();
                patient.setEmail("patient" + i + "@example.com");
                patient.setPassword(passwordEncoder.encode("password" + i));
                patient.setFirstName("FirstName" + i);
                patient.setLastName("LastName" + i);
                patient.setGender(i % 2 == 0 ? "Male" : "Female");
                patient.setDateOfBirth(LocalDate.now().minusYears(20 + (i % 30)));
                patient.setPhoneNumber("+123456789" + i);
                patient.setAddress("Address " + i);
                patient.setInsuranceProvider(i % 3 == 0 ? "Insurance Co " + (i % 5) : null);
                patient.setInsurancePolicyNumber(i % 3 == 0 ? "POLICY" + i : null);
                patients.add(patient);
            }
            patientRepository.saveAll(patients);
        }
    }
}
