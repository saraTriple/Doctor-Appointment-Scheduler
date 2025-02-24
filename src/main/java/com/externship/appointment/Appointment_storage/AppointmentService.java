package com.externship.appointment.Appointment_storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
        return appointmentRepository.findByAppointmentStatus_Status(status, pageable);
    }

    public long count() {
        return appointmentRepository.count();
    }

    public double calculateTotalRevenue() {
        List<Appointment> appointments = appointmentRepository.findAll();
        return appointments.stream()
                .filter(appointment -> appointment.getAppointmentStatus().getStatus().equals("COMPLETED"))
                .mapToDouble(Appointment::getPrice)
                .sum();
    }

    public Map<String, Long> getAppointmentStatusCounts() {
        List<Appointment> appointments = appointmentRepository.findAll();
        return appointments.stream()
                .collect(Collectors.groupingBy(
                        appointment -> appointment.getAppointmentStatus().getStatus(),
                        Collectors.counting()
                ));
    }

    public List<String> getRevenueDates() {
        List<Appointment> completedAppointments = appointmentRepository.findByAppointmentStatus_Status("COMPLETED");
        return completedAppointments.stream()
                .map(appointment -> appointment.getDate().toString())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

    public List<Double> getRevenueData() {
        List<String> dates = getRevenueDates();
        List<Double> revenueData = new ArrayList<>();
        
        for (String date : dates) {
            double dailyRevenue = appointmentRepository.findByAppointmentStatus_Status("COMPLETED").stream()
                    .filter(appointment -> appointment.getDate().toString().equals(date))
                    .mapToDouble(Appointment::getPrice)
                    .sum();
            revenueData.add(dailyRevenue);
        }
        
        return revenueData;
    }
}
