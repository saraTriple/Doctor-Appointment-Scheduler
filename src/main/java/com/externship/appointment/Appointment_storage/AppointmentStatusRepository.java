package com.externship.appointment.Appointment_storage;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AppointmentStatusRepository extends JpaRepository<AppointmentStatus, String> {
    Optional<AppointmentStatus> findByStatus(String status);
}
