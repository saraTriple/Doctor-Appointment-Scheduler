package com.externship.appointment.Patient_storage;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PatientRepository extends JpaRepository<Patient,String> {
	Patient findByEmail(String email);
	
}
