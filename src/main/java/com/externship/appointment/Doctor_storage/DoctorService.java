package com.externship.appointment.Doctor_storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class DoctorService {

    @Autowired
    private DoctorRepository doctorRepository;

    public long count() {
        return doctorRepository.count();
    }

    public List<Doctor> findAll() {
        return doctorRepository.findAll();
    }

    public List<String> getAllSpecialties() {
        return doctorRepository.findAll().stream()
                .map(Doctor::getSpecialization)
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }
}
