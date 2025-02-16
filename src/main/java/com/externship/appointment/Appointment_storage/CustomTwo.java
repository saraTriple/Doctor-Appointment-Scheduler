package com.externship.appointment.Appointment_storage;

import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CustomTwo{
    @Query("select a from Appointment a " +
            "where a.doctor.email = :email")
    public List<Appointment> findByDoctorEmail(String email);
}