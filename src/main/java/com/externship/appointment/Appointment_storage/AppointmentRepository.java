package com.externship.appointment.Appointment_storage;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment,Long> {
    List<Appointment> findByDateAndTimeAndDoctor_Email(LocalDate date, LocalTime time, String doctorEmail);
    Page<Appointment> findByDoctor_Email(String doctorEmail, Pageable pageable);
    Page<Appointment> findByPerson_Email(String patientEmail, Pageable pageable);
    List<Appointment> findByDoctor_Email(String doctorEmail);
    List<Appointment> findByPerson_Email(String patientEmail);
    List<Appointment> findByPerson_Id(Long personId);
}
