package com.externship.appointment.Appointment_storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppointmentService {
    @Autowired
    private AppointmentRepository appointmentRepository;

    public List<Appointment> findAllByEmail(String email) {
        return appointmentRepository.findByPerson_Email(email);
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
    
    // Add paginated methods
    public Page<Appointment> getAllAppointmentsPaginated(Pageable pageable) {
        return appointmentRepository.findAll(pageable);
    }
    
    public Page<Appointment> getAppointmentsByDoctor(String doctorEmail, Pageable pageable) {
        return appointmentRepository.findByDoctor_Email(doctorEmail, pageable);
    }
    
    public Page<Appointment> getAppointmentsByStatus(String status, Pageable pageable) {
        return appointmentRepository.findByStatus(status, pageable);
    }
}
