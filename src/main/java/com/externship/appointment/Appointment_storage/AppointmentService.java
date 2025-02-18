package com.externship.appointment.Appointment_storage;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AppointmentService {
    @Autowired
    private AppointmentRepository appointmentRepository;

    public List<Appointment> findAllByEmail(String email) {
        return appointmentRepository.findByDoctor_Email(email);
    }
    
    public List<Appointment> findByDocId(String doctorEmail) {
        return appointmentRepository.findByDoctor_Email(doctorEmail);
    }

    public List<Appointment> findByPesonId(String personId) {
        return appointmentRepository.findByPerson_Email(personId);
    }
    
    public List<Appointment> getAllAppointments() {
        return appointmentRepository.findAll();
    }
    
    public void createAppointment(Appointment appointment) {
        appointmentRepository.save(appointment);
    }
}
