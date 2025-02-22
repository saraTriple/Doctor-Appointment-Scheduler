package com.externship.appointment.Patient_storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PatientService {

    @Autowired
    private PatientRepository patientRepository;

    public long count() {
        return patientRepository.count();
    }
}
