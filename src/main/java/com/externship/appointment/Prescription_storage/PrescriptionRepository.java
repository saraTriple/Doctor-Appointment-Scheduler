package com.externship.appointment.Prescription_storage;

import com.externship.appointment.Appointment_storage.Appointment;
import com.externship.appointment.Patient_storage.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface PrescriptionRepository extends JpaRepository<Prescription, Long> {
    Optional<Prescription> findByAppointment(Appointment appointment);
    
    @Query("SELECT p FROM Prescription p WHERE p.patient.email = :patientId ORDER BY p.prescriptionDate DESC")
    List<Prescription> findByPatientOrderByDateDesc(String patientId);
}
