package com.externship.appointment.Appointment_storage;

import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment,Long> {
    List<Appointment> findByDateAndTimeAndDoctor_Email(LocalDate date, LocalTime time, String doctorEmail);
    List<Appointment> findByDoctor_Email(String doctorEmail);
    List<Appointment> findByPerson_Email(String patientEmail);
}
