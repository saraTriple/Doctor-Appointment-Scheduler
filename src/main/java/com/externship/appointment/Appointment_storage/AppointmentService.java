package com.externship.appointment.Appointment_storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AppointmentService {

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private AppointmentStatusRepository appointmentStatusRepository;

    public Page<Appointment> findAllPaginated(Pageable pageable) {
        return appointmentRepository.findAll(pageable);
    }

    public Optional<Appointment> findById(Long id) {
        return appointmentRepository.findById(id);
    }

    public Appointment saveAppointment(Appointment appointment) {
        appointment.setAppointmentStatus(appointmentStatusRepository.findByStatus("SCHEDULED").get());
        return appointmentRepository.save(appointment);
    }

    public Appointment updateAppointment(Appointment appointment) {
        if (appointmentRepository.existsById(appointment.getId())) {
            return appointmentRepository.save(appointment);
        }
        throw new RuntimeException("Appointment not found");
    }

    public void cancelAppointment(Long id) {
        Optional<Appointment> appointment = appointmentRepository.findById(id);
        if (appointment.isPresent()) {
            Appointment app = appointment.get();
            app.setAppointmentStatus(appointmentStatusRepository.findByStatus("CANCELLED").get());
            appointmentRepository.save(app);
        } else {
            throw new RuntimeException("Appointment not found");
        }
    }

    public Page<Appointment> findByDoctorId(String doctorEmail, Pageable pageable) {
        return appointmentRepository.findByDoctor_Email(doctorEmail, pageable);
    }
    public List<Appointment> findByDoctorId(String doctorEmail) {
        return appointmentRepository.findByDoctor_Email(doctorEmail);
    }

    public Page<Appointment> findByPatientEmail(String patientEmail, Pageable pageable) {
        return appointmentRepository.findByPerson_Email(patientEmail, pageable);
    }

    public List<Appointment> findByPatientId(Long personId) {
        return appointmentRepository.findByPerson_Id(personId);
    }
}
