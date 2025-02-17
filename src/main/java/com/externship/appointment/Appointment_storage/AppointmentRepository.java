package com.externship.appointment.Appointment_storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface AppointmentRepository extends JpaRepository<Appointment,String>,CustomTwo {
    List<Appointment> findByDateAndTimeAndDoctor_Email(LocalDate date, LocalTime time, String doctorEmail);
}
