package com.externship.appointment.Appointment_storage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
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

        List<BigDecimal> completed = appointments.stream()
                .filter(appointment -> appointment.getAppointmentStatus().getStatus().equals("COMPLETED"))
                .map(Appointment::getPrice).collect(Collectors.toList());
        int sum = 0;

        for (BigDecimal c: completed) {
            sum += c.intValue();
        }
        return sum;
    }

    public Map<String, Long> getAppointmentStatusCounts() {
        return appointmentRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        appointment -> appointment.getAppointmentStatus().getStatus(),
                        Collectors.counting()
                ));
    }

    public List<String> getRevenueDates() {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30); // Last 30 days

        return appointmentRepository.findByAppointmentStatus_StatusAndDateBetween(
                "COMPLETED", startDate, endDate).stream()
                .map(appointment -> appointment.getDate().toString())
                .distinct()
                .sorted()
                .collect(Collectors.toList());
    }

//    public List<Double> getRevenueData() {
//        LocalDate endDate = LocalDate.now();
//        LocalDate startDate = endDate.minusDays(30); // Last 30 days
//
//        List<Appointment> completedAppointments = appointmentRepository.findByAppointmentStatus_StatusAndDateBetween(
//                "COMPLETED", startDate, endDate);
//
//        Map<String, Double> dailyRevenue = completedAppointments.stream()
//                .collect(Collectors.groupingBy(
//                        appointment -> appointment,
//                        Collectors.summingDouble(Appointment::getPrice)
//                ));
//
//        return getRevenueDates().stream()
//                .map(date -> dailyRevenue.getOrDefault(date, 0.0))
//                .collect(Collectors.toList());
//    }
}
