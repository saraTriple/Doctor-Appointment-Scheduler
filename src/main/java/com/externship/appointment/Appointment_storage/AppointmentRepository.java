package com.externship.appointment.Appointment_storage;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface AppointmentRepository extends JpaRepository<Appointment, Long> {
    Optional<Appointment> findByDateAndTimeAndDoctor_Email(LocalDate date, LocalTime time, String doctorEmail);

    List<Appointment> findByDoctor_Email(String doctorEmail);

    List<Appointment> findByPerson_Email(String patientEmail);

    // Add pagination support
    Page<Appointment> findAll(Pageable pageable);

    Page<Appointment> findByDoctor_Email(String doctorEmail, Pageable pageable);

    // Add status-based queries
    @Query("SELECT a FROM Appointment a WHERE a.appointmentStatus.status = :status")
    Page<Appointment> findByStatus(@Param("status") String status, Pageable pageable);

    // Add date and time based queries
    @Query("SELECT a FROM Appointment a WHERE a.date = :date")
    Page<Appointment> findByDate(@Param("date") LocalDate date, Pageable pageable);

    @Query("SELECT a FROM Appointment a WHERE a.time = :time")
    Page<Appointment> findByTime(@Param("time") LocalTime time, Pageable pageable);

    @Query("SELECT a FROM Appointment a WHERE a.date = :date AND a.time = :time")
    Page<Appointment> findByDateAndTime(@Param("date") LocalDate date, @Param("time") LocalTime time, Pageable pageable);

    @Query("SELECT a FROM Appointment a WHERE a.doctor.email = :email")
    Page<Appointment> findByDoctorEmail(@Param("email") String email, Pageable pageable);
}
