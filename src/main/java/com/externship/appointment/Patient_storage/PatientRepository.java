package com.externship.appointment.Patient_storage;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PatientRepository extends JpaRepository<Patient,String> {
	Optional<Patient> findByEmail(String email);
	
}
